import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//Väliaikainen nimi, voidaan mahdollisesti hyödyntää informaation tallentamisessa
public class Data{
	
	private int summa;
	private int suurin;
	private int lukumaara;
	
	public Data(){
		this.summa = 0;
		this.suurin = 0;
		this.lukumaara = 0;
	}
	
	public int annaSumma(){
		return this.summa;
	}
	public int annaLukuMaara(){
		return this.lukumaara;
	}
	public int annaSuurin(){
		return this.suurin;
	}
	
	public void lisaaSumma(int maara){
		this.summa+=maara;
	}
	public void korotaMaaraa(){
		this.lukumaara++;
	}
	public void tarkistaSuurin(int x){
		if(x > suurin) suurin=x;
	}
	
	
	/*public Data(){
		try {
			BufferedReader lukija = new BufferedReader(new FileReader("Asetukset.txt"));
			System.out.println("Aletaan lukea tiedostoa läpi");
			String rivi = lukija.readLine().trim();
			
			while(rivi != null){
				if(rivi.indexOf('#') == -1 || rivi.equals("")){
					System.out.println(rivi.trim());
				}
				rivi = lukija.readLine();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Tiedostoa ei löydetty... \nSuljetaan ohjelma");
			System.exit(0);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}*/
	
	
}
