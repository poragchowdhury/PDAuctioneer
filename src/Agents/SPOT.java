package Agents;

import java.util.ArrayList;
import java.util.Random;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;
import Observer.PricePredictor;


public class SPOT extends Agent {

	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	public PricePredictor pricePredictor;
	
	public SPOT(String name, int id, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.BROKER;
		pricePredictor = new PricePredictor(Agent.predictorName);
	}
	
	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob) {
		
		if(this.neededMWh > 0){
			
			double [] param = new double[15];
			param = ob.getFeatures(param,ob.hourAhead);
			double limitPrice = pricePredictor.getLimitPrice(param);
			
			// Bidding configuration
			double numberofbids = 10;
			double unitPriceIncrement = 1.00;
			
			
			double maxPrice = limitPrice + 2*2;
			double minPrice = limitPrice + 2*(-2);
			double priceRange = maxPrice - minPrice;
			limitPrice = minPrice;
			double minMWh = 1;
	
			minMWh= Math.abs(this.neededMWh) / numberofbids;
			unitPriceIncrement = priceRange / numberofbids;
			
			for(int i = 1; i <=numberofbids; i++){
				Bid bid = new Bid(this.playerName,  this.id, limitPrice, minMWh, this.type);
				if(ob.DEBUG)
					System.out.println(bid.toString());
				bids.add(bid);
				limitPrice+=unitPriceIncrement;
			}
		}
	}
}