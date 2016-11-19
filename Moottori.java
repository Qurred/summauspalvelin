import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Moottori{

	private static final int KUUNNELTAVAPORTTI = 20050;
	private static final int ODOTUSAIKA = 5000; //5s
	private static final int VIRHEKOODI = -1;
	private  ArrayList<Thread> summauspalvelimet;
	private  ServerSocket soketti;
	private  Socket asiakas;
	private  ObjectInputStream dvs;
	private  ObjectOutputStream dus;
	private  DatagramSocket dataSoketti;
	private  boolean verbose = false;
	private  Data data;
	
	/**
	 * Harjoitusty�ss� yhdistett�v� portti on 3126, poista my�hemm�ss� vaiheessa
	 */
	public static void main(String[] args){
		Moottori moottori;
		if(args.length < 2){
			System.err.println("Annettiin "+args.length+" parametria vaikka tarvitaan kaksi.\n"
					+ "Sy�t� parametrit muotoa:\n[Osoite] [Portti]");
			System.exit(0);
		}
		if(args.length > 2 && args[2].equals("verbose")){
			moottori = new Moottori(args[0], args[1], Integer.toString(KUUNNELTAVAPORTTI), true);
			//verbose = true; //Tarkistetaan oliko 3. parametri verbose
		}else{
			moottori = new Moottori(args[0], args[1], Integer.toString(KUUNNELTAVAPORTTI), true);
		}
	}
	
	public Moottori(String osoite, String kohdePortti, String viesti, boolean verbose){
		this.verbose = true;
		if(verbose)System.out.println("K�ynnistet��n summauspalvelua..");
		//Alustetaan ArrayList johon laitetaan threadit
		summauspalvelimet = new ArrayList<>();
		data = new Data();
		lahetaUDP(osoite, Integer.parseInt(kohdePortti), Integer.toString(KUUNNELTAVAPORTTI));
		//Yritet��n muodostaa TCP-yhteys ja toistetaan 5 kertaa, jos yhteytt� ei ole viel�k��n muodostettu niin suljetaan sovellus
		int yritykset = 0; //Pit�� kirjaa kuinka monta yrityst� on tehty
		while(yritykset < 5){
			//Odota TCP yhteytt�
			if(muodostaTCP()){
				break; //Yhteyden luonti onnistui ja t�ten voidaan poistua while-loopista
			}else{//Yhteyden muodostus ei onnistunut
				yritykset++;
				if(yritykset == 5){ //Jos yrityksi� on tehty jo viisi
					if(verbose)System.out.println("TCP-yhteytt� ei saatu muodostettua 5 yrityksen j�lkeen\nSuljetaan sovellus");
					System.exit(0);//Sammutetaan sovellus
				}
				if(verbose)System.out.println("Yritet��n uudelleen...");
				lahetaUDP(osoite, Integer.parseInt(kohdePortti), Integer.toString(KUUNNELTAVAPORTTI));
				}
		}
		
		if(verbose)System.out.println("Otetaan selv�� kuinka monta porttia halutaan");
		int tarvittavaMaara = selvitaTarvittavatPortit();
		if(verbose)System.out.println("Pyydettiin " + tarvittavaMaara+" summauspalvelua");
		lahetaPortit(tarvittavaMaara);
		luoSummauspalvelut(tarvittavaMaara);
		kaynnista(); //Tuo t�ll� hetkell� erroria
		try {
			odotaPalvelinta();
		} catch (SocketTimeoutException e) {
			suljePalvelimet();
			System.out.println("Viime utelusta minuutti\nSuljetaan palvelin");
			odota(1000);
			e.printStackTrace();
		}
		odota(5000);
		if(verbose)System.out.println("Sammutetaan sovellus");
	}

	
	/**
	 * L�hett�� parametrina annettuun osoitteeseen paketin joka sis�lt��
	 * @param osoite
	 * @param kohdePortti
	 * @param viesti
	 */
	private void lahetaUDP(String osoite, int kohdePortti, String viesti){
		if(verbose){
			System.out.println("L�hetet��n UDP-paketti� osoitteeseen "+osoite+":"+kohdePortti + " viesti�: \"" + viesti+"\"");
		}
		try {
			InetAddress kohdeOsoite = InetAddress.getByName(osoite);
			dataSoketti = new DatagramSocket();
			byte[] lahetettavaData = Integer.toString(KUUNNELTAVAPORTTI).getBytes();
			DatagramPacket paketti = new DatagramPacket(lahetettavaData, lahetettavaData.length, kohdeOsoite, kohdePortti);
			dataSoketti.send(paketti);
			dataSoketti.close();
		} catch (IOException e) {
			System.err.println("UDP-paketin l�hett�minen ei onnistunut...");
			e.printStackTrace();
			System.exit(0);
		}
		if(verbose){
			System.out.println("Paketti l�hetetty onnistuneesti");
		}
	}
	
	/**
	 * Yritt�� muodostaa TCP-yhteyden
	 * Jos yhteyden muodostus ei onnistu niin palauttaa false, jos onnistuu niin palauttaa true
	 * Ottaa samalla talteen oliovirrat
	 * @return
	 */
	private boolean muodostaTCP(){
		try{
			if(verbose)System.out.println("Luodaan serverisoketti kuuntelemaan porttia " + KUUNNELTAVAPORTTI);
			soketti = new ServerSocket(KUUNNELTAVAPORTTI);
			soketti.setSoTimeout(ODOTUSAIKA);
			asiakas = soketti.accept(); //Hyv�ksyt��n serversokettiin yhteys ja siirret��n asiakas sokettiin
			//Otetaan objektitietovirrat
			dvs = new ObjectInputStream(asiakas.getInputStream());//Sis��n
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
			System.out.println("Ep�onnistui pahasti");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Ottaa oliovirrasta int-tyyppisen arvon ja palauttaa sen
	 * @return tarvittavien s�ikeiden m��r�n
	 */
	private int selvitaTarvittavatPortit(){
		try {
			return dvs.readInt(); //Luetaan oliovirrasta int-tyyppinen arvo
		} catch (IOException e) {
			e.printStackTrace();
			return -1; //Ei saatu mit��n
		}	
	}
	
	/**
	 * K�ynnist�� summauspalvelu-s�ikeet
	 */
	private void kaynnista(){
		if(verbose) System.out.println("K�ynnistet��n summauspalvelimet...");
		for (Thread summauspalvelu : summauspalvelimet) {
			System.out.println("K�ynnistet��n");
			summauspalvelu.start();
			System.out.println("K�ynnistetty");
		}
		if(verbose) System.out.println("Summauspalvelimet k�ynnistetty");
	}

	/**
	 * L�hett�� portit joita k�ytet��n
	 * @param maara
	 */
	private void lahetaPortit(int maara){
		if(verbose)System.out.println("L�hetet��n palvelimelle tiedot");
		for(int i=1;i <= maara;i++){
			try {
				dus.writeInt(KUUNNELTAVAPORTTI+1);
				dus.flush();
				odota(50); //Ei kai pakolline
			} catch (IOException e) {
				System.out.println("Portin l�hett�minen ep�onnistui");
				e.printStackTrace();
			}
		}
		
	}
	
	private void luoSummauspalvelut(int maara){
		if(verbose)System.out.println("Aloitetaan luomaan summauspalvelimia...");
		for(int i = 0; i < maara; i++){
			if(verbose)System.out.println("Luodaan summauspalvelin kuuntelemaan porttia " + (KUUNNELTAVAPORTTI + i + 1));
			summauspalvelimet.add(new Thread(new Summauspalvelu(KUUNNELTAVAPORTTI + i + 1/*, data*/)));
		}
	}
	
	private void odotaSummauspalveluita(){
		for(Thread summauspalvelu : summauspalvelimet){
			try {
				summauspalvelu.join();
			} catch (InterruptedException e) {
				System.out.println("Ei voitu liitty� threadiin. Onko Thread pys�htynyt jo?");
				e.printStackTrace();
			}
		}
	}
	
	private void odotaPalvelinta() throws SocketTimeoutException{
		try {
			asiakas.setSoTimeout(60000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				switch (dvs.readInt()) {
				case 0:
					suljePalvelimet();
					System.out.println("Suljetaan sovellus");
					System.exit(0);
					break;
				case 1:
					dus.writeInt(data.annaSumma());
					dus.flush();
					break;
				case 2:
					dus.writeInt(data.annaSuurinSae());
					dus.flush();
					break;
				case 3:
					dus.writeInt(data.annaLukuMaara());
					dus.flush();
					break;
				default:
					dus.writeInt(-1);
					dus.flush();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void suljePalvelimet(){
		for (Thread sae : summauspalvelimet) {
			sae.interrupt();
		}
	}
	
	/**
	 * Metodi, jonka avulla voidaan antaa sovelluksen lev�t� hetki
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