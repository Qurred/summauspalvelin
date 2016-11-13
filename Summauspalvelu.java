import java.io.IOException;
import java.net.ServerSocket;

public class Summauspalvelu implements Runnable{

	private static final int MAKSIMIODOTUSAIKA = 5000; //5s
	private int summa;
	private int lukumaara;
	private ServerSocket ss;
	
	public Summauspalvelu(int portti){
		try {
			this.ss = new ServerSocket(portti);
			this.ss.setSoTimeout(MAKSIMIODOTUSAIKA);
			this.summa = 0;
			this.lukumaara = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		// Jaa'a		
	}
	
	

}