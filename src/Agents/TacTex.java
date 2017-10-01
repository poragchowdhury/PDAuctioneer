package Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;
import TacTex.ChargeMwhPair;
import Observer.PricePredictor;
import TacTex.PriceMwhPair;
import configure.Configure;

public class TacTex extends Agent {
	public PricePredictor pricePredictor;
	Observer ob;
	ArrayList<Ask> asks;
	ArrayList<Bid> bids;
	private double MIN_MWH = 0.001;
	private double bidEpsilon = 0.001; // 0.1 cent to be above clearing price

	public TacTex(String name, int id, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		this.type = agentType.BROKER;
		pricePredictor = new PricePredictor(Agent.predictorName);
	}

	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob)
	{
		double neededMWh = this.neededMWh;
		this.ob = ob;
		this.asks = asks;
		this.bids = bids;
		if (neededMWh > 0) {
			// heuristic reduction, to reduce potential surplus that needs to be
			// resold
			int timeToEnd = ob.hourAhead+1; //targetTimeslot - currentTimeslotIndex; 
			if (timeToEnd > 6) 
				neededMWh *= 0.8; 
			//			List<Integer> enabledTimeslots = new ArrayList<Integer>();
			//			for(int i = 0; i < Configure.getTOTAL_HOUR_AHEAD_AUCTIONS(); i++)
			//				enabledTimeslots.add(i+ob.currentTimeSlot);

			if (ob.pricepredictor.canRunDP()){ //ob.currentTimeSlot, enabledTimeslots)) {
				//System.out.println("Can Run DP!");
				dpBasedLimit2013(ob.currentTimeSlot+ob.hourAhead+1, neededMWh, ob.currentTimeSlot);      
			}
			else {
				//System.out.println(ob.getTime());
				//explorationNullBalancingBidding(ob.currentTimeSlot+ob.hourAhead+1, ob.currentTimeSlot, this.neededMWh);
				explorationStairsBidding(ob.currentTimeSlot+ob.hourAhead+1, ob.currentTimeSlot, neededMWh);
			}

		}
		else {
			balancingLimitsSellBidding();
		}
	}

	private void balancingLimitsSellBidding() {

		if (this.neededMWh > 0) {
			// shouldn't happen
			return; // empty list
		}

		// typically should be negative:
		double buyBalancePrice = ob.pricepredictor.meanOfBalancingPrices(ob.pricepredictor.shortBalanceTransactionsData);
		// typically should be positive:
		double sellBalancingPrice = ob.pricepredictor.meanOfBalancingPrices(ob.pricepredictor.shortBalanceTransactionsData);

		// worst I am willing to sell for is sell-balancing-price (or 0, if < 0)
		double willingToSellLimit = Math.max(0, sellBalancingPrice); 
		// best I expect opponent to pay is his balancing price, (assumed to be like mine)
		double tryingToSellLimit = buyBalancePrice; // '-' inverts from buy-price to sell-price

		if (tryingToSellLimit > willingToSellLimit) {
			int remainingTries = ob.hourAhead;
			double priceStep = (tryingToSellLimit - willingToSellLimit) / 24.0;

			// METHOD 1 (could be exploitable)
			double limit = willingToSellLimit + priceStep * remainingTries;
			Ask ask = new Ask(this.playerName, this.id, limit, this.neededMWh, this.type);
			this.asks.add(ask);

		}
		else { // better to wait for balancing
			Ask ask = new Ask(this.playerName, this.id, willingToSellLimit, this.neededMWh, this.type);
			this.asks.add(ask);
		}

		return;
	}


	private void dpBasedLimit2013(int targetTimeslot, double neededMwh, int currentTimeslotIndex) {

		if ( ! ob.pricepredictor.dpCache2013.isValid(currentTimeslotIndex)) {
			ob.pricepredictor.runDP2013(100, currentTimeslotIndex);
		}

		int tradeCreationTimeslot = currentTimeslotIndex + 1;
		int bidGroupIndex = ob.pricepredictor.computeBidGroupIndex(tradeCreationTimeslot , targetTimeslot);    

		// Create the required number of stairs
		try {
			dp2013CreateManyMktOrders(targetTimeslot, neededMwh,	bidGroupIndex);
		} catch (Exception e) {	
			dp2013CreateTwoMktOrders(targetTimeslot, neededMwh, bidGroupIndex);
		}
	}

	private void dp2013CreateManyMktOrders(int targetTimeslot,
			double neededMwh, int bidGroupIndex) {
		List<Double> limits = new ArrayList<Double>();
		// insert stairs from low to high:
		// 1. current bestActions
		// 2. next-state-value=>last-state-value
		limits.add(ob.pricepredictor.dpCache2013.getBestActionWithMargin(bidGroupIndex));
		for (int i = bidGroupIndex - 1; i >= 0; --i){
			limits.add(ob.pricepredictor.dpCache2013.getStateValues().get(i));
		}
		createManyMktOrdersFromLimits(targetTimeslot, neededMwh, limits);
		return;
	}


	private void dp2013CreateTwoMktOrders(int targetTimeslot,
			double neededMwh, int bidGroupIndex) {
		double lowerLimit = ob.pricepredictor.dpCache2013.getBestActionWithMargin(bidGroupIndex);
		double upperLimit = ob.pricepredictor.dpCache2013.getStateValues().get(bidGroupIndex - 1);
		createTwoMktOrdersFromLimits(targetTimeslot, neededMwh,
				lowerLimit, upperLimit);
	}	


	private void createTwoMktOrdersFromLimits(int targetTimeslot, double neededMwh, double lowerLimit, double upperLimit) {
		double mwhForUpperLimit = Math.max(neededMwh - MIN_MWH, MIN_MWH);
		double mwhForLowerLimit = MIN_MWH;
		Bid bid1 = new Bid(this.playerName, this.id, upperLimit, mwhForUpperLimit, this.type);
		this.bids.add(bid1);
		if(ob.DEBUG)
			System.out.println(bid1.toString());
		
		Bid bid2 = new Bid(this.playerName, this.id, lowerLimit, mwhForLowerLimit, this.type);
		this.bids.add(bid2);	   
		if(ob.DEBUG)
			System.out.println(bid2.toString());
		
	}


	private void createManyMktOrdersFromLimits(int targetTimeslot,  double neededMwh, List<Double> limits) {
		// add small stairs
		double mwhLeft = neededMwh;
		int i = 0; 
		for (    ; mwhLeft > 0 && i < limits.size() - 1; mwhLeft -= MIN_MWH, ++i) {
			double limit = limits.get(i);
			Bid bid = new Bid(this.playerName, this.id, limit, MIN_MWH, this.type);
			this.bids.add(bid);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			
		}

		// Add last, large stair if still mwh left; upperLimit is either last
		// state's value (balancing-price) or the value of the MDP state to which
		// we have reached if neededMwh is small (this is an edge case)
		double upperLimit = limits.get(i);
		if (mwhLeft >= MIN_MWH) {
			Bid bid = new Bid(this.playerName, this.id, upperLimit, mwhLeft, this.type);
			this.bids.add(bid);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			
		}
		return; 
	}

	private void explorationStairsBidding(int targetTimeslot,
			int currentTimeslotIndex, double neededMWh) {
		Random randomGen = new Random();
		boolean isBuying = neededMWh > 0; 
		if ( ! isBuying ) {
			//log.error("explorationStairsBidding assumes buying. Returning empty list of orders");
			return;
		}

		List<Double> limitPrices = new ArrayList<Double>();

		double balancingLimit = Math.abs(ob.pricepredictor.meanOfBalancingPrices(ob.pricepredictor.shortBalanceTransactionsData));
		double avgMktPrice = Math.abs(25);
		double upperBid = Math.min(2 * avgMktPrice, balancingLimit); // the higher of 2xAvg-Mkt and Avg-shortBalancing
		//log.debug("explorationStairsBidding(): balancingLimit " + balancingLimit + " avgMktPrice " + avgMktPrice + " upperBid " + upperBid);
		double lowerBid = 1.0; // -15.23859038493; // i.e. -15
		final double numBids = 18;
		final double delta = (upperBid - lowerBid) / numBids;
		for (double limit = lowerBid; limit < upperBid; limit += delta) {
			//log.debug("explorationStairsBidding() limit=" + limit);
			limitPrices.add(limit + 0.5 * delta * randomGen.nextDouble()); // add random element
		} 
		// add small bids
		for (Double limit : limitPrices) {
			//orders.add(new Order(brokerContext.getBroker(), targetTimeslot, MIN_MWH, limit));
			Bid bid = new Bid(this.playerName, this.id, limit, MIN_MWH, this.type);
			this.bids.add(bid);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			
		}
		// add balancing-based bid
		//orders.add(new Order(brokerContext.getBroker(), targetTimeslot, neededMWh, upperBid));
		Bid bid = new Bid(this.playerName, this.id, upperBid, neededMWh, this.type);
		this.bids.add(bid);
		if(ob.DEBUG)
			System.out.println(bid.toString());
		

		return;
	}
/*
	private void explorationNullBalancingBidding(int targetTimeslot,
			int currentTimeslotIndex, double neededMWh) {
		// new method: small mkt order if has time, balancing-based limit otherwise
		if (targetTimeslot == currentTimeslotIndex + 1) { 
			balancingBasedBidding(targetTimeslot, currentTimeslotIndex, neededMWh);
		}
		else { 
			//System.out.println("TacTex submits null exploration bid");
			Bid bid = new Bid(this.playerName, this.id, null, MIN_MWH, this.type);
			this.bids.add(bid);
		} 
		return;
	}

	private void balancingBasedBidding(int targetTimeslot,
			int currentTimeslotIndex, double neededMWh) {

		double limit;
		if (neededMWh > 0) { // buy
			limit = ob.pricepredictor.meanOfBalancingPrices(ob.pricepredictor.shortBalanceTransactionsData);
			//System.out.println("TacTex balancing-based bid: neededMwh=" + neededMWh + " limit=" + limit);
			Bid bid = new Bid(this.playerName, this.id, limit, this.neededMWh, this.type);
			this.bids.add(bid);
		} else { // sell

			limit = ob.pricepredictor.meanOfBalancingPrices(ob.pricepredictor.shortBalanceTransactionsData);
			//System.out.println("TacTex balancing-based ask: neededMwh=" + neededMWh + " limit=" + limit);
			Ask ask = new Ask(this.playerName, this.id, limit, this.neededMWh, this.type);
			this.asks.add(ask);
		}
		return;
	}
*/
}