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
	public int debugCounter=0;
	public double totalSuccessfulBids = 0;
	public double lastmctsClearingPrice = 0.0;
	public double lastPriceDiffPerTradeAction = 0.0;
	double [] arrMctsPredClearingPrice = new double[25];
	
	public int thresholdLowAuctionsPerc = 40;
	public boolean booNobid = false;
    public ArrayList<Action> actions;
    public ArrayList<Action> actionsMCTS2;
    public double mctsSim;
    public String playerName;
    
	public MCTS(double mctsSim, String name){
		actions = new ArrayList<Action>();
		actionsMCTS2 = new ArrayList<Action>();
		this.mctsSim = mctsSim;
		this.playerName = name;
	}

	public void setup(Observer observer){
		/*
		// Need a predictor
		////this.arrPredictedClearingPrices = observer.arrPredictedClearingPrices15;
		//double volModi2 = 0.80;
		double volModi1 = 1.0;
		Action action = new Action("0",-2,2,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("1",-2,1,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("2",-2,0,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("3",-2,-1,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("4",-1,2,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("5",-1,1,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("6",-1,0,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("7",0,2,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("8",0,1,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	action = new Action("9",1,2,false, Action.ACTION_TYPE.BUY, volModi1);
    	actions.add(action);
    	
    	// no bid
		action = new Action("10",0,0,true, Action.ACTION_TYPE.NO_BID, 1);
    	actions.add(action);
    	
		/*
    	action = new Action("11",-2,2,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("12",-2,1,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("13",-2,0,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("14",-2,-1,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("15",-1,2,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("16",-1,1,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("17",-1,0,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("18",0,2,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("19",0,1,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
    	action = new Action("20",1,2,false, Action.ACTION_TYPE.BUY, volModi2);
    	actions.add(action);
		*/
		/*
		// Create all the actions
		Action action = new Action("0",-1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("1",-1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("2",-1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("3",-1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("4",-1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	
    	action = new Action("5",-1,1,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("6",-1,1,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("7",-1,1,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("8",-1,1,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("9",-1,1,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
		*/
		
		Action action = new Action("0",-2,-2,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("1",-1,-1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("2",0,0,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("3",1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("4",2,2,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
		
		
    	// Create all the actions
		//Action action = new Action("0",0,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	//actions.add(action);
    	/*
    	action = new Action("1",-1,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("2",-1,0,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("3",0,1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	action = new Action("4",0,2,false, Action.ACTION_TYPE.BUY, 1.00);
    	actions.add(action);
    	/*
    	action = new Action("5",-2,0,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("6",-1,1,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("7",-1,0,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("8",0,1,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
    	action = new Action("9",0,2,false, Action.ACTION_TYPE.BUY, 0.50);
    	actions.add(action);
		*/
		// no bid
		action = new Action("10",0,0,true, Action.ACTION_TYPE.NO_BID, 1.00);
    	actions.add(action);
    	/*
    	// Selling
    	action = new Action("11",-2,0,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("12",-1,1,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("13",-1,0,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("14",0,1,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
    	action = new Action("15",0,2,false, Action.ACTION_TYPE.SELL, 1.20);
    	actions.add(action);
		*/
    	
    	action = new Action("0",0,-0.1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actionsMCTS2.add(action);
    	action = new Action("1",0,-0.05,false, Action.ACTION_TYPE.BUY, 1.00);
    	actionsMCTS2.add(action);
    	action = new Action("2",0,0,false, Action.ACTION_TYPE.BUY, 1.00);
    	actionsMCTS2.add(action);
    	action = new Action("3",0,0.05,false, Action.ACTION_TYPE.BUY, 1.00);
    	actionsMCTS2.add(action);
    	action = new Action("4",0,0.1,false, Action.ACTION_TYPE.BUY, 1.00);
    	actionsMCTS2.add(action);
		// no bid
		action = new Action("10",0,0,true, Action.ACTION_TYPE.NO_BID, 1.00);
		actionsMCTS2.add(action);
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
    		//this.mctsSim = observer.MCTSSimulation;
    	}
    	else{
    		// use the default agent's mctssim
    	}
		
    	for(int j = 0; j <= observer.hourAhead; j++){
    		arrMctsPredClearingPrice[j] = observer.pricepredictor.getPrice(j);
    		arrCounterHigherBids[j] = 0;
    	}
	
    	
    	root.minmctsClearingPrice = arrMctsPredClearingPrice[observer.hourAhead];
    	
		// loop it and do some number of simulations
    	for(int i=0; i < this.mctsSim; i++){
    		root.runMonteCarlo(actions, this, observer, false);
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
    
    public TreeNode getBestDoubleMCTSMove(Observer observer) {
    	int [] arrCounterHigherBids = new int[25];
    	TreeNode root = new TreeNode();
    	root.hourAheadAuction = observer.hourAhead+1;
    	
    	// Set the number of Simulations
    	if(this.playerName.equalsIgnoreCase("varMCTSAgent"+Configure.getVARMCTSAGENTITERATION())){
    		// do nothing
    	}
    	else if(Configure.getDIFF_MCTS_SIM_CASES_EXP()){
    		//this.mctsSim = observer.MCTSSimulation;
    	}
    	else{
    		// use the default agent's mctssim
    	}
		
    	for(int j = 0; j <= observer.hourAhead; j++){
    		arrMctsPredClearingPrice[j] = observer.pricepredictor.getPrice(j);
    		arrCounterHigherBids[j] = 0;
    	}
	
    	
    	root.minmctsClearingPrice = arrMctsPredClearingPrice[observer.hourAhead];
    	
		// loop it and do some number of simulations
    	for(int i=0; i < this.mctsSim; i++){
    		root.runMonteCarlo(actions, this, observer, false);
    	}
    	
    	/*
    	for(int jj=observer.hourAhead-1; jj>= 0; jj--){
    		if(arrMctsPredClearingPrice[observer.hourAhead] > arrMctsPredClearingPrice[jj]){
				arrCounterHigherBids[observer.hourAhead]++;
			}
    	}
    	
    	int thresholdLimit = observer.hourAhead * (thresholdLowAuctionsPerc/100);
    	*/
    	/*
		if(arrCounterHigherBids[observer.hourAhead] > thresholdLimit)
			return new TreeNode(true); // No bid option
		else
			return root.finalSelect(observer);
    	*/
//    	for(int i = observer.hourAhead; i >= 0; i--)
//    		System.out.print(arrMctsPredClearingPrice[i] + " ");
//    	System.out.println();
    	double pmctsprice = root.getMCTSValue(arrMctsPredClearingPrice, observer);
//    	for(int i = observer.hourAhead; i >= 0; i--)
//    		System.out.print(arrMctsPredClearingPrice[i] + " ");
//    	System.out.println();
//    	
    	
    	for(Action a : actionsMCTS2) {
    		a.minMult = pmctsprice;
    	}
    	
    	root = new TreeNode();
    	root.hourAheadAuction = observer.hourAhead+1;
    	root.minmctsClearingPrice = arrMctsPredClearingPrice[observer.hourAhead];
    	// loop it and do some number of simulations
    	for(int i=0; i < this.mctsSim; i++){
    		root.runMonteCarlo(actionsMCTS2, this, observer, true);
    	}
    	return root.finalSelect(observer);
    	
    	
    }
}