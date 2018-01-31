package MCTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import configure.Configure;
import Agents.Agent;
import MCTS.TreeNode;
import Observer.Observer;
import Observer.PricePredictor;
import Observer.Utility;

/**
 * Created by Moinul Morshed Porag Chowdhury
 * mchowdhury4@miners.utep.edu
 * Date: 19-March-2016
 * Time: 21:51:31
 */
public class MCTS {
	// Predicted Clearing Price
	public double [] probability = {0.025, 0.069, 0.16, 0.30, 0.50, 0.69, 0.84, 0.932, 0.975};
	public double [] sigma = {-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2};
	
	public PricePredictor pricePredictor = new PricePredictor(Agent.predictorName);
	public TreeNode root;
	public double totalSuccessfulBids = 0;
	public double lastmctsClearingPrice = 0.0;
	public double lastPriceDiffPerTradeAction = 0.0;
	double [] arrMctsPredClearingPrice = new double[25];

	public double [] thresholdMCTS = new double[5];
	public double varthreshold = 10;


	public int thresholdLowAuctionsPerc = 40;
	public boolean booNobid = false;
	public ArrayList<Action> actions;
	public ArrayList<Action> dynamicactionsMCTS2;
	public double mctsSim;
	public String playerName;
	public int thresholdcount = 0;
	public double MIN_PR = 0.025;
	public double MAX_PR = 0.975;
	public Utility utility;
	
	public MCTS(double mctsSim, String name){
		actions = new ArrayList<Action>();
		dynamicactionsMCTS2 = new ArrayList<Action>();
		this.mctsSim = mctsSim;
		this.thresholdMCTS[0] = mctsSim*0.05;
		this.thresholdMCTS[1] = mctsSim*0.1;
		this.thresholdMCTS[2] = mctsSim*0.2;
		this.thresholdMCTS[3] = mctsSim*0.5;
		this.thresholdMCTS[4] = mctsSim;
		this.playerName = name;
		utility = new Utility();
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

		/*
		Action action = new Action(0,-2,-2,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.add(action);
		action = new Action(1,-1,-1,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.add(action);
		action = new Action(2,0,0,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.add(action);
		action = new Action(3,1,1,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.add(action);
		action = new Action(4,2,2,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.add(action);
		*/
		
		/*
		//C1
		double [] newP = new double[observer.hourAhead+1];
		double threshold = 1.0;
		
		for(int i = 0; i < newP.length; i++) {
			newP[i] = MIN_PR;
			if(i == 0)
				threshold *= MAX_PR;
			else
				threshold *= MIN_PR;
		}

		double totP = Double.MIN_VALUE;

		int lastCounter = 0;
		while(totP < threshold) {
			totP = 1;
			for(int i = 0; i < newP.length; i++) {
				totP *= newP[i];
			}

			if(totP < threshold)
			{
				int index = lastCounter%newP.length;
				newP[index]+=MIN_PR;
			}
			lastCounter++;
		}

		double mult = utility.calc_q(newP[newP.length-1]);
		System.out.println("q: " + newP[newP.length-1] + " z: " + mult);
		if(newP[newP.length-1] < 0.5)
			mult *= -1;
		*/
		
		/*
		// C2
		// Find the actions sigma
		// increasing probability
		int [] newPIndices = new int[observer.hourAhead+1];
		int threshold = 7;
		for(int i = 0; i < newPIndices.length; i++) {
			newPIndices[i] = threshold;
			//threshold = 0;
			if(threshold > 1)
				threshold-=1;
		}

		double [] arrPredClearingPrice = new double[observer.hourAhead+1];
		double [] arrsortedPredClearingPrice = new double[observer.hourAhead+1];
		
		for(int HA = 0; HA < arrPredClearingPrice.length; HA++){
			double d = observer.pricepredictor.getPrice(HA);
			arrPredClearingPrice[HA] = d;
			arrsortedPredClearingPrice[HA] = d;
		}
		Arrays.sort(arrsortedPredClearingPrice);

		int index = 0;
		for(int i = 0; i < arrPredClearingPrice.length; i++) {
			if(arrPredClearingPrice[observer.hourAhead] == arrsortedPredClearingPrice[i])
			{
				index = i;
				break;
			}
		}
		
		double mult  = sigma[newPIndices[index]];
		*/	
//		Action action = new Action(0,mult,mult,false, Action.ACTION_TYPE.BUY, 1.00, false);
//		actions.add(action);
		
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
//		Action action = new Action(0,0,0,true, Action.ACTION_TYPE.NO_BID, 1.00, false);
//		actions.add(action);
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
	}

	public TreeNode getBestMCTSMove(Observer observer) {
		int [] arrCounterHigherBids = new int[25];
		TreeNode root = new TreeNode();
		//root.parent = null;
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

		// loop it and do some number of simulations
		for(int i=0; i < this.mctsSim; i++){
			root.runMonteCarlo(actions, this, observer,i);
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