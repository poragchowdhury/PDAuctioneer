package MCTS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import configure.Configure;

import Agents.Agent;
import MCTS.TreeNode;
import Observer.Observer;
import Observer.PricePredictor;

/**
 * Created by Moinul Morshed Porag Chowdhury
 * mchowdhury4@miners.utep.edu
 * Date: 19-March-2016
 * Time: 21:51:31
 */
public class MCTS {
	// Predicted Clearing Price
	public PricePredictor pricePredictor = new PricePredictor(Agent.predictorName);
	public TreeNode root;
	public double totalSuccessfulBids = 0;
	public double lastmctsClearingPrice = 0.0;
	public double lastPriceDiffPerTradeAction = 0.0;
	double [] arrMctsPredClearingPrice = new double[25];
	
	int thresholdLowAuctionsPerc = 40;
	public boolean booNobid = false;
    ArrayList<Action> actions;
    public double mctsSim;
    public String playerName;
    
	public MCTS(double mctsSim, String name){
		actions = new ArrayList<Action>();
		this.mctsSim = mctsSim;
		this.playerName = name;
	}

	public void setup(Observer observer){
		
		// Need a predictor
		////this.arrPredictedClearingPrices = observer.arrPredictedClearingPrices15;
		
    	// Create all the actions
		Action action = new Action("A0",-2,0,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("A1",-1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("A2",-1,0,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("A3",0,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("A4",0,2,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	
    	action = new Action("A5",-2,0,false, Action.ACTION_TYPE.BUY, 1.20);
    	actions.add(action);
    	action = new Action("A6",-1,1,false, Action.ACTION_TYPE.BUY, 1.20);
    	actions.add(action);
    	action = new Action("A7",-1,0,false, Action.ACTION_TYPE.BUY, 1.20);
    	actions.add(action);
    	action = new Action("A8",0,1,false, Action.ACTION_TYPE.BUY, 1.20);
    	actions.add(action);
    	action = new Action("A9",0,2,false, Action.ACTION_TYPE.BUY, 1.20);
    	actions.add(action);

    	// no bid
		action = new Action("A10",0,0,true, Action.ACTION_TYPE.NO_BID, 1.00);
    	actions.add(action);

    	// Selling
    	action = new Action("A11",-2,0,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("A12",-1,1,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("A13",-1,0,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("A14",0,1,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("A15",0,2,false, Action.ACTION_TYPE.SELL, 1.0);
    	actions.add(action);

    }
	
    public TreeNode getBestMCTSMove(Observer observer) {
    	int [] arrCounterHigherBids = new int[25];
    	TreeNode root = new TreeNode();
    	root.hourAheadAuction = observer.hourAhead+1;
    	
    	// Set the number of Simulations
    	if(this.playerName.equalsIgnoreCase("varMCTSAgent"+Configure.getVARMCTSAGENTITERATION())){
    		// do nothing
    	}
    	else if(Configure.getDIFF_MCTS_SIM_CASES_EXP()){
    		this.mctsSim = observer.MCTSSimulation;
    	}
    	else{
    		// use the default agent's mctssim
    	}
    	
    	//System.out.println("Iterations " + i);
    	double [] param = new double[11];
    	param = observer.getFeatures(param);
    	
    	for(int j = 0; j <= observer.hourAhead; j++){
    		param[2] = j;
    		arrMctsPredClearingPrice[j] = pricePredictor.getLimitPrice(param); //Math.abs((r.nextGaussian()*2)+observer.arrPredictedClearingPrices15[observer.currentTimeSlot][j]);
    		arrCounterHigherBids[j] = 0;
    	}
    	root.minmctsClearingPrice = arrMctsPredClearingPrice[observer.hourAhead];
    	
		// loop it and do some number of simulations
    	for(int i=0; i < this.mctsSim; i++){
    		root.runMonteCarlo(actions, this, observer);
    	}
    	
    	for(int jj=observer.hourAhead-1; jj>= 0; jj--){
    		if(arrMctsPredClearingPrice[observer.hourAhead] > arrMctsPredClearingPrice[jj]){
				arrCounterHigherBids[observer.hourAhead]++;
			}
    	}
    	
    	int thresholdLimit = observer.hourAhead * (thresholdLowAuctionsPerc/100);
    	
    	/*
		if(arrCounterHigherBids[observer.hourAhead] > thresholdLimit)
			return new TreeNode(true); // No bid option
		else
			return root.finalSelect(observer);
    	*/
    	return root.finalSelect(observer);
    }
}