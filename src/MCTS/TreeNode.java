package MCTS;
/**

 * Created by Moinul Morshed Porag Chowdhury
 * mchowdhury4@miners.utep.edu
 * Date: 19-March-2016
 * Time: 21:51:31
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import configure.Configure;
import Auctioneer.Ask;
import Auctioneer.Bid;
import MCTS.Action.ACTION_TYPE;
import Observer.Observer;

public class TreeNode {
	public int CP = 0;
	public int PCP = 1;
	public int Pr = 2;
	public int HA = 3;
    static Random r = new Random();
    static double epsilon = 1e-6;
	public double MIN_PR = 0.025;
	public double MAX_PR = 0.975;
    public Action.ACTION_TYPE actionType;
	public double [] probability = {0.025, 0.069, 0.16, 0.30, 0.50, 0.69, 0.84, 0.932, 0.975};
	public double [] sigma = {-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2};
	
    ArrayList<TreeNode> children;
    public double nVisits, 
	    totValue, 
    	minMult,
    	maxMult,
    	volPercentage,
    	currentNodeCostAvg,
    	currentNodeCostLast;
    
    public boolean dynamicState = false;
    public boolean enableDynamicAction = true;
    
    public int actionName;
    
    int hourAheadAuction;
    int	appliedAction;
    
    public boolean nobid = false;
    
    public TreeNode(){
    	
    }
    
    public TreeNode(int n, boolean nobid){
    	this.actionName = n;
    	this.nobid = nobid;
    }
    
    public TreeNode(TreeNode tn){
    	this.nVisits = tn.nVisits;
    	this.totValue = tn.totValue;
    	this.currentNodeCostAvg = tn.currentNodeCostAvg;
    	this.currentNodeCostLast = tn.currentNodeCostLast;
    	this.minMult = tn.minMult;
    	this.maxMult = tn.maxMult;
    	this.hourAheadAuction = tn.hourAheadAuction;
    	this.appliedAction = tn.appliedAction;
    	this.nobid = tn.nobid;
    	this.actionType = tn.actionType;
    	this.volPercentage = tn.volPercentage;
    	this.actionName = tn.actionName;
    	this.dynamicState = tn.dynamicState;
    }
    
    public int unvisitedChildren(TreeNode tn){
    	int count = 0;
    	
    	for(TreeNode child : tn.children)
    	{
    		if(child.nVisits == 0)
    			count++;
    	}
    	return count;
    }
    
    public double IJCAIC1(Observer ob, MCTS mcts) {
    	//C1
		double [] newP = new double[ob.hourAhead+1];
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
		double mult = mcts.utility.calc_q(newP[newP.length-1]);
		System.out.println("q: " + newP[newP.length-1] + " z: " + mult);
		if(newP[newP.length-1] < 0.5)
			mult *= -1;
		
		return mult;
	}
    
    public double newC1(Observer ob, MCTS mcts) {
    	//C1
		double [] newP = new double[ob.hourAhead+1];
		double threshold = MAX_PR;
		
		for(int i = 0; i < newP.length; i++) {
			newP[i] = MIN_PR;
		}

		int lastCounter = 0;
		while(isThreshold(newP, threshold)) {
			lastCounter = lastCounter%newP.length;
			newP[lastCounter]+=MIN_PR;
			lastCounter++;
		}

		double mult = mcts.utility.calc_q(newP[newP.length-1]);
		//System.out.println("q: " + newP[newP.length-1] + " z: " + mult);
		if(newP[newP.length-1] < 0.5)
			mult *= -1;

		return mult;
    }

    public void staticAction() {
    	/*
    	Action action = new Action(0,-2,-2,false, Action.ACTION_TYPE.BUY, 1.00, false);
		mcts.actions.add(action);
		action = new Action(1,-1,-1,false, Action.ACTION_TYPE.BUY, 1.00, false);
		mcts.actions.add(action);
		action = new Action(2,0,0,false, Action.ACTION_TYPE.BUY, 1.00, false);
		mcts.actions.add(action);
		action = new Action(3,1,1,false, Action.ACTION_TYPE.BUY, 1.00, false);
		mcts.actions.add(action);
		action = new Action(4,2,2,false, Action.ACTION_TYPE.BUY, 1.00, false);
		mcts.actions.add(action);
    	action = new Action(5,0,0,true, Action.ACTION_TYPE.NO_BID, 1.00, false);
		mcts.actions.add(action);
		*/
    }
    
    public void runMonteCarlo(ArrayList<Action> actions, MCTS mcts, Observer ob, int sims) {
    	
    	double simCost = 0.0;
    	double neededEnergy = ob.neededEneryMCTSBroker;
    	double iniNeededEnergy = ob.initialNeededEneryMCTSBroker;
        List<TreeNode> visited = new LinkedList<TreeNode>();
        TreeNode cur = this;
        
        visited.add(this);
        
        // add dynamic action space logic
        if(sims == 0) {
        	//double mult = newC1(ob, mcts);
        	double [][] info = new double[ob.hourAhead+1][4];
			
        	/* C3
        	 * -578271.95:1K: with 0% error:69.96 // -579544.97
        	 * -517060.30: with 10% error: pp err 40.653649
        	 *  */
			double mult = C3(ob, mcts, info);
        	
        	/* IJCAIC2
        	 * -534894.45:1K: with 0% error: pp err
        	 * -492914.52:1K: with 10% error: pp err 32.2803751942143 
        	 *  */
			//double mult = IJCAIC2(ob, mcts);
        	
    		Action action = new Action(0,mult,mult,false, Action.ACTION_TYPE.BUY, 1.00, false);
    		mcts.actions.add(action);
    		action = new Action(1,0,0,true, Action.ACTION_TYPE.NO_BID, 1.00, false);
    		mcts.actions.add(action);
    	}
        else if(sims > mcts.thresholdMCTS[mcts.thresholdcount] && enableDynamicAction) {
        	int actionsize = actions.size();
        	double pmctsprice = cur.getMCTSValue(mcts.arrMctsPredClearingPrice[this.hourAheadAuction-1],ob);
        	Action action = new Action(actionsize,pmctsprice,0,false, Action.ACTION_TYPE.BUY, 1.00, true);
        	mcts.actions.add(action);	
        	mcts.thresholdcount++;
        	
        }
        
        int actionsize = actions.size();
        
    	while (!cur.isLeaf()) {
    		
        	if(cur.children == null){
        		cur.expand(actions, mcts, ob, mcts.arrMctsPredClearingPrice[this.hourAheadAuction-1]);
        	}
        	
    		TreeNode unvisitedNode = cur.selectRandomUnvisited(mcts, ob);
    		if(unvisitedNode != null){
    			// Initiate all 11 nodes
        		// select a random node
        		cur = unvisitedNode;
        		visited.add(cur);
        		// do the rollout
        		double [] retValue = rollout(cur, mcts.arrMctsPredClearingPrice, ob, neededEnergy, iniNeededEnergy, actions, mcts);
        		// add to the sim cost
        		neededEnergy -= retValue[0];
                simCost += retValue[1]*(-1);
                break;
        	}
    		
    		int childrensize = cur.children.size();
    		if(childrensize < actionsize)
        	{
    			// Reset the exploration count to 1 for all children
    			//for(TreeNode child : cur.children)
    			//	child.nVisits = 0;
    			// add a new child
    			for(int i = childrensize; i < actionsize; i++) {
	    			Action action = actions.get(i);
	        		TreeNode newchild = new TreeNode();
	            	newchild.hourAheadAuction =cur.hourAheadAuction-1;
	                newchild.appliedAction = action.actionName;
	                newchild.nobid = action.nobid;
	                newchild.minMult = action.minMult;
	                newchild.maxMult = action.maxMult;
	                newchild.actionType = action.type;
	                newchild.volPercentage = action.percentage;
	                newchild.actionName = action.actionName;
	                newchild.dynamicState = action.dynamicAction;
	                cur.children.add(newchild);
    			}
    			unvisitedNode = cur.selectRandomUnvisited(mcts, ob);	
                cur = unvisitedNode;
                visited.add(cur);
        		// do the rollout
        		double [] retValue = rollout(cur, mcts.arrMctsPredClearingPrice, ob, neededEnergy, iniNeededEnergy, actions, mcts);
        		// add to the sim cost
        		neededEnergy -= retValue[0];
                simCost += retValue[1]*(-1);
                break;
        	}
        	
    		cur = cur.select(mcts, ob, neededEnergy);
    		
    		if(cur == null)
    			System.out.println("hi");
            // Do the simulation-rollout for wholesale auction
            double [] retValue = simulation(cur, mcts.arrMctsPredClearingPrice, ob, neededEnergy, iniNeededEnergy);
            neededEnergy -= retValue[0];
            simCost += retValue[1]*(-1);
            
            visited.add(cur);
        }
        
    	double balancingSimCost = Math.abs(neededEnergy)*ob.arrBalacingPrice[ob.currentTimeSlot]*(-1); //
    	
    	simCost += balancingSimCost;

        // make the sim cost as unit cost
        simCost /= ob.neededEneryMCTSBroker;
        
        for (TreeNode node : visited) {
            node.updateStats(simCost, balancingSimCost);
        }
        
    }

    public double IJCAIC2(Observer ob, MCTS mcts) {
    	
    	// C2
    	int [] newPIndices = new int[ob.hourAhead+1];
		int threshold = 7;
		for(int i = 0; i < newPIndices.length; i++) {
			newPIndices[i] = threshold;
			//threshold = 0;
			if(threshold > 1)
				threshold-=1;
		}

		double [] arrsortedPredClearingPrice = new double[ob.hourAhead+1];
		
		for(int HA = 0; HA < arrsortedPredClearingPrice.length; HA++){
			arrsortedPredClearingPrice[HA] = mcts.arrMctsPredClearingPrice[HA];
		}
		Arrays.sort(arrsortedPredClearingPrice);

		int index = 0;
		for(int i = 0; i < arrsortedPredClearingPrice.length; i++) {
			if(mcts.arrMctsPredClearingPrice[ob.hourAhead] == arrsortedPredClearingPrice[i])
			{
				index = i;
				break;
			}
		}
		
		double mult = sigma[newPIndices[index]];
		return mult;
    }
    
    public double C3(Observer ob, MCTS mcts, double [][] info) {
    	//C3
    	double threshold = MAX_PR;
    	double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
		// Initialize
    	// System.out.println("Initialize array");
		for(int i = 0; i < info.length; i++) {
			info[i][Pr] = MIN_PR;
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
			info[lastCounter][Pr]+=MIN_PR;
			double prp = info[lastCounter][Pr];
			double z = mcts.utility.calc_q(prp);
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
	
		double mult = mcts.utility.calc_q(prob);
		if(prob < 0.5)
			mult *= -1;

		return mult;
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
	public boolean isThreshold(double [][] p, double threshold) {
    	double totalP = p[0][Pr];
    	for(int i = 1; i < p.length; i++) {
    		totalP += p[i][Pr]*(1-totalP); 
    	}
    	if(totalP >= threshold)
    		return false;
    	return true;
    }
    public boolean isThreshold(double [] p, double threshold) {
    	double totalP = p[0];
    	for(int i = 1; i < p.length; i++) {
    		totalP += p[i]*(1-totalP); 
    	}
    	if(totalP >= threshold)
    		return false;
    	return true;
    }
    
    public void expand(ArrayList<Action> actions, MCTS mcts, Observer ob, double mean) {
    	int nActions = actions.size();
    	children = new ArrayList<TreeNode>();
    	
    	int newHourAheadAuction = this.hourAheadAuction-1;
		
        for (int i=0; i<nActions; i++) {
            TreeNode newchild = new TreeNode();
            Action action = actions.get(i);
            newchild.hourAheadAuction =newHourAheadAuction;
            newchild.appliedAction = action.actionName;
            newchild.nobid = action.nobid;
            newchild.maxMult = action.maxMult;
            newchild.minMult = action.minMult;
            newchild.actionType = action.type;
            newchild.volPercentage = action.percentage;
            newchild.currentNodeCostAvg = 0.0;
            newchild.currentNodeCostLast = 0.0;
            newchild.actionName = action.actionName;
            newchild.dynamicState = action.dynamicAction;
            children.add(newchild);
        }
    }

    public TreeNode finalSelect(Observer ob) {
    	boolean printOn = false;
        TreeNode selected = null;
        double bestValue = Double.MAX_VALUE *-1;
        
        for (TreeNode c : children) {
         	double totlPoint = c.totValue;
         	double dividend = -Math.abs(ob.arrBalacingPrice[ob.currentTimeSlot]);
         	totlPoint = (1-(totlPoint/(dividend)));
         	
         	if(dividend == 0 || ob.neededEneryMCTSBroker <= 0.001)
         		totlPoint = 0;
         	
         	double visitPoint = Math.sqrt(2*Math.log(this.nVisits+1) / (c.nVisits + epsilon));
         	
         	double randPoint = r.nextDouble() * epsilon;
            double uctValue = totlPoint + visitPoint + randPoint;
         	// small random number to break ties randomly in unexpanded nodes
            if(printOn)
            	System.out.print("Action " + c.appliedAction + " NodeUCost " + c.totValue  + " childnvisit " + c.nVisits + " parentVisits " + this.nVisits + " totlPoint " + totlPoint + " nvisitPoint " + visitPoint + " UCTval = " + uctValue);
         	
         	if (totlPoint > bestValue) {
                selected = c;
                bestValue = totlPoint;
                if(printOn)
                	System.out.println(" [best] ");
            }
         	else{
         		if(printOn)
         			System.out.println("");
         	}
         	
        }

        if(true)
        	System.out.println("Seed " + ob.SEED + "["+ob.currentTimeSlot + "," + ob.hourAhead +"]"+" Selected : HourAhead " + selected.hourAheadAuction + " Action " + selected.appliedAction + " neededMWh " + ob.neededEneryMCTSBroker);
        
        return selected;
    }

    public double getMCTSValue(double mean, Observer ob) {
    	boolean printOn = false;
        TreeNode selected = null;
        double bestValue = Double.MAX_VALUE *-1;
        boolean flag = true;
        
        TreeNode cur = this;

    	if(cur == null || cur.children == null || cur.hourAheadAuction-1 == 0)
    		flag = false;
    	if(cur.children != null) {
	        for (TreeNode c : cur.children) {
	         	double totlPoint = c.totValue;// / ((c.nVisits) + epsilon);
	         	double dividend = -Math.abs(ob.arrBalacingPrice[ob.currentTimeSlot]);//*ob.neededEneryMCTSBroker);//(Configure.getPERHOURENERGYDEMAND()/Configure.getNUMBER_OF_BROKERS());
	         	totlPoint = (1-(totlPoint/(dividend)));
	         	
	         	if(dividend == 0 || ob.neededEneryMCTSBroker <= 0.001)
	         		totlPoint = 0;
	         	
	         	double visitPoint = Math.sqrt(2*Math.log(this.nVisits+1) / (c.nVisits + epsilon));
	         	
	         	double randPoint = r.nextDouble() * epsilon;
	            double uctValue = totlPoint + visitPoint + randPoint;
	         	// small random number to break ties randomly in unexpanded nodes
	            if(printOn)
	            	System.out.print("Action " + c.appliedAction + " Points " + c.totValue +" UCT value = " + uctValue + " totlPoint " + totlPoint + " nvisitPoint " + visitPoint + " c.nvisits " + c.nVisits + " totalVisits " + this.nVisits);
	         	
	         	if (totlPoint > bestValue) {
	                selected = c;
	                bestValue = totlPoint;
	                if(printOn)
	                	System.out.println(" [best] ");
	            }
	         	else{
	         		if(printOn)
	         			System.out.println("");
	         	}
	        }
	        // set the price to predicted price array inside the action
	        //action.predictions[selected.hourAheadAuction] = Math.abs(selected.totValue);
	        return Math.abs(selected.totValue);
    	}
	    
        return Math.abs(mean);
    }

    
    public TreeNode selectRandom(MCTS mcts, Observer observer) {
    	Random r = new Random();
    	int i = r.nextInt(mcts.actions.size()) + 0;
    	return children.get(i);
    }
    
    public TreeNode selectRandomUnvisited(MCTS mcts, Observer observer) {
    	for(TreeNode child : this.children)
    	{
    		if(child.nVisits == 0)
    			return child;
    	}
    	return null;
    }
    
    public TreeNode select(MCTS mcts, Observer observer, double neededEnergy) {
    	boolean printOn = false;
    	TreeNode nobidaction;
        TreeNode selected = null;
        int countLowerAuction = 0;
        boolean booNobid = false;
        
        if(Configure.getTHRESHOLDFILTER()) {
	        for(int jj=this.hourAheadAuction-1; jj>= 0; jj--){
				if(mcts.arrMctsPredClearingPrice[this.hourAheadAuction-1] > mcts.arrMctsPredClearingPrice[jj]){
					countLowerAuction++;
				}
			}
	        
	    	int thresholdLimit = this.hourAheadAuction * (mcts.thresholdLowAuctionsPerc/100);
	    	if(countLowerAuction > thresholdLimit)
	    		booNobid = true;
        }
        
        double bestValue = Double.MAX_VALUE *-1;
        for (TreeNode c : children) {
        	if(booNobid){
        		if(c.nobid) {
        			selected = c;
        			break;
        		}
        	}
        	else{
	        	double nVisitValue = 0;
	         	double totlPoint = 0;
	         	
	         	if(c.nVisits == 0){
	         		nVisitValue = 1 + epsilon;
	         		totlPoint = c.totValue;// / (1 + epsilon);
	         	}
	         	else{
	         		nVisitValue = c.nVisits + epsilon;
	         		totlPoint = c.totValue;// / ((c.nVisits) + epsilon);
	         	}
	         	
	         	double dividend = -Math.abs(observer.arrBalacingPrice[observer.currentTimeSlot]);//*neededEnergy);//(Configure.getPERHOURENERGYDEMAND()/Configure.getNUMBER_OF_BROKERS());
	         	
	         	totlPoint = (1-(totlPoint/(dividend)));
	         	
	         	if(dividend == 0 || neededEnergy <= 0.001)
	         		totlPoint = 0;
	         	
	         	double visitPoint = Math.sqrt(2*Math.log(nVisits+1) / nVisitValue);
	        	
	         	double randPoint = r.nextDouble() * epsilon;
	            double uctValue = totlPoint + visitPoint + randPoint; 
	            
	            // small random number to break ties randomly in unexpanded nodes
	            if(printOn)
	            	System.out.println("Action " + c.appliedAction + " UCT value = " + uctValue + " totlPoint " + totlPoint + " nvisitPoint " + visitPoint + " c.nvisits " + c.nVisits + " totalVisits " + nVisits);
	            
	            if (uctValue > bestValue) {
	                selected = c;
	                bestValue = uctValue;
	            }
	        }
     	}
        
        if(printOn){
        	System.out.println("HourAhead " + selected.hourAheadAuction + " Action " + selected.appliedAction);
        	System.out.println();
        }
        
        if(selected == null) {
        	System.out.println("bestValue " + bestValue + " Max " + (Double.MAX_VALUE *-1));
        }
        return selected;
    }

    public boolean isLeaf() {
        return hourAheadAuction == 0;
    }

    
    public double [] rollout(TreeNode tempNode, double [] arrPredClearingPrice, Observer ob, Double neededMWh, Double iniNeededEnergy, ArrayList<Action> actions, MCTS mcts) {
    	TreeNode tn = new TreeNode(tempNode);
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
		double totalBidVolume = 0.00;	
		double costValue = 0.00;
		double stddev = ob.STDDEV[tn.hourAheadAuction];
		while(true){
			
			double singleBidVolume = 0.00;
			if(!tn.nobid){
	    		// Bidding configuration
				double numberofbids = 1;
	    		double unitPriceIncrement = 1.00;
	    		double clearingPrice = Math.abs((r.nextGaussian()*stddev)+arrPredClearingPrice[tn.hourAheadAuction]);
	    		
	    		double limitPrice = 0;
	    		
	    		
    			if(tn.dynamicState)
    				limitPrice = tn.minMult + (tn.minMult*tn.maxMult);
    			else 	
    				limitPrice = arrPredClearingPrice[tn.hourAheadAuction]+(stddev*tn.maxMult);//tn.minmctsClearingPrice; // clearingPrice + (tn.minMult*7.8); //(Math.abs((r.nextGaussian()*stddev)+ 
	    		
	    		double maxPrice = limitPrice;//tn.minmctsClearingPrice; // clearingPrice + (tn.maxMult*7.8);
	    		double priceRange = 0;//tn.maxmctsClearingPrice - tn.minmctsClearingPrice;
	    		double minMWh = 1;
	    		
	    		unitPriceIncrement = priceRange / numberofbids;
	    		
	    		if(tn.actionType == ACTION_TYPE.BUY){
					// Buy energy
					double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
					double totalE = surplus + Math.abs(neededMWh);

					if(totalE > 0) {
						
						minMWh= Math.abs(totalE) / numberofbids;
			    		for(int i = 1; i <=numberofbids; i++){
			    			if(limitPrice >= clearingPrice) //arrPredClearingPrice[tn.hourAheadAuction]){
			    			{
			    				double tcp = (clearingPrice+limitPrice)/2;
			    				costValue+=minMWh*clearingPrice; // tcp;//
			    				totalBidVolume+=minMWh;
			    				singleBidVolume+=minMWh;
			    			}
			    			limitPrice+=unitPriceIncrement;
			    		}
					}
	    		}
	    		else{
	    			// Submit sell orders
					double surplus = 0 - neededMWh;
					minMWh= (Math.abs(iniNeededEnergy)*(1-tn.volPercentage));
					if(minMWh > surplus)
						minMWh /= numberofbids;
					else
						minMWh = surplus / numberofbids;
					
					for(int i = 1; i <=numberofbids; i++){
						if(limitPrice <= clearingPrice)//arrPredClearingPrice[tn.hourAheadAuction]){
						{
							double tcp = (clearingPrice+limitPrice)/2;
		    				costValue-=minMWh*clearingPrice;//arrPredClearingPrice[tn.hourAheadAuction];
		    				totalBidVolume-=minMWh;
		    				singleBidVolume-=minMWh;
		    			}
						limitPrice+=unitPriceIncrement;
					}
	    		}
	    		
	    		if(tn.hourAheadAuction == ob.hourAhead){
					tn.currentNodeCostLast = singleBidVolume * clearingPrice * -1;
				}
	     	}
	    	
			neededMWh -= singleBidVolume; 
	    	
	    	if(tn.hourAheadAuction == 0)
	    		break;
    		
	    	if(neededMWh==0)
				break;
	    	
	    	tn.expand(actions, mcts, ob, arrPredClearingPrice[tn.hourAheadAuction-1]);
    		tn = tn.selectRandom(mcts, ob);
	    }
		
    	return new double[] {totalBidVolume, costValue};
    }
    
    
    public double [] simulation(TreeNode tn, double [] arrPredClearingPrice, Observer ob, Double neededMWh, Double iniNeededEnergy) {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
		double totalBidVolume = 0.00;	
		double costValue = 0.00;
    	if(!tn.nobid){
    
    		// Bidding configuration
    		double numberofbids = 1;
    		double unitPriceIncrement = 1.00;
    		double stddev = ob.STDDEV[tn.hourAheadAuction];
    		double clearingPrice = Math.abs((r.nextGaussian()*stddev)+arrPredClearingPrice[tn.hourAheadAuction]);
    		double limitPrice = 0;
    		
			if(tn.dynamicState)
				limitPrice = tn.minMult + (tn.minMult*tn.maxMult);
			else 	
				limitPrice = arrPredClearingPrice[tn.hourAheadAuction]+(stddev*tn.maxMult); 
    		
    		double maxPrice = limitPrice;
    		double priceRange = maxPrice - limitPrice;
    		double minMWh = 1;
    		
    		
    		unitPriceIncrement = priceRange / numberofbids;
			
    		if(tn.actionType == ACTION_TYPE.BUY)
    		{
    			double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
				double totalE = surplus + Math.abs(neededMWh);

				if(totalE > 0) {
				
	    			minMWh= Math.abs(totalE) / numberofbids;
		    		for(int i = 1; i <=numberofbids; i++){
		    			if(limitPrice >= clearingPrice){
		    				double tcp = (clearingPrice+limitPrice)/2;
		    				costValue+=minMWh*clearingPrice;
		    				totalBidVolume+=minMWh;
		    			}
		    			limitPrice+=unitPriceIncrement;
		    		}
				}
    		}
    		else{
    			// Submit sell orders
				double surplus = 0 - neededMWh;
				minMWh= (Math.abs(iniNeededEnergy)*(1-tn.volPercentage));
				if(minMWh > surplus)
					minMWh /= numberofbids;
				else
					minMWh = surplus / numberofbids;
				
				minMWh /= Configure.getNUMBER_OF_PRODUCERS();
				
				for(int i = 1; i <=numberofbids; i++){
					if(limitPrice <= clearingPrice){
						costValue-=minMWh*clearingPrice;
	    				totalBidVolume-=minMWh;
	    			}
					limitPrice+=unitPriceIncrement;
				}
    		}
    		
    		if(tn.hourAheadAuction == ob.hourAhead){
				tn.currentNodeCostLast = totalBidVolume * clearingPrice * -1;
			}
    	}

    	return new double[] {totalBidVolume, costValue};
    }
    
    public void updateStats(double simCost, double balancingSimCost) {
    	totValue = ((totValue*this.nVisits) + simCost)/(this.nVisits+1);
    	currentNodeCostAvg = ((currentNodeCostAvg*this.nVisits) + currentNodeCostLast)/(this.nVisits+1);
        nVisits+=1;
    }

    public int arity() {
        return children == null ? 0 : children.size();
    }
    
    public ArrayList<TreeNode> getChildren(TreeNode root, int hourAhead, int count){
    	if(count == hourAhead){
    		return root.children;
    	}
    	return getChildren(root, hourAhead, count++);
    }
    
}