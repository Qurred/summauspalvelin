import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Summauspalvelu implements Runnable{

	private static final int MAKSIMIODOTUSAIKA = 20000; //5s
	private static int luku;
	private static int summa;
	private ServerSocket ss;
	private Socket asiakas;
	private ObjectInputStream ois;
	private ObjectOutputStream ous;
	private OutputStream oS;
	private InputStream iS;
	boolean kaynnissa;
	ArrayList<Integer> lista;

	public Summauspalvelu(int portti){
		try {
			this.ss = new ServerSocket(portti);
			this.ss.setSoTimeout(MAKSIMIODOTUSAIKA);
			this.asiakas = new Socket();
			summa = 0;
			luku = 0;
			kaynnissa = true;
			lista = new ArrayList<Integer>();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public synchronized int suurinSumma(ArrayList<Integer> suurin){
		for (int i = 0; i < suurin.size(); i++){
			if (suurin.get(i) > luku){
				luku = suurin.get(i);
			}
		}
		return luku;
	}

	public synchronized int lukujenMaara(ArrayList<Integer> maara){
		return maara.size();
	}
	public synchronized int lukujenSumma(ArrayList<Integer> summat){
		for (int i = 0; i < summat.size(); i++){
			summa = summa + summat.get(i);
		}
		return summa;
	}

	public void run() {

		try {
			asiakas = ss.accept();
			iS = asiakas.getInputStream();
			oS = asiakas.getOutputStream();
			ous = new ObjectOutputStream(oS);
			ois = new ObjectInputStream(iS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (kaynnissa){

			try {
				int luku = ois.readInt(); //<- aiheuttaa virheen, l‰hde on tuntematon. Ehkei Vastapuoli kerke‰ tekem‰‰n mit‰‰n?
				if (luku == 0){
					kaynnissa = false;
					break;
				}
				lista.add(luku);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				asiakas.close(); //<- Miksi asiakas-soketti suljetaan whileloopin sis‰ll‰? Eikˆ t‰m‰ aiheuta kaikkien muiden yhteyksien menetyksen?
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

}
