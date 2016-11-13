import java.util.ArrayList;

public class Moottori{

	public static final int KUUNNELTAVAPORTTI = 20050;
	public static ArrayList<Summauspalvelu> sp;
	
	/**
	 * Harjoitustyössä yhdistettävä portti on 3126, poista myöhemmässä vaiheessa
	 */
	public static void main(String[] args){
		if(args.length < 2){
			System.err.println("Annettiin "+args.length+" parametria vaikka tarvitaan kaksi.\n"
					+ "Syötä parametrit muotoa:\n[Osoite] [Portti]");
			System.exit(0);
		}
		System.out.println("Käynnistetään summauspalvelua..");
		sp = new ArrayList<>();
		lahetaUDP(args[0], Integer.parseInt(args[1]));
	}
	
	public static void lahetaUDP(String osoite, int portti){
		System.out.println("Lähetetään UDP-pakettiä osoitteeseen "+osoite+":"+portti);
		System.out.println("Nyt pitäisi odottaa 5 s yhteyden muodostumista\nJos yhteyttä ei muodosteta niin paketti lähetetään uudestaan");
		
	}
	
	public static void luoSummauspalvelut(){
		
	}
}