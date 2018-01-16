package MCTS_Kernel;

import java.util.Random;

public class Action {
	public int actionName;
	public double minMult;
	public double maxMult;
	public boolean nobid;
	public ACTION_TYPE type;
	public double percentage;
	public boolean dynamicAction;
	
	public static enum ACTION_TYPE{
		BUY,
		SELL,
		NO_BID
	}
	
	public Action(){
		
	}
	
	public Action(int actionName, double minMult, double maxMult, boolean noBid, ACTION_TYPE type, double percentage, boolean dynamicAction){
		this.actionName = actionName;
		this.minMult = minMult;
		this.maxMult = maxMult;
		this.nobid = noBid;
		this.type = type;
		this.percentage = percentage;
		this.dynamicAction = dynamicAction;
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
