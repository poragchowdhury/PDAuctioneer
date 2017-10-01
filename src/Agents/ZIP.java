package Agents;

import java.util.ArrayList;
import java.util.Random;

import configure.Configure;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;
import Observer.PricePredictor;


public class ZIP extends Agent {

	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	public double offSet = 0;
	public boolean prevBidPassed = false;
	public PricePredictor pricePredictor;
	
	public ZIP(String name, int id, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.BROKER;
		pricePredictor = new PricePredictor(Agent.predictorName);
	}
	
	@Override
	public void setFlag(boolean flag) {
		this.prevBidPassed = flag;	
	}
	
	
	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob) {
		// Bidding configuration
		
		if(this.neededMWh > 0){
			
//			double [] param = new double[11];
//			param = ob.getFeatures(param);
			double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
			double profitmargin = limitPrice*0.01;
			
			if(ob.hourAhead == Configure.getTOTAL_HOUR_AHEAD_AUCTIONS())
			{
				// reset the offSet
				offSet = 0.0; 
				prevBidPassed = false;
			}
			else{
				if(!prevBidPassed)
					offSet = offSet + limitPrice * 0.1; 
				
				limitPrice += offSet;
			}
			
			if((this.neededMWh-MIN_MWH) <= 0) {
				return;
			}
			
			Bid bid = new Bid(this.playerName, this.id, (limitPrice+profitmargin), this.neededMWh, this.type);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			bids.add(bid);
		}
	}

}