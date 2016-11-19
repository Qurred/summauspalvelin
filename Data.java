
public class Data{
	
	private int summa;
	private int suurinSae;
	private int suurinSumma;
	private int lukumaara;
	
	public Data(){
		this.summa = 0;
		this.suurinSumma = 0;
		this.suurinSae = 0;
		this.lukumaara = 0;
	}
	
	public int annaSumma(){
		return this.summa;
	}
	public int annaLukuMaara(){
		return this.lukumaara;
	}
	public int annaSuurinSae(){
		return this.suurinSae;
	}
	
	public void lisaaSumma(int maara){
		this.summa+=maara;
	}
	public void korotaMaaraa(){
		this.lukumaara++;
	}
	public void tarkistaSuurin(int summa, int saeNumero){
		if(summa > suurinSumma){
			this.suurinSumma=summa;
			this.suurinSae = saeNumero;
		}
	}
	
}
