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
    
    int hourAheadAuction,
    	appliedAction;
    
    public boolean nobid = false;
    
    public TreeNode(){
    	
    }
    
    public TreeNode(boolean nobid){
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
    
    public void runMonteCarlo(ArrayList<Action> actions, MCTS mcts, Observer ob) {
    	
    	double simCost = 0.0;
    	Double neededEnergy = ob.neededEneryMCTSBroker;
    	Double iniNeededEnergy = ob.initialNeededEneryMCTSBroker;
        List<TreeNode> visited = new LinkedList<TreeNode>();
        TreeNode cur = this;
        
        visited.add(this);
        
    	while (!cur.isLeaf()) {
    		if(neededEnergy == 0)
    			break;
        	
        	if(cur.children == null){
        		cur.expand(actions, mcts, ob);
        	}
        	
        	int unvisitedChildren = unvisitedChildren(cur);
    		
    		if(unvisitedChildren == actions.size()){
    			// Initiate all 11 nodes
        		// select a random node
        		cur = cur.selectRandom(mcts, ob);
        		visited.add(cur);
        		// do the rollout
        		double [] retValue = rollout(cur, mcts.arrMctsPredClearingPrice, ob, neededEnergy, iniNeededEnergy, actions, mcts);
        		// add to the sim cost
        		neededEnergy -= retValue[0];
                simCost += retValue[1]*(-1);
                break;
        	}
    		
    		cur = cur.select(mcts, ob);
            // Do the simulation-rollout for wholesale auction
            double [] retValue = simulation(cur, mcts.arrMctsPredClearingPrice, ob, neededEnergy, iniNeededEnergy);
            neededEnergy -= retValue[0];
            simCost += retValue[1]*(-1);
            
            visited.add(cur);
        }
        
    	double balancingSimCost = neededEnergy*ob.arrBalacingPrice[ob.currentTimeSlot]*(-1);
        simCost += balancingSimCost;

        for (TreeNode node : visited) {
            // would need extra logic for n-player game
            // System.out.println(node);
            node.updateStats(simCost, balancingSimCost);
        }
    }

    public void expand(ArrayList<Action> actions, MCTS mcts, Observer ob) {
    	int nActions = actions.size();
    	children = new TreeNode[nActions];
    	int newHourAheadAuction = this.hourAheadAuction-1;
        for (int i=0; i<nActions; i++) {
            children[i] = new TreeNode();
            children[i].hourAheadAuction =newHourAheadAuction;
            
            double [] param = new double[11];
	    	param = ob.getFeatures(param);
			param[2] = newHourAheadAuction;
			double mean = mcts.pricePredictor.getLimitPrice(param);
			double stddev = 7.8;
			double [] prices = actions.get(i).getAdjustedPrice(mean, stddev);
            children[i].minmctsClearingPrice = prices[0];
            children[i].maxmctsClearingPrice = prices[1];
            children[i].appliedAction = i;
            children[i].nobid = actions.get(i).nobid;
            children[i].maxMult = actions.get(i).maxMult;
            children[i].minMult = actions.get(i).minMult;
            children[i].actionType = actions.get(i).type;
            children[i].volPercentage = actions.get(i).percentage;
            children[i].currentNodeCostAvg = 0.0;
            children[i].currentNodeCostLast = 0.0;
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
         	double dividend = -ob.arrBalacingPrice[ob.currentTimeSlot]*(Configure.getPERHOURENERGYDEMAND()/Configure.getNUMBER_OF_BROKERS());
         	totlPoint = (1-(totlPoint/(dividend)));
         	
         	double visitPoint = Math.sqrt(2*Math.log(this.nVisits+1) / (c.nVisits + epsilon));
         	
         	double randPoint = r.nextDouble() * epsilon;
            double uctValue = totlPoint + visitPoint + randPoint;
         	// small random number to break ties randomly in unexpanded nodes
            // System.out.println("UCT value = " + uctValue);
         	if(printOn)
            	System.out.print("Action " + c.appliedAction + " UCT value = " + uctValue + " totlPoint " + totlPoint + " nvisitPoint " + visitPoint + " c.nvisits " + c.nVisits + " totalVisits " + this.nVisits);
         	
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
        if(printOn)
        	System.out.println("["+ob.currentTimeSlot + "," + ob.hourAhead +"]"+" Selected : HourAhead " + selected.hourAheadAuction + " Action " + selected.appliedAction + "\n");
        // System.out.println("Returning: " + selected);
        return selected;
    }

    public TreeNode selectRandom(MCTS mcts, Observer observer) {
    	Random r = new Random();
    	int i = r.nextInt(mcts.actions.size()) + 0;
    	return children[i];
    }
    public TreeNode select(MCTS mcts, Observer observer) {
    	boolean printOn = false;
        TreeNode selected = null;
        int countLowerAuction = 0;
        boolean booNobid = false;
        for(int jj=this.hourAheadAuction-1; jj>= 0; jj--){
			if(mcts.arrMctsPredClearingPrice[this.hourAheadAuction-1] > mcts.arrMctsPredClearingPrice[jj]){
				countLowerAuction++;
			}
		}
    	
    	int thresholdLimit = this.hourAheadAuction * (mcts.thresholdLowAuctionsPerc/100);
    	if(countLowerAuction > thresholdLimit)
    		booNobid = false;
        
        double bestValue = Double.MAX_VALUE *-1;
        for (TreeNode c : children) {
        	if(booNobid){
        		if(c.nobid)
        			selected = c;
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
	         	
	         	double dividend = -observer.arrBalacingPrice[observer.currentTimeSlot]*(Configure.getPERHOURENERGYDEMAND()/Configure.getNUMBER_OF_BROKERS());
	         	
	         	totlPoint = (1-(totlPoint/(dividend)));
	     
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
		while(true){
			if(neededMWh==0)
				break;
			double singleBidVolume = 0.00;
			if(!tn.nobid){
	    		// Bidding configuration
				double numberofbids = 10;
	    		double unitPriceIncrement = 1.00;
	    		double clearingPrice = Math.abs((r.nextGaussian()*7.8)+arrPredClearingPrice[tn.hourAheadAuction]);
	    		double limitPrice = tn.minmctsClearingPrice; //clearingPrice + (tn.minMult*7.8);
	    		double maxPrice = tn.minmctsClearingPrice; // clearingPrice + (tn.maxMult*7.8); 
	    		double priceRange = tn.maxmctsClearingPrice - tn.minmctsClearingPrice;
	    		double minMWh = 1;
	    		
	    		unitPriceIncrement = priceRange / numberofbids;
	    		
	    		if(tn.actionType == ACTION_TYPE.BUY){
					// Buy energy
	    			double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
					double totalE = surplus + (Math.abs(neededMWh)/5);
					
	    			minMWh= Math.abs(totalE) / numberofbids;
		    		for(int i = 1; i <=numberofbids; i++){
		    			if(limitPrice >= clearingPrice) //arrPredClearingPrice[tn.hourAheadAuction]){
		    			{
		    				//if(limitPrice >= ob.arrPredictedClearingPrices[ob.currentTimeSlot][tn.hourAheadAuction]){
		    				costValue+=minMWh*clearingPrice;//arrPredClearingPrice[tn.hourAheadAuction];
		    				totalBidVolume+=minMWh;
		    				singleBidVolume+=minMWh;
		    			}
		    			limitPrice+=unitPriceIncrement;
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
    		
	    	tn.expand(actions, mcts, ob);
    		tn = tn.selectRandom(mcts, ob);
	    }
		
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
    		double numberofbids = 10;
    		double unitPriceIncrement = 1.00;
    		double clearingPrice = Math.abs((r.nextGaussian()*7.8)+arrPredClearingPrice[tn.hourAheadAuction]);//arrPredClearingPrice[tn.hourAheadAuction];
    		double limitPrice = tn.minmctsClearingPrice; // clearingPrice + (tn.minMult*7.8); //
    		double maxPrice = tn.minmctsClearingPrice; // clearingPrice + (tn.maxMult*7.8);
    		double priceRange = maxPrice - limitPrice; // tn.maxmctsClearingPrice - tn.minmctsClearingPrice;
    		double minMWh = 1;
    		
    		
    		unitPriceIncrement = priceRange / numberofbids;
			//double clearingPrice = Math.abs((r.nextGaussian()*7.8)+arrPredClearingPrice[tn.hourAheadAuction]);
    		
    		if(tn.actionType == ACTION_TYPE.BUY)
    		{
    			double surplus = Math.abs(ob.initialNeededEneryMCTSBroker)*(tn.volPercentage-1);
				double totalE = surplus + (Math.abs(neededMWh)/Configure.getNUMBER_OF_BROKERS());
				
    			minMWh= Math.abs(totalE) / numberofbids;
	    		for(int i = 1; i <=numberofbids; i++){
	    			if(limitPrice >= clearingPrice){
	    			//if(limitPrice >= arrPredClearingPrice[tn.hourAheadAuction]){
	    			//if(limitPrice >= ob.arrPredictedClearingPrices[ob.currentTimeSlot][tn.hourAheadAuction]){
	    				costValue+=minMWh*clearingPrice;
	    				totalBidVolume+=minMWh;
	    			}
	    			limitPrice+=unitPriceIncrement;
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