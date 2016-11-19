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
	 * Harjoitustyössä yhdistettävä portti on 3126, poista myöhemmässä vaiheessa
	 */
	public static void main(String[] args){
		Moottori moottori;
		if(args.length < 2){
			System.err.println("Annettiin "+args.length+" parametria vaikka tarvitaan kaksi.\n"
					+ "Syötä parametrit muotoa:\n[Osoite] [Portti]");
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
		if(verbose)System.out.println("Käynnistetään summauspalvelua..");
		//Alustetaan ArrayList johon laitetaan threadit
		summauspalvelimet = new ArrayList<>();
		data = new Data();
		lahetaUDP(osoite, Integer.parseInt(kohdePortti), Integer.toString(KUUNNELTAVAPORTTI));
		//Yritetään muodostaa TCP-yhteys ja toistetaan 5 kertaa, jos yhteyttä ei ole vieläkään muodostettu niin suljetaan sovellus
		int yritykset = 0; //Pitää kirjaa kuinka monta yritystä on tehty
		while(yritykset < 5){
			//Odota TCP yhteyttä
			if(muodostaTCP()){
				break; //Yhteyden luonti onnistui ja täten voidaan poistua while-loopista
			}else{//Yhteyden muodostus ei onnistunut
				yritykset++;
				if(yritykset == 5){ //Jos yrityksiä on tehty jo viisi
					if(verbose)System.out.println("TCP-yhteyttä ei saatu muodostettua 5 yrityksen jälkeen\nSuljetaan sovellus");
					System.exit(0);//Sammutetaan sovellus
				}
				if(verbose)System.out.println("Yritetään uudelleen...");
				lahetaUDP(osoite, Integer.parseInt(kohdePortti), Integer.toString(KUUNNELTAVAPORTTI));
				}
		}
		
		if(verbose)System.out.println("Otetaan selvää kuinka monta porttia halutaan");
		int tarvittavaMaara = selvitaTarvittavatPortit();
		if(verbose)System.out.println("Pyydettiin " + tarvittavaMaara+" summauspalvelua");
		lahetaPortit(tarvittavaMaara);
		luoSummauspalvelut(tarvittavaMaara);
		kaynnista(); //Tuo tällä hetkellä erroria
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
	 * Lähettää parametrina annettuun osoitteeseen paketin joka sisältää
	 * @param osoite
	 * @param kohdePortti
	 * @param viesti
	 */
	private void lahetaUDP(String osoite, int kohdePortti, String viesti){
		if(verbose){
			System.out.println("Lähetetään UDP-pakettiä osoitteeseen "+osoite+":"+kohdePortti + " viestiä: \"" + viesti+"\"");
		}
		try {
			InetAddress kohdeOsoite = InetAddress.getByName(osoite);
			dataSoketti = new DatagramSocket();
			byte[] lahetettavaData = Integer.toString(KUUNNELTAVAPORTTI).getBytes();
			DatagramPacket paketti = new DatagramPacket(lahetettavaData, lahetettavaData.length, kohdeOsoite, kohdePortti);
			dataSoketti.send(paketti);
			dataSoketti.close();
		} catch (IOException e) {
			System.err.println("UDP-paketin lähettäminen ei onnistunut...");
			e.printStackTrace();
			System.exit(0);
		}
		if(verbose){
			System.out.println("Paketti lähetetty onnistuneesti");
		}
	}
	
	/**
	 * Yrittää muodostaa TCP-yhteyden
	 * Jos yhteyden muodostus ei onnistu niin palauttaa false, jos onnistuu niin palauttaa true
	 * Ottaa samalla talteen oliovirrat
	 * @return
	 */
	private boolean muodostaTCP(){
		try{
			if(verbose)System.out.println("Luodaan serverisoketti kuuntelemaan porttia " + KUUNNELTAVAPORTTI);
			soketti = new ServerSocket(KUUNNELTAVAPORTTI);
			soketti.setSoTimeout(ODOTUSAIKA);
			asiakas = soketti.accept(); //Hyväksytään serversokettiin yhteys ja siirretään asiakas sokettiin
			//Otetaan objektitietovirrat
			dvs = new ObjectInputStream(asiakas.getInputStream());//Sisään
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
			System.out.println("Epäonnistui pahasti");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Ottaa oliovirrasta int-tyyppisen arvon ja palauttaa sen
	 * @return tarvittavien säikeiden määrän
	 */
	private int selvitaTarvittavatPortit(){
		try {
			return dvs.readInt(); //Luetaan oliovirrasta int-tyyppinen arvo
		} catch (IOException e) {
			e.printStackTrace();
			return -1; //Ei saatu mitään
		}	
	}
	
	/**
	 * Käynnistää summauspalvelu-säikeet
	 */
	private void kaynnista(){
		if(verbose) System.out.println("Käynnistetään summauspalvelimet...");
		for (Thread summauspalvelu : summauspalvelimet) {
			System.out.println("Käynnistetään");
			summauspalvelu.start();
			System.out.println("Käynnistetty");
		}
		if(verbose) System.out.println("Summauspalvelimet käynnistetty");
	}

	/**
	 * Lähettää portit joita käytetään
	 * @param maara
	 */
	private void lahetaPortit(int maara){
		if(verbose)System.out.println("Lähetetään palvelimelle tiedot");
		for(int i=1;i <= maara;i++){
			try {
				dus.writeInt(KUUNNELTAVAPORTTI+1);
				dus.flush();
				odota(50); //Ei kai pakolline
			} catch (IOException e) {
				System.out.println("Portin lähettäminen epäonnistui");
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
				System.out.println("Ei voitu liittyä threadiin. Onko Thread pysähtynyt jo?");
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
	 * Metodi, jonka avulla voidaan antaa sovelluksen levätä hetki
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