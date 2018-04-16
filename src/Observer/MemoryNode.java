package Observer;

public class MemoryNode {
	public double price;
	public double profitability;
	public int nVisits;
	
	public MemoryNode(double price, double profitability, int nVisits){
		this.price = price;
		this.profitability = profitability;
		this.nVisits = nVisits;
	}
	
	public void updateMemoryNode(double profitability){
		this.profitability = ((this.profitability*this.nVisits)+profitability)/(this.nVisits+1);
		this.nVisits++;
	}
	
}
