import java.util.ArrayList;

public class Moottori{

	public static final int KUUNNELTAVAPORTTI = 20050;
	public static ArrayList<Summauspalvelu> sp;
	
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
		sp = new ArrayList<>();
		lahetaUDP(args[0], Integer.parseInt(args[1]));
	}
	
	public static void lahetaUDP(String osoite, int portti){
		System.out.println("L�hetet��n UDP-paketti� osoitteeseen "+osoite+":"+portti);
		System.out.println("Nyt pit�isi odottaa 5 s yhteyden muodostumista\nJos yhteytt� ei muodosteta niin paketti l�hetet��n uudestaan");
		
	}
	
	public static void luoSummauspalvelut(){
		
	}
}