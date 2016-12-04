import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Summauspalvelu extends Thread{

	private static final int MAKSIMIODOTUSAIKA = 5000; //5s
	private int PORTTI;
	private ServerSocket ss;
	private Socket asiakas;
	private Data data;
	private int luku; //Ehkä voi poistaa
	private /*volatile*/ boolean paalla = true;


	public Summauspalvelu(int portti, Data data){
			super();
			this.PORTTI = portti;
			this.data = data;
			luku = 1;
	}

	public void run() {
		try {
			ss = new ServerSocket(this.PORTTI);	
			asiakas = ss.accept();
			
			InputStream iS = asiakas.getInputStream();
			ObjectInputStream oiS = new ObjectInputStream(iS);
			
			asiakas.setSoTimeout(MAKSIMIODOTUSAIKA);

			while (paalla){
				try {
					luku = oiS.readInt();
					if (luku == 0){
						break;
					}
					data.lisaaLuku(luku, PORTTI);
				}catch(EOFException e){
					break;
				}
				catch (IOException e) {
					break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sulje(){
		paalla = false;
		try {
			asiakas.close();
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
