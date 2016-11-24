import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Summauspalvelu extends Thread{

	private static final int MAKSIMIODOTUSAIKA = 20000; //5s
	private int PORTTI;
	private ServerSocket ss;
	private Socket asiakas;
	private Data data;
	private int luku;
	private volatile boolean paalla = true;


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
					System.out.println(PORTTI + ": " + luku);
					if (luku == 0){
						System.out.println(PORTTI + ": sai nollan");
						break;
					}
					
				}catch(EOFException e){
					System.out.println(PORTTI + ": paalla " + paalla+", Stream " + iS.available());
					
					System.out.println(PORTTI+": "+e.toString());
					break;
				}
				catch (IOException e) {
					System.out.println(PORTTI+": "+e.toString());
					break;
				}
			}
			System.out.println(PORTTI + ": Lis‰t‰‰n luku " + luku);
			data.lisaaLuku(luku, PORTTI);
		} catch (IOException e) {
			System.out.println(PORTTI+": "+e.toString());
		}


	}
	public void sulje(){
		paalla = false;
		try {
			asiakas.close();
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
