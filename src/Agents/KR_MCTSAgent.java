package Agents;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import configure.Configure;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import MCTS_Kernel.Action.ACTION_TYPE;
import MCTS_Kernel.TreeNode;
import MCTS_Kernel.MCTS_Kernel;
import Observer.Observer;
import Observer.PricePredictor;

public class KR_MCTSAgent extends Agent {

	public MCTS_Kernel mcts;
	public boolean firstTimeFlags =  true;
	public double remainingNeededMWh;
	public double offSet = 0;
	public boolean prevBidPassed = false;

	public KR_MCTSAgent(String name, int id, double neededMWh, double mean, double stddev, double mctsSim){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		this.type = agentType.BROKER;
		this.mcts = new MCTS_Kernel(mctsSim, name);
	}

	public MCTS_Kernel getMCTS(){
		return this.mcts;
	}


	
	@Override
	public void setFlag(boolean flag) {
		this.prevBidPassed = flag;	
	}
	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer observer) {

		////System.out.println("\nSPOT2 neededMWh " + neededMWh);
		if(firstTimeFlags){
			mcts.setup(observer);
			firstTimeFlags = false;
		}
		//long start = System.currentTimeMillis();
		observer.neededEneryMCTSBroker = this.neededMWh; 
		observer.initialNeededEneryMCTSBroker = this.initialNeededMWh;
		if(this.neededMWh <= 0.001)
			return;
		
		TreeNode bestMove = mcts.getBestMCTSMove(observer);
		// Clear the actionspace
		System.out.println("Action size " + mcts.actions.get(0).size());
		mcts.actions.get(0).clear();
		mcts.thresholdcount=0;
		
		if(bestMove == null)
			System.out.println("BestMove is null");
		//else 
		//	System.out.println("BestMove " + bestMove.actionName);;
		
		//double minMWh = bestMove.demand / bestMove.iteration;
		//long end = System.currentTimeMillis();
		//NumberFormat formatter = new DecimalFormat("#0.00000");
		//String totalTime =  formatter.format((end - start) / 1000d); 
		//double dTotalTime =  Double.parseDouble(totalTime);
		////System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
		//if(dTotalTime > 0){
		//	observer.nanoTime += dTotalTime;
		//	observer.nanoTimeCount++;
		//}
		//System.out.println(totalTime);

		//double limitPrice = bestMove.mctsClearingPrice;

		// Bidding configuration
		double numberofbids = 1;
		double unitPriceIncrement = 1.00;

		/*double [] param = new double[11];
	    	param = observer.getFeatures(param);
	    	double clearingPrice = mcts.pricePredictor.getLimitPrice(param);
	    	double limitPrice = clearingPrice + (bestMove.minMult*7.8); //tn.minmctsClearingPrice;
    		double maxPrice = clearingPrice + (bestMove.maxMult*7.8); //tn.minmctsClearingPrice;
    		double priceRange = maxPrice - limitPrice; // tn.maxmctsClearingPrice - tn.minmctsClearingPrice;
		 */
		//double limitPrice = bestMove.totValue;//pcp+bestMove.minMult*observer.STDDEV[observer.hourAhead]; //bestMove.minmctsClearingPrice;// 
		double limitPrice = 0;//bestMove.minMult+bestMove.minMult*bestMove.maxMult;
		if(bestMove.dynamicState)
			limitPrice = bestMove.minMult+bestMove.minMult*bestMove.maxMult;
		else {
			double pcp = observer.pricepredictor.getPrice(observer.hourAhead);
			limitPrice = pcp+bestMove.minMult*observer.STDDEV[observer.hourAhead];
		}
		//limitPrice = bestMove.totValue;
		System.out.println("Action " + bestMove.actionName + " LimitPriceMCTS " + limitPrice + " minMult " + bestMove.minMult + " maxMult " + bestMove.maxMult);
		double priceRange = 0;//bestMove.maxmctsClearingPrice - bestMove.minmctsClearingPrice;
		double minMWh = 1;


		unitPriceIncrement = priceRange / numberofbids;

//		if(playerName.equalsIgnoreCase("MCTSX"))
	//		observer.recordBestMove(bestMove);

		if(observer.hourAhead == 0) {
			// submit market order 
			// because we don't want to buy from balancing market
			if(this.neededMWh > 0) {
				Bid bid = new Bid(this.playerName, this.id, null, this.neededMWh, this.type);
				if(observer.DEBUG)
					System.out.println(bid.toString());
				bids.add(bid);
			}
			return;
		}
		
		if(!bestMove.nobid){

			observer.mctsxPredictedCostPerHour += bestMove.currentNodeCostAvg;

			observer.mctsxPredictedCost[(observer.day*Configure.getHOURS_IN_A_DAY())+observer.hour][observer.hourAhead] = bestMove.totValue;

			if(bestMove.actionType == ACTION_TYPE.BUY){
				
				//if(bestMove.volPercentage == 0.80)
				//	System.out.println("?");
				
				// Submit buy orders
				double surplus = Math.abs(this.initialNeededMWh)*(bestMove.volPercentage-1);

				double totalE = surplus + this.neededMWh;

				if((totalE-MIN_MWH) <= 0) {
					return;
				}
				if(observer.hourAhead > 6)
					totalE *= 0.8;

				//minMWh = totalE  / numberofbids;
				for(int i = 1; i <=numberofbids; i++){
					Bid bid = new Bid(this.playerName, this.id, limitPrice, MIN_MWH, this.type);
					if(observer.DEBUG)
						System.out.println(bid.toString());
					bids.add(bid);
					limitPrice+=unitPriceIncrement;
					totalE -= MIN_MWH;

					if((totalE) <= 0) {
						return;
					}
				}
				//limitPrice = observer.pricepredictor.meanOfBalancingPrices(observer.pricepredictor.shortBalanceTransactionsData);
				Bid bid = new Bid(this.playerName, this.id, limitPrice, totalE, this.type);
				if(observer.DEBUG)
					System.out.println(bid.toString());
				bids.add(bid);

			}
			else{
				// Submit sell orders

				double surplus = 0 - neededMWh;
				minMWh= (Math.abs(this.initialNeededMWh)*(bestMove.volPercentage-1));
				if(minMWh > surplus)
					minMWh /= numberofbids;
				else
					minMWh = surplus / numberofbids;

				for(int i = 1; i <=numberofbids; i++){
					Ask ask = new Ask(this.playerName, this.id, limitPrice, minMWh, this.type);
					if(observer.DEBUG)
						System.out.println("Selling: " + ask.toString());
					asks.add(ask);

					limitPrice+=unitPriceIncrement;
				}
			}
		}
	}
}
