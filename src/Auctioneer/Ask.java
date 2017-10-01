package Auctioneer;

import Agents.Agent;


public class Ask implements Comparable  {

	public String agentName;
	public int agentID;
	public Double price;
	public double amount;
	public Agent.agentType agentType;
	
	public Ask(){
		this.agentName = "Default";
		this.agentID = 0;
		this.price = Double.MIN_VALUE;
		this.amount = 0;
	}
	
	public Ask(String name, int id, Double price, double amount, Agent.agentType agentType){
		this.agentName = name;
		this.agentID = id;
		if(price == null)
			this.price = price;
		else
			this.price = Math.abs(price);
		this.amount = Math.abs(amount);
		this.agentType = agentType;
	}
	
	@Override
	public int compareTo(Object o) {
		if(((Ask)o).price == null) {
			if(((Ask)this).price == null)
				return -1;
			else
				return 1;
		}
		else {
			if(((Ask)this).price == null)
				return -1;
		}
		
		if(this.price > ((Ask)o).price)
			return 1;
		else if(this.price < ((Ask)o).price)
			return -1;
		else 
			return 0;
	}
	
	@Override
    public String toString() {
        return "[ agentName=" + this.agentName + ", id=" + agentID + ", price=" + price + ", amount=" + amount + "]";
    }
}
