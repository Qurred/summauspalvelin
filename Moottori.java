import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Moottori{

	private static final int KUUNNELTAVAPORTTI = 20050;
	private static final int ODOTUSAIKA = 5000; //5s
	private static ArrayList<Thread> summauspalvelimet;
	private static ServerSocket soketti;
	private static Socket asiakas;
	private static ObjectInputStream dvs;
	private static ObjectOutputStream dus;
	private static DatagramSocket dataSoketti;
	private static boolean verbose = false;
	
	/**
	 * Harjoitustyˆss‰ yhdistett‰v‰ portti on 3126, poista myˆhemm‰ss‰ vaiheessa
	 */
	public static void main(String[] args){
		if(args.length < 2){
			System.err.println("Annettiin "+args.length+" parametria vaikka tarvitaan kaksi.\n"
					+ "Syˆt‰ parametrit muotoa:\n[Osoite] [Portti]");
			System.exit(0);
		}
		if(args.length > 2 && args[2].equals("verbose")){
			verbose = true;
		}
		if(verbose){
			System.out.println("K‰ynnistet‰‰n summauspalvelua..");
		}
		summauspalvelimet = new ArrayList<>();
		
		lahetaUDP(args[0], Integer.parseInt(args[1]), Integer.toString(KUUNNELTAVAPORTTI));
		
		valiaikainenNimi();
		if(verbose){
			System.out.println("Sammutetaan sovellus");
		}
	}
	/**
	 * L‰hett‰‰ parametrina annettuun osoitteeseen paketin joka sis‰lt‰‰
	 * @param osoite
	 * @param kohdePortti
	 * @param viesti
	 */
	public static void lahetaUDP(String osoite, int kohdePortti, String viesti){
		if(verbose){
			System.out.println("L‰hetet‰‰n UDP-paketti‰ osoitteeseen "+osoite+":"+kohdePortti + " viesti‰: \"" + viesti+"\"");
		}
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
			System.exit(0);
		}
		if(verbose){
			System.out.println("Paketti l‰hetetty onnistuneesti");
		}
	}
	
	public static void valiaikainenNimi(){
		try {
			if(verbose){
				System.out.println("Luodaan serverisoketti kuuntelemaan porttia " + KUUNNELTAVAPORTTI);
			}
			soketti = new ServerSocket(KUUNNELTAVAPORTTI);
			soketti.setSoTimeout(ODOTUSAIKA);
			asiakas = soketti.accept();
			if(verbose){
				System.out.println("Saatiin yhteys");
			}
			//Otetaan objektitietovirrat
			dvs = new ObjectInputStream(asiakas.getInputStream());
			dus = new ObjectOutputStream(asiakas.getOutputStream());
			//System.out.println(dvs.readUTF());
			int tarvittavaMaara = dvs.readInt();
			if(verbose){
				System.out.println("Pyydettiin " + tarvittavaMaara+" summauspalvelua");
			}
			sleep(1000);
			lahetaPortit(tarvittavaMaara);
			sleep(1000);
			//Luodaan summauspalvelut ja annetaan niille parametreina portit
			luoSummauspalvelut(tarvittavaMaara);
			//Nyt kun threadit on luotu niin k‰ynnistet‰‰n ne
			for (Thread summauspalvelu : summauspalvelimet) {
				summauspalvelu.run();
			}
			if(verbose){
				System.out.println("K‰ynnistetty summauspalvelimet");
			}
			odotaSummauspalveluita();
			
		} catch(SocketTimeoutException e){
			System.out.println("Socket timeout");
			
		}
		catch (IOException e) {
			System.out.println("Ep‰onnistui pahasti");
			e.printStackTrace();
		}
	}
	/**
	 * L‰hett‰‰ portit joita k‰ytet‰‰n
	 * @param maara
	 */
	public static void lahetaPortit(int maara){
		if(verbose){
			System.out.println("L‰hetet‰‰n palvelimelle tiedot");
		}
		for(int i=1;i <= maara;i++){
			try {
				dus.writeInt(KUUNNELTAVAPORTTI+1);
				dus.flush();
				sleep(50); //Ei kai pakolline
			} catch (IOException e) {
				System.out.println("Portin l‰hett‰minen ep‰onnistui");
				e.printStackTrace();
			}
		}
		
	}
	
	public static void luoSummauspalvelut(int maara){
		if(verbose){
			System.out.println("Aloitetaan luomaan summauspalvelimia...");
		}
		for(int i = 0; i < maara; i++){
			if(verbose){
				System.out.println("Luodaan summauspalvelin kuuntelemaan porttia " + (KUUNNELTAVAPORTTI + i + 1));
			}
			summauspalvelimet.add(new Thread(new Summauspalvelu(KUUNNELTAVAPORTTI + i + 1)));
		}
	}
	
	public static void odotaSummauspalveluita(){
		for(Thread summauspalvelu : summauspalvelimet){
			try {
				summauspalvelu.join();
			} catch (InterruptedException e) {
				System.out.println("Ei voitu liitty‰ threadiin. Onko Thread pys‰htynyt jo?");
				e.printStackTrace();
			}
		}
	}
	
	public static void sleep(int i){
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}