package MCTS_Kernel;
/**

 * Created by Moinul Morshed Porag Chowdhury
 * mchowdhury4@miners.utep.edu
 * Date: 19-March-2016
 * Time: 21:51:31
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import configure.Configure;
import Auctioneer.Ask;
import Auctioneer.Bid;
import MCTS_Kernel.Action.ACTION_TYPE;
import Observer.Observer;

public class TreeNode {
    static Random r = new Random();
    //static int nActions = 15;
    static double epsilon = 1e-6;
    
    public Action.ACTION_TYPE actionType;
    
    public double kernel [][][];
    public double limitprices [];
    //TreeNode[] children;
    ArrayList<TreeNode> children;
    public TreeNode parent;
    public double nVisits,
    	ntotValueVisits,
	    totValue, 
//	    minmctsClearingPrice,
//	    maxmctsClearingPrice,
    	minMult,
    	maxMult,
    	volPercentage,
    	currentNodeCostAvg,
    	currentNodeCostLast;
    
    public boolean dynamicState = false;
    
    public int actionName;
    
    int hourAheadAuction;
    int	appliedAction;
    int actionsize;
    public boolean nobid = false;
    
    public TreeNode(double pprice, ArrayList<Action> actions, double stddev, int newactions){
    	this.limitprices = new double[20];
    	this.kernel = new double[20][20][4];
    	this.actionsize = actions.size() + newactions;
    	for(Action a:actions) {
    		if(a.dynamicAction)
    			this.limitprices[a.actionName] = a.minMult;
    		else
    			this.limitprices[a.actionName] = pprice + (stddev * a.minMult);
    	}
    }

    public TreeNode(TreeNode tn, int newactions){
    	this.nVisits = tn.nVisits;
    	this.actionsize = tn.actionsize;
    	this.ntotValueVisits = tn.ntotValueVisits; 
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
    	this.parent = tn.parent;
    	this.limitprices = new double[20];
    	this.kernel = new double[20][20][4];
    	int asize = this.actionsize + newactions;
    	for(int i = 0; i < asize; i++) {
    		this.limitprices[i] = tn.limitprices[i];
    		for(int j = 0; j < asize; j++) {
    			this.kernel[i][j][0] = tn.kernel[i][j][0]; // Successful bid accepted
    			this.kernel[i][j][1] = tn.kernel[i][j][1]; // Total Visits for this item
    			this.kernel[i][j][2] = tn.kernel[i][j][2]; // TotalPoints
    			this.kernel[i][j][3] = tn.kernel[i][j][3]; // TotalVisits
    		}
    	}
    	
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
    
    public void runMonteCarlo(ArrayList<Action> actions, MCTS_Kernel mcts, Observer ob, int sims) {
    	
    	double simCost = 0.0;
    	double neededEnergy = ob.neededEneryMCTSBroker;
    	double iniNeededEnergy = ob.initialNeededEneryMCTSBroker;
        List<TreeNode> visited = new LinkedList<TreeNode>();
        TreeNode cur = this;
        
        visited.add(this);
        //System.out.println("Sim number: " + sims);
        // add dynamic action space logic
        if(mcts.thresholdcount < 4 && sims > mcts.thresholdMCTS[mcts.thresholdcount]) {
        	int actionsize = actions.size();
        	double pmctsprice = cur.getMCTSValue(mcts.arrMctsPredClearingPrice[ob.hourAhead],ob);
        	// ACTION: ACT_NO, MIN_PRC, MAX_PRC, booNOBID, ACT_TYP, PERC, booDYN_ACT
        	Action action = new Action(actionsize,pmctsprice,0,false, Action.ACTION_TYPE.BUY, 1.00, true);
        	actions.add(action);	
        	mcts.thresholdcount++;
        }
        
        int actionsize = actions.size();
        
    	while (!cur.isLeaf()) {
    		//if(neededEnergy == 0)
    		//	break;
        	
        	if(cur.children == null){
        		cur.expand(actions, mcts, ob, mcts.arrMctsPredClearingPrice[this.hourAheadAuction-1]);
        	}
        	
        	TreeNode unvisitedNode = cur.selectRandomUnvisited(mcts, ob);
    		if(unvisitedNode != null){
    			mcts.debugCounter++;
    			// Initiate all 11 nodes
        		// select a random node
    			cur = unvisitedNode;//cur.selectRandomUnvisited(mcts, ob);
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
    			
    			// check if root
    			if(cur.parent == null) {
    				cur.nVisits = actionsize;
    			}
    			
				TreeNode tempcur = cur;
				while(tempcur.parent != null) {
					tempcur.parent.nVisits += actionsize;
					tempcur.nVisits = 0;
					tempcur = tempcur.parent;
				}
    			
    			// Reset the exploration count to 1 for all children
    			//for(TreeNode child : cur.children)
    			//	child.nVisits = 0;
    			// add a new child :: EXPAND
				
    			for(int i = childrensize; i < actionsize; i++) {
	    			Action action = actions.get(i);
	    			TreeNode newchild = new TreeNode(mcts.arrMctsPredClearingPrice[cur.hourAheadAuction-1], actions, ob.STDDEV[cur.hourAheadAuction-1], mcts.thresholdcount);
	        		newchild.parent = cur;
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
        	
//    		if(mcts.debugCounter>1)
//	    		System.out.println("debugCounter" + mcts.debugCounter);
	    	
    		mcts.debugCounter=0;
    		cur = cur.select(mcts, ob, neededEnergy);
    		
    		if(cur == null)
    			System.out.println("hi, its null");
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
            // System.out.println(node);
            node.updateStats(simCost, balancingSimCost);
        }
        
    }

    public void expand(ArrayList<Action> actions, MCTS_Kernel mcts, Observer ob, double mean) {
    	int nActions = actions.size();
    	children = new ArrayList<TreeNode>();
    	
    	int newHourAheadAuction = this.hourAheadAuction-1;
		
        for (int i=0; i<nActions; i++) {
            TreeNode newchild = new TreeNode(mcts.arrMctsPredClearingPrice[newHourAheadAuction], actions, ob.STDDEV[newHourAheadAuction], mcts.thresholdcount);
            Action action = actions.get(i);
            newchild.hourAheadAuction =newHourAheadAuction;
            newchild.parent = this;
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
            newchild.nVisits = 0;
            newchild.ntotValueVisits = 0;
            children.add(newchild);
        }
    }

    public TreeNode finalSelect(Observer ob) {
    	boolean printOn = false;
        TreeNode selected = null;
        double bestValue = Double.MAX_VALUE *-1;
        //if(children == null)
        	//System.out.println(ob.getTime());
        
        //printKernel();
        
        double kernelValue [] = new double[20];
     	double kernelDensity [] = new double[20];
     	double kernelW = 0;
     	double dividend = -Math.abs(ob.arrBalacingPrice[ob.currentTimeSlot]);
     	double [] kernelDividend = new double[20]; 
     	//double totalP = 0;
     	for(int i = 0; i < this.children.size(); i++) {
     		for(int e = 0; e < this.children.size(); e++) {
     			double d = kernel[i][e][1];
     			if(d == 0) {
     				d = 1;
     			}
     			kernelValue[i] += (kernel[i][e][0]/d) * kernel[e][0][2] * kernel[e][0][3]; 
     			kernelDividend[i] += (kernel[i][e][0]/d) * dividend * kernel[e][0][3]; 
     			kernelDensity[i] += (kernel[i][e][0]/d) * kernel[e][0][3]; 
     		}
     		
     		if(dividend == 0 || kernelDividend[i] == 0 || ob.neededEneryMCTSBroker <= 0.001) {
 				kernelValue[i] = 0;
 				kernelDividend[i] = 1;
 			}
     		kernelValue[i] = (1- kernelValue[i]/kernelDividend[i]);
 			kernelW += kernelDensity[i];
     	}
        
     	for(int i = 0; i < this.children.size(); i++) {
     		TreeNode c = children.get(i);
     		if(kernelDensity[i] == 0)
     			kernelDensity[i] = 1;
         	double visitPoint = (1/4)*Math.sqrt(2*Math.log(kernelW) / kernelDensity[i]);
     		double randPoint = r.nextDouble() * epsilon;
     		//System.out.println("value: " + kernelValue[i] + " points: " + visitPoint);
            double uctValue = kernelValue[i] + visitPoint + randPoint; 
     		
            if(printOn)
            	System.out.print("Action " + c.appliedAction + " NodeUCost " + c.totValue  + " childnvisit " + c.nVisits + " parentVisits " + this.nVisits + " totlPoint " + kernelValue[i] + " nvisitPoint " + visitPoint + " UCTval = " + uctValue);
         	
            if (uctValue > bestValue) {
                selected = this.children.get(i);
                bestValue = uctValue;
	            if(printOn)
	            	System.out.println(" [best] ");
	        }
	     	else{
	     		if(printOn)
	     			System.out.println("");
	     	}
     	}
     	
     	if(selected == null) {
     		System.out.println("Pick Random");
     		Random ran = new Random();
     		int x = ran.nextInt(this.children.size()) + 0;
     		selected = children.get(x);
     	}

     	if(true)
        	System.out.println("Seed " + ob.SEED + "["+ob.currentTimeSlot + "," + ob.hourAhead +"]"+" Selected : HourAhead " + selected.hourAheadAuction + " Action " + selected.appliedAction + " neededMWh " + ob.neededEneryMCTSBroker);
     	
     	return selected;
    }

    public double getMCTSValue(double mean, Observer ob) {
    	//int actionsize = actions.size();
    	//Action action = new Action(actionsize,0,0,false, Action.ACTION_TYPE.BUY, 1.00, true);
    	boolean printOn = false;
        TreeNode selected = null;
        double bestValue = Double.MAX_VALUE *-1;
        //if(children == null)
        	//System.out.println(ob.getTime());
        TreeNode cur = this;

    	if(cur.children != null) {
	        for (TreeNode c : cur.children) {
	         	double totlPoint = c.totValue;// / ((c.nVisits) + epsilon);
	         	double dividend = -Math.abs(ob.arrBalacingPrice[ob.currentTimeSlot]);//*ob.neededEneryMCTSBroker);//(Configure.getPERHOURENERGYDEMAND()/Configure.getNUMBER_OF_BROKERS());
	         	totlPoint = (1-(totlPoint/(dividend)));
	         	
//		         	if(dividend == 0 || ob.neededEneryMCTSBroker <= 0.001)
//		         		totlPoint = 0;
	         	
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

    
    public TreeNode selectRandom(MCTS_Kernel mcts, Observer observer) {
    	Random r = new Random();
    	int i = r.nextInt(mcts.actions.size()) + 0;
    	return children.get(i);
    }
    
    public TreeNode selectRandomUnvisited(MCTS_Kernel mcts, Observer observer) {
    	for(TreeNode child : this.children)
    	{
    		if(child.nVisits == 0)
    			return child;
    	}
    	return null;
    }
    
    public TreeNode select(MCTS_Kernel mcts, Observer observer, double neededEnergy) {
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
        
        double kernelValue [] = new double[20];
     	double kernelDensity [] = new double[20];
     	double kernelW = 0;
     	double dividend = -Math.abs(observer.arrBalacingPrice[observer.currentTimeSlot]);
     	double [] kernelDividend = new double[20]; 
     	//double totalP = 0;
     	for(int i = 0; i < this.children.size(); i++) {
     		for(int e = 0; e < this.children.size(); e++) {
     			double d = kernel[i][e][1];
     			if(d == 0) {
     				d = 1;
     			}
     			kernelValue[i] += (kernel[i][e][0]/d) * kernel[e][0][2] * kernel[e][0][3]; 
     			kernelDividend[i] += (kernel[i][e][0]/d) * dividend * kernel[e][0][3]; 
     			kernelDensity[i] += (kernel[i][e][0]/d) * kernel[e][0][3]; 
     		}
     		
     		if(dividend == 0 || kernelDividend[i] == 0 || neededEnergy <= 0.001) {
 				kernelValue[i] = 0;
 				kernelDividend[i] = 1;
 			}
     		kernelValue[i] = (1- kernelValue[i]/kernelDividend[i]);
 			kernelW += kernelDensity[i];
     	}
     	
     	for(int i = 0; i < this.children.size(); i++) {
     		if(kernelDensity[i] == 0)
     			kernelDensity[i] = 1;
         	double visitPoint = (1/4)*Math.sqrt(2*Math.log(kernelW) / kernelDensity[i]);
     		double randPoint = r.nextDouble() * epsilon;
     		//System.out.println("value: " + kernelValue[i] + " points: " + visitPoint);
            double uctValue = kernelValue[i] + visitPoint + randPoint; 
     		
            if (uctValue > bestValue) {
                selected = this.children.get(i);
                bestValue = uctValue;
            }
     	}
     	
     	if(selected == null) {
     		System.out.println("Pick Random");
     		Random ran = new Random();
     		int x = ran.nextInt(this.children.size()) + 0;
     		selected = children.get(x);
     	}
     	
     	return selected;
    }

    public boolean isLeaf() {
        return hourAheadAuction == 0;
    }

    
    public double [] rollout(TreeNode tempNode, double [] arrPredClearingPrice, Observer ob, Double neededMWh, Double iniNeededEnergy, ArrayList<Action> actions, MCTS_Kernel mcts) {
    	boolean updateKernel = true;
    	TreeNode tn = new TreeNode(tempNode, mcts.thresholdcount);
    	
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
	    		// Drawing a clearing price
	    		double clearingPrice = Math.abs((r.nextGaussian()*stddev)+arrPredClearingPrice[tn.hourAheadAuction]);
	    		
	    		double limitPrice = 0;
	    		
	    		
    			if(tn.dynamicState)
    				limitPrice = tn.minMult + (tn.minMult*tn.maxMult);
    			else 	
    				limitPrice = arrPredClearingPrice[tn.hourAheadAuction]+(stddev*tn.maxMult); 
	    		
	    		double maxPrice = limitPrice;
	    		double priceRange = 0;
	    		double minMWh = 1;
	    		
	    		unitPriceIncrement = priceRange / numberofbids;
	    		
	    		if(tn.actionType == ACTION_TYPE.BUY){
					// Buy energy
	    			double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
					double totalE = surplus + Math.abs(neededMWh);

					if(totalE > 0) {
						int count = 0;
						minMWh= Math.abs(totalE) / numberofbids;
			    		for(int i = 1; i <=numberofbids; i++){
			    			if(limitPrice >= clearingPrice) //arrPredClearingPrice[tn.hourAheadAuction]){
			    			{
			    				double tcp = (clearingPrice+limitPrice)/2;
			    				//if(limitPrice >= ob.arrPredictedClearingPrices[ob.currentTimeSlot][tn.hourAheadAuction]){
			    				costValue+=minMWh*clearingPrice; // tcp;//
			    				totalBidVolume+=minMWh;
			    				singleBidVolume+=minMWh;
			    				count++;
			    			}
			    			limitPrice+=unitPriceIncrement;
			    		}
			    		
			    		if(updateKernel)
			    		{
			    			for(int i = 0; i < tempNode.parent.children.size(); i++) {
			    				if(tempNode.limitprices[i] >= clearingPrice)
			    					tempNode.parent.kernel[i][tempNode.actionName][0]++;
			    				
								tempNode.parent.kernel[i][tempNode.actionName][1]++;
							}
						}
			    		updateKernel = false;
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
							//if(limitPrice >= ob.arrPredictedClearingPrices[ob.currentTimeSlot][tn.hourAheadAuction]){
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

			// update kernel for NOBID
			if(updateKernel && tempNode.nobid)
			{
				for(int i = 0; i < tempNode.parent.children.size(); i++) {
					tempNode.parent.kernel[i][tempNode.actionName][0]++;
					tempNode.parent.kernel[i][tempNode.actionName][1]++;
				}
				updateKernel = false;
			}	
	    	
			neededMWh -= singleBidVolume; 
	    	
	    	if(tn.hourAheadAuction == 0)
	    		break;
    		
	    	if(neededMWh==0)
				break;
	    	
	    	tn.expand(actions, mcts, ob, arrPredClearingPrice[tn.hourAheadAuction-1]);
    		tn = tn.selectRandom(mcts, ob);
	    }
		
//		if(totalBidVolume != 0)
//			costValue /= totalBidVolume;
		
    	return new double[] {totalBidVolume, costValue}; //totalBidVolume;
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
    		double clearingPrice = Math.abs((r.nextGaussian()*stddev)+arrPredClearingPrice[tn.hourAheadAuction]);//arrPredClearingPrice[tn.hourAheadAuction];
    		double limitPrice = 0;//arrPredClearingPrice[tn.hourAheadAuction]+(stddev*tn.minMult);//tn.minmctsClearingPrice; // clearingPrice + (tn.minMult*7.8); //(Math.abs((r.nextGaussian()*stddev)+
    		
			if(tn.dynamicState)
				limitPrice = tn.minMult + (tn.minMult*tn.maxMult);
			else 	
				limitPrice = arrPredClearingPrice[tn.hourAheadAuction]+(stddev*tn.maxMult);//tn.minmctsClearingPrice; // clearingPrice + (tn.minMult*7.8); //(Math.abs((r.nextGaussian()*stddev)+ 
    		
    		double maxPrice = limitPrice;//tn.minmctsClearingPrice; // clearingPrice + (tn.maxMult*7.8);
    		double priceRange = maxPrice - limitPrice; // tn.maxmctsClearingPrice - tn.minmctsClearingPrice;
    		double minMWh = 1;
    		
    		
    		unitPriceIncrement = priceRange / numberofbids;
			//double clearingPrice = Math.abs((r.nextGaussian()*7.8)+arrPredClearingPrice[tn.hourAheadAuction]);
    		
    		if(tn.actionType == ACTION_TYPE.BUY)
    		{
    			//double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
				//double totalE = surplus + Math.abs(neededMWh);
				
				double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);

				double totalE = surplus + Math.abs(neededMWh);

				if(totalE > 0) {
				
	    			minMWh= Math.abs(totalE) / numberofbids;
		    		for(int i = 1; i <=numberofbids; i++){
		    			if(limitPrice >= clearingPrice){
		    				double tcp = (clearingPrice+limitPrice)/2;
		    			//if(limitPrice >= arrPredClearingPrice[tn.hourAheadAuction]){
		    			//if(limitPrice >= ob.arrPredictedClearingPrices[ob.currentTimeSlot][tn.hourAheadAuction]){
		    				costValue+=minMWh*clearingPrice;
		    				totalBidVolume+=minMWh;
		    			}
		    			limitPrice+=unitPriceIncrement;
		    		}
		    		
	    			for(int i = 0; i < tn.parent.children.size(); i++) {
	    				if(tn.limitprices[i] >= clearingPrice)
	    					tn.parent.kernel[i][tn.actionName][0]++;
	    				
						tn.parent.kernel[i][tn.actionName][1]++;
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
					//if(limitPrice >= arrPredClearingPrice[tn.hourAheadAuction]){
					if(limitPrice <= clearingPrice){
						//if(limitPrice >= ob.arrPredictedClearingPrices[ob.currentTimeSlot][tn.hourAheadAuction]){
						//double clearingPrice = Math.abs((r.nextGaussian()*7.8)+arrPredClearingPrice[tn.hourAheadAuction]);
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
    	else {
    		// NO BID
			for(int i = 0; i < tn.parent.children.size(); i++) {
				tn.parent.kernel[i][tn.actionName][0]++;
				tn.parent.kernel[i][tn.actionName][1]++;
			}
    	}

//    	if(totalBidVolume != 0)
//			costValue /= totalBidVolume;
    	
    	return new double[] {totalBidVolume, costValue}; //totalBidVolume;
    }
    
    public void updateStats(double simCost, double balancingSimCost) {
    	totValue = ((totValue*this.ntotValueVisits) + simCost)/(this.ntotValueVisits+1);
    	currentNodeCostAvg = ((currentNodeCostAvg*this.ntotValueVisits) + currentNodeCostLast)/(this.ntotValueVisits+1);
        nVisits+=1;
        ntotValueVisits+=1;
        if(parent!=null) {
	        parent.kernel[actionName][0][2] = totValue;
	        parent.kernel[actionName][0][3] = nVisits;
        }
        
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
    
    public void printKernel() {
    	for(int i = 0; i < children.size(); i++) {
    		for(int j = 0; j < children.size(); j++) {
    			System.out.print("" + kernel[i][j][0] + "/" + kernel[i][j][1] + " ");
    		}
    		System.out.println();	
    	}
    	
    }
    
}