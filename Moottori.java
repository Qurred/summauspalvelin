import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Moottori{

	//-- Muuttujien esittelu
	//-- final intit
	private static final int KUUNNELTAVAPORTTI = 2000;
	private static final int ODOTUSAIKA = 5000; //5s
	private static final int VIRHEKOODI = -1;
	
	//-- Yhteysmuuttujat
	private  ServerSocket soketti;
	private  Socket asiakas;
	private  ObjectInputStream dvs;
	private  ObjectOutputStream dus;
	private  DatagramSocket dataSoketti;
	
	//-- Dataa
	private  boolean verbose;
	private  Data data;
	private  Summauspalvelu[] summauspalvelimet;

	public static void main(String[] args){
		Moottori moottori;
		if(args.length < 2){
			System.err.println("Annettiin "+args.length+" parametria vaikka tarvitaan kaksi.\n"
					+ "Syˆt‰ parametrit muotoa:\n[Osoite] [Portti]");
			System.exit(0);
		}
		if(args.length > 2 && args[2].equals("verbose")){
			moottori = new Moottori(args[0], args[1], Integer.toString(KUUNNELTAVAPORTTI), true);
		}else{
			moottori = new Moottori(args[0], args[1], Integer.toString(KUUNNELTAVAPORTTI), false);
		}
	}

	public Moottori(String osoite, String kohdePortti, String viesti, boolean verbose){
		this.verbose = verbose;
		if(verbose)System.out.println("K‰ynnistet‰‰n summauspalvelua..");

		lahetaUDP(osoite, Integer.parseInt(kohdePortti), Integer.toString(KUUNNELTAVAPORTTI));

		//Yritet‰‰n muodostaa TCP-yhteys ja toistetaan 5 kertaa, jos yhteytt‰ ei ole viel‰k‰‰n muodostettu niin suljetaan sovellus
		int yritykset = 0; //Pit‰‰ kirjaa kuinka monta yrityst‰ on tehty
		while(yritykset < 5){
			if(muodostaTCP()){
				break; //Yhteyden luonti onnistui ja t‰ten voidaan poistua while-loopista
			}else{//Yhteyden muodostus ei onnistunut
				yritykset++;
				if(yritykset == 5){ //Jos yrityksi‰ on tehty jo viisi
					if(verbose)System.out.println("TCP-yhteytt‰ ei saatu muodostettua 5 yrityksen j‰lkeen\nSuljetaan sovellus");
					System.exit(1000);//Sammutetaan sovellus 1 sekunnin p‰‰st‰
				}
				if(verbose)System.out.println("Yritet‰‰n uudelleen...");
				lahetaUDP(osoite, Integer.parseInt(kohdePortti), Integer.toString(KUUNNELTAVAPORTTI));
			}
		}

		if(verbose)System.out.println("Otetaan selv‰‰ kuinka monta porttia halutaan");

		//-- Selvitet‰‰n Y:lt‰ montako summauspalvelinta tarvitaan
		int tarvittavaMaara = selvitaTarvittavatPortit();
		
		//-- muuttujien alustamista
		summauspalvelimet = new Summauspalvelu[tarvittavaMaara];
		data = new Data(tarvittavaMaara, KUUNNELTAVAPORTTI);

		if(verbose)System.out.println("Pyydettiin " + tarvittavaMaara+" summauspalvelua");
		
		//-- Luodaan summauspalvelimet ja l‰hetet‰‰n Y:lle niiden portit
		lahetaJaKaunnistaPalvelimet(tarvittavaMaara);

		try {
			odotaPalvelinta();
		} catch (SocketTimeoutException e) {
			if(verbose)System.out.println("Viime utelusta minuutti\nSuljetaan palvelin ja sen osat");
		}
		suljePalvelimet();
		//if(verbose)data.tulostaTiedot(); //Tulostaa Data-olion tiedot
		if(verbose)System.out.println("Sammutetaan sovellus");
	}


	/**
	 * L‰hett‰‰ parametrina annettuun osoitteeseen paketin joka sis‰lt‰‰
	 * @param osoite
	 * @param kohdePortti
	 * @param viesti
	 */
	private void lahetaUDP(String osoite, int kohdePortti, String viesti){
		if(verbose) System.out.println("L‰hetet‰‰n UDP-paketti‰ osoitteeseen "+osoite+":"+kohdePortti + " viesti‰: \"" + viesti+"\"");
		try {
			InetAddress kohdeOsoite = InetAddress.getByName(osoite);
			dataSoketti = new DatagramSocket();
			byte[] lahetettavaData = Integer.toString(KUUNNELTAVAPORTTI).getBytes();
			DatagramPacket paketti = new DatagramPacket(lahetettavaData, lahetettavaData.length, kohdeOsoite, kohdePortti);
			dataSoketti.send(paketti);
			dataSoketti.close();
		} catch (IOException e) {
			System.err.println("UDP-paketin l‰hett‰minen ei onnistunut...");
			e.printStackTrace();
			System.exit(1000);
		}
		if(verbose) System.out.println("Paketti l‰hetetty onnistuneesti");
	}

	/**
	 * Yritt‰‰ muodostaa TCP-yhteyden
	 * Jos yhteyden muodostus ei onnistu niin palauttaa false, jos onnistuu niin palauttaa true
	 * Ottaa samalla talteen oliovirrat
	 * @return
	 */
	private boolean muodostaTCP(){
		try{
			if(verbose)System.out.println("Luodaan serverisoketti kuuntelemaan porttia " + KUUNNELTAVAPORTTI);
			soketti = new ServerSocket(KUUNNELTAVAPORTTI);
			soketti.setSoTimeout(ODOTUSAIKA);
			asiakas = soketti.accept(); //Hyv‰ksyt‰‰n serversokettiin yhteys ja siirret‰‰n asiakas sokettiin
			//Otetaan objektitietovirrat
			dvs = new ObjectInputStream(asiakas.getInputStream());//Sis‰‰n
			dus = new ObjectOutputStream(asiakas.getOutputStream());//Ulos
			if(verbose)System.out.println("Saatiin yhteys");
		}catch(SocketTimeoutException e){ //Soketti timeout
			try {
				if(verbose)System.out.println("Moottorin soketti timeout");
				if(dus != null){
					dus.writeInt(VIRHEKOODI);
					dus.flush();
					dvs.close();
					dus.close();
				}
				soketti.close(); //Suljetaan soketti
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		} catch (IOException e) {
			System.out.println("Ep‰onnistui pahasti");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Ottaa oliovirrasta int-tyyppisen arvon ja palauttaa sen
	 * @return tarvittavien s‰ikeiden m‰‰r‰n
	 */
	private int selvitaTarvittavatPortit(){
		try {
			return dvs.readInt(); //Luetaan oliovirrasta int-tyyppinen arvo
		} catch (IOException e) {
			e.printStackTrace();
			return -1; //Ei saatu mit‰‰n
		}	
	}


	/**
	 * L‰hett‰‰ portit joita k‰ytet‰‰n
	 * @param maara
	 */
	private void lahetaJaKaunnistaPalvelimet(int maara){
		if(verbose)System.out.println("L‰hetet‰‰n palvelimelle tiedot");
		for(int i=1;i <= maara;i++){
			try {
				summauspalvelimet[i-1]=new Summauspalvelu(KUUNNELTAVAPORTTI + i, data);
				summauspalvelimet[i-1].start();
				dus.writeInt(KUUNNELTAVAPORTTI+i);
				dus.flush();
			} catch (IOException e) {
				System.out.println("Portin l‰hett‰minen ep‰onnistui");
				e.printStackTrace();
			}
		}

	}
	/**
	 * Ottaa oliovirrasta int-tyyppisen arvon. Mik‰li arvo on 0, sulkee. Jos arvo on 1,
	 * kutsuu data-luokan metodia annaSumma ja tulostaa kyselyn tuloksen. Jos arvo on 2,
	 * kutsuu data-luokan metodia annaSuurinSummausPalvelin ja tulostaa kyselyn tuloksen.
	 * Jos arvo on 3, kutsuu annaLukumaara ja tulostaa kyselyn tuloksen.
	 * @throws SocketTimeoutException
	 */

	private void odotaPalvelinta() throws SocketTimeoutException{
		try {
			asiakas.setSoTimeout(60000);
			int tulos;
			while(true){
				odota(100);
				tulos = dvs.readInt();
				if(tulos == 0)break;
				else if(tulos == 1){
					data.tulostaTiedot();
					tulos = data.annaSumma();
					dus.writeInt(tulos);
					System.out.println("1 :" + tulos);
					dus.flush();
				}
				else if(tulos == 2){
					dus.writeInt(data.annaSuurinSummausPalvelin());
					System.out.println("2 :" + data.annaSuurinSummausPalvelin());
					dus.flush();
				}
				else if(tulos == 3){
					dus.writeInt(data.annaLukumaara());
					System.out.println("3 :" + data.annaLukumaara());
					dus.flush();
				}
				else{
					dus.writeInt(-1);
					dus.flush();
				}
			}
		}catch(IOException e){
			return;
		}
	}
	/**
	 * Sulkee summauspalvelimet
	 */

	private void suljePalvelimet(){
		for (int i = 0; i < summauspalvelimet.length; i++) {
			summauspalvelimet[i].sulje();
		}
	}

	/**
	 * Metodi, jonka avulla voidaan antaa sovelluksen lev‰t‰ hetki
	 * @param i
	 */
	private void odota(int i){
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}