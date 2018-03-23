package MCTS_Kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import configure.Configure;
import Agents.Agent;
import MCTS_Kernel.TreeNode;
import Observer.Observer;
import Observer.PricePredictor;
import Observer.Utility;

/**
 * Created by Moinul Morshed Porag Chowdhury
 * mchowdhury4@miners.utep.edu
 * Date: 19-March-2016
 * Time: 21:51:31
 */
public class MCTS_Kernel {
	// Predicted Clearing Price
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
    public ArrayList<ArrayList<Action>> actions;
    public ArrayList<Action> dynamicactionsMCTS2;
    public double mctsSim;
    public String playerName;
    public int thresholdcount = 0;
    
	public int CP = 0;
	public int PCP = 1;
	public int Pr = 2;
	public int HA = 3;
	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	public double MIN_PR = 0.025;
	public double newMIN_PR = 0.005;
	public double MAX_PR = 0.975;
	public double newMAX_PR = 0.998;
	public double [] probability = {0.025, 0.069, 0.16, 0.30, 0.50, 0.69, 0.84, 0.932, 0.975};
	public double [] sigma = {-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2};
	public Utility utility;
	double totalLayerInput;

    
	public MCTS_Kernel(double mctsSim, String name){
		actions = new ArrayList<ArrayList<Action>>();
		dynamicactionsMCTS2 = new ArrayList<Action>();
		this.mctsSim = mctsSim;
		this.thresholdMCTS[0] = mctsSim*0.05;
		this.thresholdMCTS[1] = mctsSim*0.10;
		this.thresholdMCTS[2] = mctsSim*0.20;
		this.thresholdMCTS[3] = mctsSim*0.50;
		this.thresholdMCTS[4] = mctsSim;
		this.playerName = name;
		
		utility = new Utility();
	}

	public void setup(Observer observer){
		
    }
	
    public TreeNode getBestMCTSMove(Observer observer) {
    	int [] arrCounterHigherBids = new int[25];
    	
    	for(int j = 0; j <= observer.hourAhead; j++){
    		actions.add(j, new ArrayList<Action>());
    		arrMctsPredClearingPrice[j] = observer.pricepredictor.getPrice(j);
    		arrCounterHigherBids[j] = 0;
    	}

    	
    	//***********ADDING INITIAL ACTION TO MCTS*************//
//    	double [][] info = new double[observer.hourAhead+1][4];
		//double mult = C4(observer, info);
    	//double mult = C2(observer);
    	//double [][] info = new double[ob.hourAhead+1][4];
    	
		Action action = new Action(0,0,0,true, Action.ACTION_TYPE.NO_BID, 1.00, false);
		actions.get(0).add(action);
		double mult = 0;
		action = new Action(1,mult,mult,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.get(0).add(action);
		mult = -1;
		action = new Action(2,mult,mult,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.get(0).add(action);
		mult = 1;
		action = new Action(3,mult,mult,false, Action.ACTION_TYPE.BUY, 1.00, false);
		actions.get(0).add(action);
		//************************//
		TreeNode root = new TreeNode(arrMctsPredClearingPrice[observer.hourAhead], actions.get(0), observer.STDDEV[observer.hourAhead], thresholdcount);
    	root.parent = null;
    	root.nVisits = 0;//actions.size();
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
		
//    	root.minmctsClearingPrice = arrMctsPredClearingPrice[observer.hourAhead];
    	
		// loop it and do some number of simulations
    	for(int i=0; root.ntotValueVisits < this.mctsSim; i++){
    		root.runMonteCarlo(actions.get(0), this, observer,i);
    		i = (int)root.ntotValueVisits;
    		//System.out.println("sim " + i);
    	}
    	root.printKernel();
    	
    	
//    	for(int jj=observer.hourAhead-1; jj>= 0; jj--){
//    		if(arrMctsPredClearingPrice[observer.hourAhead] > arrMctsPredClearingPrice[jj]){
//				arrCounterHigherBids[observer.hourAhead]++;
//			}
//    	}
    	
//    	int thresholdLimit = observer.hourAhead * (thresholdLowAuctionsPerc/100);
    	
    	/*
		if(arrCounterHigherBids[observer.hourAhead] > thresholdLimit)
			return new TreeNode(true); // No bid option
		else
			return root.finalSelect(observer);
    	*/
    	return root.finalSelect(observer);
    }
    
	public double C4(Observer ob, double [][] info) {
		//C4
		double threshold = MAX_PR;
		//double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
		// Initialize
		// System.out.println("Initialize array");
		for(int i = 0; i < info.length; i++) {
			info[i][Pr] = newMIN_PR;
			double d =  ob.pricepredictor.getPrice(i);
			info[i][CP] = d;
			info[i][PCP] = d;
			info[i][HA] = i;
		}

		// print2D(info);

		int lastCounter = 0;
		while(isThreshold(info, threshold)) {
			// sort the array based on clearing price
			bubbleSort(info);
			// System.out.println("BUBBLE SORT");
			// print2D(info);
			// get the index to increment the probability
			lastCounter = getProperIndex(info);

			if(lastCounter == -1)
				break; // finished updating all

			// System.out.println("INCREMENTING "+lastCounter+": "+ info[lastCounter][CP] +" from " + info[lastCounter][Pr] +" to "+ (info[lastCounter][Pr]+MIN_PR));
			info[lastCounter][Pr]+=newMIN_PR;
			double prp = info[lastCounter][Pr];
			double z = utility.calc_q(prp);
			if(prp < 0.5)
				z *= -1;
			info[lastCounter][PCP] = Math.abs(info[lastCounter][CP]+(7.8*z));
			// System.out.println("Z:"+z+" newPCP "+info[lastCounter][PCP]);
		}

		// Find the probability of corresponding hourAhead auction
		int index = 0;
		double prob = 0.5;
		//print2D(info);
		for(int i = 0; i < info.length; i++) {
			if(ob.hourAhead == info[i][HA])
			{
				index = i;
				prob = info[index][Pr];
				break;
			}
		}

		double mult = utility.calc_q(prob);
		if(prob < 0.5)
			mult *= -1;

		//double std = mult*7.8;
		//double C2limitPrice = Math.abs(limitPrice+std);

		return mult;
	}
	
	public boolean isThreshold(double [][] p, double threshold) {
		double totalP = p[0][Pr];
		for(int i = 1; i < p.length; i++) {
			totalP += p[i][Pr]*(1-totalP); 
		}
		if(totalP >= threshold)
			return false;
		return true;
	}
	public int getProperIndex(double [][] info) {
		for(int i = 0; i < info.length; i++) {
			if(info[i][Pr] < MAX_PR) {
				return i;
			}
		}
		return -1;
	}

	public void bubbleSort(double [][] info) {
		double tempPr = 0.0;
		double tempHA = 0;
		double tempPCP = 0.0;
		double tempCP = 0.0;
		for(int i = 0; i < info.length; i++) {
			for(int j = i+1; j < info.length; j++) {
				if(info[i][PCP] > info[j][PCP]) {
					tempPr = info[i][Pr];
					tempHA = info[i][HA];
					tempPCP = info[i][PCP];
					tempCP = info[i][CP];

					info[i][Pr] = info[j][Pr];
					info[i][HA] = info[j][HA];
					info[i][PCP] = info[j][PCP];
					info[i][CP] = info[j][CP];

					info[j][Pr] = tempPr;
					info[j][HA] = tempHA;
					info[j][PCP] = tempPCP;
					info[j][CP] = tempCP;
				}
			}
		}
	}
	
	public double C2(Observer ob) {
		// increasing probability: FINAL
		int [] newPIndices = new int[ob.hourAhead+1];
		int threshold = 7;
		for(int i = 0; i < newPIndices.length; i++) {
			newPIndices[i] = threshold;
			//threshold = 0;
			if(threshold > 1)
				threshold-=1;
		}

		double [] arrPredClearingPrice = new double[ob.hourAhead+1];
		double [] arrsortedPredClearingPrice = new double[ob.hourAhead+1];

		for(int HA = 0; HA < arrPredClearingPrice.length; HA++){
			double d = ob.pricepredictor.getPrice(HA);
			arrPredClearingPrice[HA] = d;
			arrsortedPredClearingPrice[HA] = d;
		}
		Arrays.sort(arrsortedPredClearingPrice);

		int index = 0;
		for(int i = 0; i < arrPredClearingPrice.length; i++) {
			if(arrPredClearingPrice[ob.hourAhead] == arrsortedPredClearingPrice[i])
			{
				index = i;
				break;
			}
		}
		double C1limitPrice = 0.0;
		double limitPrice = arrPredClearingPrice[ob.hourAhead];
		double z = sigma[newPIndices[index]];
		//System.out.println("Z: " + z);
		//C1limitPrice = Math.abs(limitPrice+z*7.8);
		return z;
	}

}