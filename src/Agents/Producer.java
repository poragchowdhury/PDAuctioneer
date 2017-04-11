package Agents;

import java.util.ArrayList;
import java.util.Random;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;


public class Producer extends Agent{
	
	public double meanBidPrice = 45;
	public double stddevPrice = 5;
	public double greenPoints = 1;
	public double [] cap;
	
	public Producer(String name, int id, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.PRODUCER;
		this.cap = new double [24];
		initCap();
	}
	
	public void initCap(){
		this.cap[10] = this.cap[9] = 0.80;
		this.cap[8] = this.cap[7] = 0.85;
		this.cap[6] = this.cap[5] = 0.90;
		this.cap[4] = this.cap[3] = 0.95;
		this.cap[2] = this.cap[1] = this.cap[0] = 1;
	}
	
	public Producer(){
		this.playerName = "MonopolyProducer";
		this.greenPoint = 0;
		this.type = agentType.PRODUCER;
		this.neededMWh = 0;
		this.id = -1;
		this.cap = new double [24];
	}
	
	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob) {
		double greenpoints = 1;
		if(ob.GREEN_AUCTION_FLAG)
			greenpoints = ob.arrProducerGreenPoints[this.id];
		
		double price = ob.arrProducerBidPrice[this.id][ob.currentTimeSlot]/greenpoints;
		
		double bandwidth = this.neededMWh * this.cap[ob.hourAhead];
		
		Ask ask = new Ask(this.playerName, this.id, price, bandwidth, this.type);
		if(ob.DEBUG)
			System.out.println(ask.toString());
		asks.add(ask);	
	}
}
