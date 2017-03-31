package Agents;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import MCTS.Action.ACTION_TYPE;
import MCTS.MCTS;
import MCTS.TreeNode;
import Observer.Observer;
import Observer.PricePredictor;

public class MCTSAgent extends Agent {
	
	public MCTS mcts;
	public boolean firstTimeFlags =  true;
	public double remainingNeededMWh;
	
	public MCTSAgent(String name, int id, double neededMWh, double mean, double stddev, double mctsSim){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		this.type = agentType.BROKER;
		this.mcts = new MCTS(mctsSim, name);
	}
	
	public MCTS getMCTS(){
		return this.mcts;
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
		if(this.neededMWh > 0)
		{
			TreeNode bestMove = mcts.getBestMCTSMove(observer);
			if(bestMove == null)
				System.out.println("BestMove is null");
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
			double numberofbids = 10;
			double unitPriceIncrement = 1.00;
			double limitPrice = bestMove.minmctsClearingPrice;
			double priceRange = bestMove.maxmctsClearingPrice - bestMove.minmctsClearingPrice;
			double minMWh = 1;
			
			
			unitPriceIncrement = priceRange / numberofbids;
			
			
			if(!bestMove.nobid){
				if(bestMove.actionType == ACTION_TYPE.BUY){
					// Submit buy orders
					minMWh= (Math.abs(this.neededMWh)*bestMove.volPercentage) / numberofbids;
					for(int i = 1; i <=numberofbids; i++){
						Bid bid = new Bid(this.playerName, this.id, limitPrice, minMWh);
						if(observer.DEBUG)
							System.out.println(bid.toString());
						bids.add(bid);
						limitPrice+=unitPriceIncrement;
					}
				}
				else{
					// Submit sell orders
					minMWh= (Math.abs(this.initialNeededMWh)*(1-bestMove.volPercentage));
					if(minMWh > Math.abs(this.neededMWh))
						minMWh /= numberofbids;
					else
						minMWh = Math.abs(this.neededMWh) / numberofbids;
					for(int i = 1; i <=numberofbids; i++){
						Ask ask = new Ask(this.playerName, this.id, limitPrice, minMWh);
						if(observer.DEBUG)
							System.out.println(ask.toString());
						asks.add(ask);
						limitPrice+=unitPriceIncrement;
					}
				}
			}
		}
	}
}
