package Auctioneer;

import Agents.Agent;

public class Bid implements Comparable {
	public String agentName;
	public int agentID;
	public double price;
	public double amount;
	public Agent.agentType agentType;
	
	public Bid(){
		this.agentName = "Default";
		this.agentID = -1;
		this.price = 0;
		this.amount = 0;
	}
	
	public Bid(String name, int id, double price, double amount, Agent.agentType agentType){
		this.agentName = name;
		this.agentID = id;
		this.price = price;
		this.amount = amount;
		this.agentType = agentType;
	}
	
	@Override
	public int compareTo(Object o) {
		if(this.price > ((Bid)o).price)
			return -1;
		else if(this.price < ((Bid)o).price)
			return 1;
		else 
			return 0;
	}
	
	@Override
    public String toString() {
        return "[ agentName=" + this.agentName + ", id=" + agentID + ", price=" + price + ", amount=" + amount + "]";
    }
}
