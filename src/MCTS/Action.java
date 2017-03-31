package MCTS;

import java.util.Random;

public class Action {
	public String actionName;
	public double minMult;
	public double maxMult;
	public boolean nobid;
	public ACTION_TYPE type;
	public double percentage;
	public static enum ACTION_TYPE{
		BUY,
		SELL,
		NO_BID
	}
	
	public Action(){
		
	}
	
	public Action(String actionName, double minMult, double maxMult, boolean noBid, ACTION_TYPE type, double percentage){
		this.actionName = actionName;
		this.minMult = minMult;
		this.maxMult = maxMult;
		this.nobid = noBid;
		this.type = type;
		this.percentage = percentage;
	}
	
	public double [] getAdjustedPrice(double meanPrice, double stddev){
		double minPrice = meanPrice + ((this.minMult * stddev));
		double maxPrice = meanPrice + ((this.maxMult * stddev));
		return new double [] {minPrice, maxPrice};
	}
	
	public String toString(){
		return "[action:" + this.actionName + " minMult: " + this.minMult+" maxMult: " + this.maxMult +" nobid : " + this.nobid +"]";
	}
}
