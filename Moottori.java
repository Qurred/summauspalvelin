import java.io.DataInputStream;
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
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.omg.CORBA.DataOutputStream;

public class Moottori{

	public static final int KUUNNELTAVAPORTTI = 20050;
	public static final int ODOTUSAIKA = 5000; //5s
	public static ArrayList<Summauspalvelu> summauspalvelimet;
	public static ServerSocket soketti;
	public static Socket asiakas;
	
	/**
	 * Harjoitusty�ss� yhdistett�v� portti on 3126, poista my�hemm�ss� vaiheessa
	 */
	public static void main(String[] args){
		if(args.length < 2){
			System.err.println("Annettiin "+args.length+" parametria vaikka tarvitaan kaksi.\n"
					+ "Sy�t� parametrit muotoa:\n[Osoite] [Portti]");
			System.exit(0);
		}
		System.out.println("K�ynnistet��n summauspalvelua..");
		summauspalvelimet = new ArrayList<>();
		lahetaUDP(args[0], Integer.parseInt(args[1]), Integer.toString(KUUNNELTAVAPORTTI));
		valiaikainenNimi();
		System.out.println("Sammutetaan sovellus");
	}
	/**
	 * L�hett�� parametrina annettuun osoitteeseen paketin joka sis�lt��
	 * @param osoite
	 * @param kohdePortti
	 * @param viesti
	 */
	public static void lahetaUDP(String osoite, int kohdePortti, String viesti){
		System.out.println("L�hetet��n UDP-paketti� osoitteeseen "+osoite+":"+kohdePortti + " viesti�: \"" + viesti+"\"");
		try {
			InetAddress kohdeOsoite = InetAddress.getByName(osoite);
			DatagramSocket dataSoketti = new DatagramSocket();
			byte[] lahetettavaData = Integer.toString(KUUNNELTAVAPORTTI).getBytes();
			DatagramPacket paketti = new DatagramPacket(lahetettavaData, lahetettavaData.length, kohdeOsoite, kohdePortti);
			dataSoketti.send(paketti);
		} catch (IOException e) {
			System.err.println("UDP-paketin l�hett�minen ei onnistunut...");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Paketti l�hetetty onnistuneesti");
		
	}
	
	public static void valiaikainenNimi(){
		try {
			System.out.println("Luodaan serverisoketti kuuntelemaan porttia " + KUUNNELTAVAPORTTI);
			soketti = new ServerSocket(KUUNNELTAVAPORTTI);
			soketti.setSoTimeout(ODOTUSAIKA);
			asiakas = soketti.accept();
//			asiakas.setSoTimeout(ODOTUSAIKA);
			System.out.println("Saatiin yhteys");
			//Otetaan objektitietovirrat
			ObjectInputStream dvs = new ObjectInputStream(asiakas.getInputStream());
			ObjectOutputStream dus = new ObjectOutputStream(asiakas.getOutputStream());
			//System.out.println(dvs.readUTF());
			int tarvittavaMaara = dvs.readInt();
			System.out.println("Pyydettiin " + tarvittavaMaara+" summauspalvelua");
			//Luodaan summauspalvelut ja annetaan niille parametreina portit
			for(int i = 0; i < tarvittavaMaara; i++){
				System.out.println("Luodaan summauspalvelin kuuntelemaan porttia " + (KUUNNELTAVAPORTTI + i + 1));
				summauspalvelimet.add(new Summauspalvelu(KUUNNELTAVAPORTTI + i + 1));
			}
			//Nyt kun threadit on luotu niin k�ynnistet��n ne
			for (Summauspalvelu summauspalvelu : summauspalvelimet) {
				summauspalvelu.run();
			}
			System.out.println("K�ynnistetty summauspalvelimet");
			
			for(Summauspalvelu summauspalvelu : summauspalvelimet){
				//TODO Summauspalveluiden odotus, pit�isi l�yty� THREADist�
			}
			
		} catch(SocketTimeoutException e){
			System.out.println("Socket timeout");
			
		}
		catch (IOException e) {
			System.out.println("Ep�onnistui pahasti");
			e.printStackTrace();
		}
	}
	
	public static void luoSummauspalvelut(){
		
	}
}