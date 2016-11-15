import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Summauspalvelu implements Runnable{

	private static final int MAKSIMIODOTUSAIKA = 20000; //5s
	private static int summa;
	private static int lukumaara;
	private static int suurin;
	private ServerSocket ss;
	private Socket asiakas;
	private ObjectInputStream ois;
	private ObjectOutputStream ous;
	
	public Summauspalvelu(int portti){
		try {
			this.ss = new ServerSocket(portti);
			this.ss.setSoTimeout(MAKSIMIODOTUSAIKA);
			this.asiakas = new Socket();
			summa = 0;
			lukumaara = 0;
			suurin = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public synchronized int suurinSumma(int luku){
		if (luku < suurin){
			suurin = luku;
			 return luku;
		}
			else{
			 return suurin;
		}
    }
	public synchronized int lukujenMaara(){
		lukumaara = lukumaara++;
		return lukumaara;
	}
	public synchronized int lukujenSumma(int luku){
		summa = summa + luku;
		return summa;
	}
	
	public void run() {

		try {
			asiakas = ss.accept();
			this.ois = new ObjectInputStream(asiakas.getInputStream());
			this.ous = new ObjectOutputStream(asiakas.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			asiakas.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Kuunnellaan ServerSokettia ja hyv‰ksyt‰‰n Soketiksi ServerSokettiin yritett‰v‰ yhteys
		// K‰ytet‰‰n esim while-looppia et niin kaua ku jokin o totta -> totuusarvo muuttuu ku saadaan nolla
			//asetetaan luku muuttujaan readInt objectinputista
			//K‰yt‰‰n arvolla m‰‰ritellyt metodit l‰pi
			//jne...
	

		
		
	}
	
	}
