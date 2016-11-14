import java.io.IOException;
import java.net.ServerSocket;

public class Summauspalvelu implements Runnable{

	private static final int MAKSIMIODOTUSAIKA = 5000; //5s
	private static int summa;
	private static int lukumaara;
	private static int suurin;
	private ServerSocket ss;
	
	public Summauspalvelu(int portti){
		try {
			this.ss = new ServerSocket(portti);
			this.ss.setSoTimeout(MAKSIMIODOTUSAIKA);
			summa = 0;
			lukumaara = 0;
			suurin = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public synchronized int suurinSumma(int luku){
		if (luku < suurin){
			return luku;
		}
			else{
				return suurin;
		}
    }
	public synchronized int lukujenMaara(){
		return lukumaara +1;
	}
	public synchronized int lukujenSumma(int luku){
		return summa + luku;
	}
	
	public void run() {
		// Jaa'a 		
	}
	
	
	

}