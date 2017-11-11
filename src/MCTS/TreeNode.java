package MCTS;
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
import MCTS.Action.ACTION_TYPE;
import Observer.Observer;

public class TreeNode {
    static Random r = new Random();
    //static int nActions = 15;
    static double epsilon = 1e-6;
    
    public Action.ACTION_TYPE actionType;
    
    TreeNode[] children;
    public double nVisits, 
	    totValue, 
	    minmctsClearingPrice,
	    maxmctsClearingPrice,
    	minMult,
    	maxMult,
    	volPercentage,
    	currentNodeCostAvg,
    	currentNodeCostLast;
    
    public String actionName;
    
    int hourAheadAuction;
    String	appliedAction;
    
    public boolean nobid = false;
    
    public TreeNode(){
    	
    }
    
    public TreeNode(int n, boolean nobid){
    	this.actionName = "" + n;
    	this.nobid = nobid;
    }
    
    public TreeNode(TreeNode tn){
    	this.nVisits = tn.nVisits;
    	this.totValue = tn.totValue;
    	this.currentNodeCostAvg = tn.currentNodeCostAvg;
    	this.currentNodeCostLast = tn.currentNodeCostLast;
    	this.minmctsClearingPrice = tn.minmctsClearingPrice;
    	this.maxmctsClearingPrice = tn.maxmctsClearingPrice;
    	this.minMult = tn.minMult;
    	this.maxMult = tn.maxMult;
    	this.hourAheadAuction = tn.hourAheadAuction;
    	this.appliedAction = tn.appliedAction;
    	this.nobid = tn.nobid;
    	this.actionType = tn.actionType;
    	this.volPercentage = tn.volPercentage;
    	this.actionName = tn.actionName;
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
    
    public void runMonteCarlo(ArrayList<Action> actions, MCTS mcts, Observer ob, boolean doubleMCTS) {
    	
    	double simCost = 0.0;
    	double neededEnergy = ob.neededEneryMCTSBroker;
    	double iniNeededEnergy = ob.initialNeededEneryMCTSBroker;
        List<TreeNode> visited = new LinkedList<TreeNode>();
        TreeNode cur = this;
        
        visited.add(this);
        
    	while (!cur.isLeaf()) {
    		//if(neededEnergy == 0)
    		//	break;
        	
        	if(cur.children == null){
        		cur.expand(actions, mcts, ob, mcts.arrMctsPredClearingPrice[this.hourAheadAuction-1]);
        	}
        	
        	//int unvisitedChildren = unvisitedChildren(cur);
    		TreeNode unvisitedNode = cur.selectRandomUnvisited(mcts, ob);
    		if(unvisitedNode != null){//actions.size()
    			mcts.debugCounter++;
    			// Initiate all 11 nodes
        		// select a random node
        		cur = unvisitedNode;//cur.selectRandomUnvisited(mcts, ob);
        		visited.add(cur);
        		// do the rollout
        		double [] retValue = rollout(cur, mcts.arrMctsPredClearingPrice, ob, neededEnergy, iniNeededEnergy, actions, mcts, doubleMCTS);
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
    			System.out.println("hi");
            // Do the simulation-rollout for wholesale auction
            double [] retValue = simulation(cur, mcts.arrMctsPredClearingPrice, ob, neededEnergy, iniNeededEnergy, doubleMCTS);
            neededEnergy -= retValue[0];
            simCost += retValue[1]*(-1);
            
            visited.add(cur);
        }
        
    	double balancingSimCost = Math.abs(neededEnergy)*ob.arrBalacingPrice[ob.currentTimeSlot]*(-1);
        simCost += balancingSimCost;

        // make the sim cost as unit cost
        simCost /= ob.neededEneryMCTSBroker;
        
        for (TreeNode node : visited) {
            // would need extra logic for n-player game
            // System.out.println(node);
            node.updateStats(simCost, balancingSimCost);
        }
        
    }

    public void expand(ArrayList<Action> actions, MCTS mcts, Observer ob, double mean) {
    	int nActions = actions.size();
    	children = new TreeNode[nActions];
    	int newHourAheadAuction = this.hourAheadAuction-1;
    	//double [] param = new double[15];
    	//param = ob.getFeatures(param, newHourAheadAuction);
		//double mean = ob.pricepredictor.getPrice(newHourAheadAuction);
		double stddev = ob.STDDEV[newHourAheadAuction];//7.8;
		
        for (int i=0; i<nActions; i++) {
            children[i] = new TreeNode();
            children[i].hourAheadAuction =newHourAheadAuction;
            
            double [] prices = actions.get(i).getAdjustedPrice(mean, stddev);
            children[i].minmctsClearingPrice = prices[0];
            children[i].maxmctsClearingPrice = prices[1];
            children[i].appliedAction = actions.get(i).actionName;
            children[i].nobid = actions.get(i).nobid;
            children[i].maxMult = actions.get(i).maxMult;
            children[i].minMult = actions.get(i).minMult;
            children[i].actionType = actions.get(i).type;
            children[i].volPercentage = actions.get(i).percentage;
            children[i].currentNodeCostAvg = 0.0;
            children[i].currentNodeCostLast = 0.0;
            children[i].actionName = actions.get(i).actionName;
        }
    }

    public TreeNode finalSelect(Observer ob) {
    	boolean printOn = false;
        TreeNode selected = null;
        double bestValue = Double.MAX_VALUE *-1;
        //if(children == null)
        	//System.out.println(ob.getTime());
        
        
        for (TreeNode c : children) {
         	double totlPoint = c.totValue;// / ((c.nVisits) + epsilon);
         	double dividend = -Math.abs(ob.arrBalacingPrice[ob.currentTimeSlot]);//*ob.neededEneryMCTSBroker);//(Configure.getPERHOURENERGYDEMAND()/Configure.getNUMBER_OF_BROKERS());
         	totlPoint = (1-(totlPoint/(dividend)));
         	
         	if(dividend == 0 || ob.neededEneryMCTSBroker <= 0.001)
         		totlPoint = 0;
         	
         	double visitPoint = Math.sqrt(2*Math.log(this.nVisits+1) / (c.nVisits + epsilon));
         	
         	double randPoint = r.nextDouble() * epsilon;
            double uctValue = totlPoint + visitPoint + randPoint;
         	// small random number to break ties randomly in unexpanded nodes
            // System.out.println("UCT value = " + uctValue);
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

        if(true)
        	System.out.println("Seed " + ob.SEED + "["+ob.currentTimeSlot + "," + ob.hourAhead +"]"+" Selected : HourAhead " + selected.hourAheadAuction + " Action " + selected.appliedAction + " neededMWh " + ob.neededEneryMCTSBroker);
        // System.out.println("Returning: " + selected);
        return selected;
    }

    public double getMCTSValue(double [] arrMctsPredClearingPrice, Observer ob) {
    	boolean printOn = false;
        TreeNode selected = null;
        double bestValue = Double.MAX_VALUE *-1;
        //if(children == null)
        	//System.out.println(ob.getTime());
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
	            // System.out.println("UCT value = " + uctValue);
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
	        
	        return Math.abs(selected.totValue);
        }
        	
        return arrMctsPredClearingPrice[ob.hourAhead]; 
    }

    
    public TreeNode selectRandom(MCTS mcts, Observer observer) {
    	Random r = new Random();
    	int i = r.nextInt(mcts.actions.size()) + 0;
    	return children[i];
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
        // System.out.println("Returning: " + selected);
        
        if(selected == null) {
        	System.out.println("bestValue " + bestValue + " Max " + (Double.MAX_VALUE *-1));
        }
        return selected;
    }

    public boolean isLeaf() {
        return hourAheadAuction == 0;
    }

    
    public double [] rollout(TreeNode tempNode, double [] arrPredClearingPrice, Observer ob, Double neededMWh, Double iniNeededEnergy, ArrayList<Action> actions, MCTS mcts, boolean doubleMCTS) {
    	TreeNode tn = new TreeNode(tempNode);
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
		double totalBidVolume = 0.00;	
		double costValue = 0.00;
		double stddev = ob.STDDEV[tn.hourAheadAuction];
		while(true){
//			if(neededMWh==0)
//				break;
			double singleBidVolume = 0.00;
			if(!tn.nobid){
	    		// Bidding configuration
				double numberofbids = 1;
	    		double unitPriceIncrement = 1.00;
	    		double clearingPrice = Math.abs((r.nextGaussian()*stddev)+arrPredClearingPrice[tn.hourAheadAuction]);
	    		double limitPrice = 0;
	    		
	    		if(doubleMCTS)
	    			limitPrice = tn.minMult + (tn.minMult*tn.maxMult);
	    		else
	    			limitPrice = arrPredClearingPrice[tn.hourAheadAuction]+(stddev*tn.minMult);//tn.minmctsClearingPrice; // clearingPrice + (tn.minMult*7.8); //(Math.abs((r.nextGaussian()*stddev)+ 
	    		
	    		double maxPrice = limitPrice;//tn.minmctsClearingPrice; // clearingPrice + (tn.maxMult*7.8);
	    		double priceRange = 0;//tn.maxmctsClearingPrice - tn.minmctsClearingPrice;
	    		double minMWh = 1;
	    		
	    		unitPriceIncrement = priceRange / numberofbids;
	    		
	    		if(tn.actionType == ACTION_TYPE.BUY){
					// Buy energy
	    			//double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
					//double totalE = surplus + Math.abs(neededMWh);
					
					double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
					double totalE = surplus + Math.abs(neededMWh);

					if(totalE > 0) {
						
						minMWh= Math.abs(totalE) / numberofbids;
			    		for(int i = 1; i <=numberofbids; i++){
			    			if(limitPrice >= clearingPrice) //arrPredClearingPrice[tn.hourAheadAuction]){
			    			{
			    				double tcp = (clearingPrice+limitPrice)/2;
			    				//if(limitPrice >= ob.arrPredictedClearingPrices[ob.currentTimeSlot][tn.hourAheadAuction]){
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
	    	
			neededMWh -= singleBidVolume; 
	    	
	    	if(tn.hourAheadAuction == 0)
	    		break;
    		
	    	tn.expand(actions, mcts, ob, arrPredClearingPrice[tn.hourAheadAuction-1]);
    		tn = tn.selectRandom(mcts, ob);
	    }
		
    	return new double[] {totalBidVolume, costValue}; //totalBidVolume;
    }
    
    
    public double [] simulation(TreeNode tn, double [] arrPredClearingPrice, Observer ob, Double neededMWh, Double iniNeededEnergy, boolean doubleMCTS) {
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
    		
    		if(doubleMCTS)
    			limitPrice = tn.minMult + (tn.minMult*tn.maxMult);
    		else
    			limitPrice = arrPredClearingPrice[tn.hourAheadAuction]+(stddev*tn.minMult);//tn.minmctsClearingPrice; // clearingPrice + (tn.minMult*7.8); //(Math.abs((r.nextGaussian()*stddev)+ 
    		
    		
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

    	return new double[] {totalBidVolume, costValue}; //totalBidVolume;
    }
    
    public void updateStats(double simCost, double balancingSimCost) {
    	totValue = ((totValue*this.nVisits) + simCost)/(this.nVisits+1);
    	currentNodeCostAvg = ((currentNodeCostAvg*this.nVisits) + currentNodeCostLast)/(this.nVisits+1);
        nVisits+=1;
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }
    
    public TreeNode [] getChildren(TreeNode root, int hourAhead, int count){
    	if(count == hourAhead){
    		return root.children;
    	}
    	return getChildren(root, hourAhead, count++);
    }
    
}