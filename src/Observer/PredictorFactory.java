package Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import TacTex.ChargeMwhPair;
import TacTex.PriceMwhPair;
import configure.Configure;

public class PredictorFactory {
	public double bidEpsilon = 0.001; // 0.1 cent to be above clearing price
	public double MIN_MWH = 0.001;
	public DPCache dpCache2013;
	public TreeMap<Integer,ArrayList<PriceMwhPair>> supportingBidGroups;
	public ArrayList<ChargeMwhPair> shortBalanceTransactionsData;
	Observer ob;
	public ArrayList<Integer> hourAheadAuctions;
	public PredictorFactory(Observer ob) {
		this.ob = ob;
		this.dpCache2013 = new DPCache();
		this.shortBalanceTransactionsData = new ArrayList<ChargeMwhPair>();
		this.supportingBidGroups = new TreeMap<Integer, ArrayList<PriceMwhPair>>();
		this.hourAheadAuctions = new ArrayList<Integer>();
	}
	
	public double getPrice(int hourAhead) {
		if(Configure.getUSE_DP_PREDICTOR()) {
			if (shortBalanceTransactionsData.size() < 1)
				return 30.0;
			else {
				if(dpCache2013.isValid(ob.currentTimeSlot)) {
					return Math.abs(dpCache2013.stateValues.get(ob.hourAhead+1));
				}
				return 60.0;//(meanOfBalancingPrices(shortBalanceTransactionsData));
			}
		}
		else {
			// set values from REPTREE predictor
			double [] param = new double[15];
			param = ob.getFeatures(param, hourAhead);

			//param[2] = hourAhead;
			double lprice = ob.REPTreePricePredictor.getLimitPrice(param);
			return lprice;
		}
	}
	
	public boolean canRunDP() {//int currentTimeslotIndex, List<Integer> enabledTimeslots) {
		
		if(Configure.getUSE_DP_PREDICTOR()==false)
			return true;
		// check 24 points in each of the previous slots 1..i
		int largeEnoughSample = 2;
		//System.out.println("supportingBidGroups "+supportingBidGroups.size());
		// should normally be 24
		if (supportingBidGroups.size() < Configure.getTOTAL_HOUR_AHEAD_AUCTIONS())//enabledTimeslots.size()) 
			return false;

		// these are considered 'step 0' meaning letting the imbalance
		// be resolved by the DU
		//System.out.println("shortBalanceTransactionsData "+shortBalanceTransactionsData.size());
		if (shortBalanceTransactionsData.size() < largeEnoughSample)
			return false;

		//int nextTradeCreationTimeslot = currentTimeslotIndex + 1;
		//for (Integer timeslot : enabledTimeslots) {  
		for(int i = 1; i <= Configure.getTOTAL_HOUR_AHEAD_AUCTIONS(); i++) {
			int index = i;//computeBidGroupIndex(nextTradeCreationTimeslot, timeslot.intValue());  
			//System.out.println("supportingBidGroups "+supportingBidGroups.size() + " index " + index + "supportingBidGroups.get(index).size()" + supportingBidGroups.get(index).size());
			if (supportingBidGroups.get(index).size() < largeEnoughSample) {
				//System.out.println("supportingBidGroups "+supportingBidGroups.size() + " index " + index + "supportingBidGroups.get(index).size()" + supportingBidGroups.get(index).size());
				return false;
			}
		}
		//System.out.println("Yes");
		return true;
	}
	

	public double meanOfBalancingPrices(ArrayList<ChargeMwhPair> balancingTxData) {

		int N = balancingTxData.size();
		if (N == 0) {
			//log.error("shouldn't happen: meanOfBalancingPrices() should not be called with an empty ArrayList");
			return 350*2;
		}

		double totalMwh = 0;
		double totalCharge = 0;
		for (ChargeMwhPair c : balancingTxData) {
			totalCharge += c.getCharge();
			totalMwh += Math.abs(c.getMwh());
		}
		// shouldn't happen
		if (0 == totalMwh) {
			//log.error("how come totalMwh in balancing is 0");
			return 0;
		}
		// normal case
		return (totalCharge / totalMwh);
	}
	
	/**
	 * @param neededMwh
	 * @param currentTimeslot
	 */
	public void runDP2013(double neededMwh, int currentTimeslot) {
		//System.out.println("******Enter DP2013");
		dpCache2013.clear();

		boolean isBuying = neededMwh > 0.0;
		// remember that the "latest" auction is 1
		// and the earliest is 24
		if ( ! isBuying ) {
			System.out.println("asks are not supported yet! behavior undefined...");
		}

		ArrayList<Double> stateValues = dpCache2013.getStateValues();
		ArrayList<Double> bestActions = dpCache2013.getBestActions();

		// step-0 value: any amount that was not purchased is balanced
		double valueOfStep0 = -meanOfBalancingPrices(shortBalanceTransactionsData);
		// if buys too well at start => underestimates balancing-costs,
		// so we add protection in the first week until there is enough 
		// data.
		//	    if (configuratorFactoryService.isUseStairBidExplore() && currentTimeslot < 360 + 168) { 
		//	      // only in the first week, protect against too low prices:
		//	      // the higher of 2xAvg-Mkt and Avg-shortBalancing 
		//	      double oldValueOfStep0 = valueOfStep0;
		//	      double avgMktPrice = Math.abs(getMeanMarketPricePerMWH());
		//	      valueOfStep0 = Math.min(-2 * avgMktPrice, valueOfStep0); 
		//	    }
		//	    
		// seed the DP algorithm
		stateValues.add(valueOfStep0); 
		bestActions.add(null); // actually, not Market order, but noop => balancing
		if(Configure.getUSE_DP_PREDICTOR()) {
			// log.info(" dp balancing estimation: " + nextStateValue);
			// DP back sweep
			//System.out.println(supportingBidGroups.size());
			for (int index = 1; index <= supportingBidGroups.size(); ++index) {
				//System.out.println(supportingBidGroups.size());
				ArrayList<PriceMwhPair> currentGroup = getBidGroup(index);
				double totalEnergyInCurrentGroup = sumTotalEnergy(currentGroup);
	
				int targetTimeslot = currentTimeslot + index;
				//	      SortedSet<OrderbookOrder> 
				//	          outstandingOrders = 
				//	              getOutstandingOrdersIfExist(targetTimeslot, isBuying); 
				double lowestAskPrice = ob.lowestAskPrice != null ? ob.lowestAskPrice : 0; 
				
				// scan action values and choose the best
				double bestActionValue = stateValues.get(stateValues.size() - 1);
				double bestPrice = -0.0;
				double acumulatedEnergy = 0;
				for (PriceMwhPair c : currentGroup) {
					if (c.getPricePerMwh() < lowestAskPrice) {
						totalEnergyInCurrentGroup -= c.getMwh();
					}
					else {
						acumulatedEnergy += c.getMwh();
						double Psuccess = acumulatedEnergy / totalEnergyInCurrentGroup;
						double Pfail = 1 - Psuccess;
						double bidPrice = -c.getPricePerMwh(); // trades are positive, bids are negative
						double nextStateValue = stateValues.get(stateValues.size() - 1);
						double actionValue = Psuccess * bidPrice + Pfail * nextStateValue; 
						if (actionValue > bestActionValue) { 
							bestActionValue = actionValue;
							bestPrice = bidPrice; 
						}        
					}
				}
				stateValues.add(bestActionValue);
				bestActions.add(bestPrice);
				//System.out.println("******loop" + index);
				
			}
		}
		else {
			// set values from REPTREE predictor
			double [] param = new double[15];
	    	
	    	for(int j = 0; j < Configure.getTOTAL_HOUR_AHEAD_AUCTIONS(); j++){
	    		param = ob.getFeatures(param,j);
		    	double lprice = ob.REPTreePricePredictor.getLimitPrice(param);
	    		stateValues.add(-lprice);
	    		bestActions.add(-lprice);
	    	}
		}
		dpCache2013.setValid(currentTimeslot);
		//System.out.println("******Exit DP2013");
		
	}

	private ArrayList<PriceMwhPair> getBidGroup(int bidGroupIndex) {
		ArrayList<PriceMwhPair> group = supportingBidGroups.get(bidGroupIndex);
		if (null == group) {
			group = new ArrayList<PriceMwhPair>();
			supportingBidGroups.put(bidGroupIndex, group);
		}
		return group;
	}
	
	private double sumTotalEnergy(ArrayList<PriceMwhPair> currentGroup) {
		double totalEnergyInCurrentGroup = 0;
		for (PriceMwhPair c : currentGroup) {
			totalEnergyInCurrentGroup += c.getMwh();
		}
		return totalEnergyInCurrentGroup;
	}
	
	public void recordTradeResult(int tradeCreationTimeslot, int timeslot, double price, double mwh) {
		int index = computeBidGroupIndex(tradeCreationTimeslot, timeslot);
		double bidPrice = price; // note recording a positive number, while bids are negative
		
		addTradeToGroup(index, bidPrice, mwh);
		if(!hourAheadAuctions.contains(index))
			hourAheadAuctions.add(index);
		//		    log.info(" tg [" + index + "]" + tradeCreationTimeslot + "=>" + timeslot + " p " + price + " mwh " + mwh);
	}
	
	/**
	 * Compute an trade index, between 1 to 24 (for n+1,...,n+24)
	 * Note: the auction, and therfore the trade creation, take place
	 * in the timeslot following the timeslot during which bids are 
	 * submitted.
	 * 
	 * @param tradeCreationTimeslot
	 * @param timeslot
	 * @return
	 */
	public int computeBidGroupIndex(int tradeCreationTimeslot, int timeslot) {    
		//	    int bidsSubmisionTimeslot = tradeCreationTimeslot - 1;
		//	    return timeslot - bidsSubmisionTimeslot;
		return ob.hourAhead+1;
	}

	
	public void addTradeToGroup(int index, double bidPrice, double mwh) {
		ArrayList<PriceMwhPair> bidGroup = getBidGroup(index);
		PriceMwhPair trade = new PriceMwhPair(bidPrice, mwh);
		// if trade exists in group, merge
		boolean exists = false;
		for (PriceMwhPair item : bidGroup) {
			if (item.getPricePerMwh() == trade.getPricePerMwh()) {
				item.addMwh(trade.getMwh());
				exists = true;
				break;
			}
		} 
		// otherwise, add sorted
		if ( ! exists ) {
			insertToSortedArrayList(bidGroup, trade);
		}
	}

	public <T> void insertToSortedArrayList(ArrayList<T> list, T value) {
		list.add(value);
		Comparable<T> cmp = (Comparable<T>) value;
		for (int i = list.size()-1; i > 0 && cmp.compareTo(list.get(i-1)) < 0; i--)
			Collections.swap(list, i, i-1);
	}
	/**
	 * holds the result of DP for current timeslot
	 */
	public class DPCache {
		public HashMap<Integer, Boolean> validTimeslots;
	
		public ArrayList<Double> stateValues;
	
		public ArrayList<Double> bestActions;
	
		public DPCache() {
			validTimeslots = new HashMap<Integer, Boolean>();
			stateValues = new ArrayList<Double>();
			bestActions = new ArrayList<Double>();
		}
	
		public void clear() {
			validTimeslots.clear();
			stateValues.clear();
			bestActions.clear();
		}
	
		public boolean isValid(int timeslot) {
			return getValidEntryFromMap(timeslot);
		}
	
		public void setValid(int timeslot) {
			validTimeslots.put(timeslot, true);      
		}
	
	
		public Double getBestAction(int bidGroupIndex) {
			return getBestActions().get(bidGroupIndex);
		}
	
		public double getBestActionWithMargin(int bidGroupIndex) {
			return getBestAction(bidGroupIndex) - bidEpsilon; // '-' is correct for both bid/ask
		}
	
		public ArrayList<Double> getBestActions() {
			return bestActions;
		}
	
		public ArrayList<Double> getStateValues() {
			return stateValues;      
		}
	
	
		private boolean getValidEntryFromMap(int timeslot) {
			Boolean valid = validTimeslots.get(timeslot);
			if (null == valid) {
				valid = false;
				validTimeslots.put(timeslot, valid);
			}
			return valid;      
		}
	}
}