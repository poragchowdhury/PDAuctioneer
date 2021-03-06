package Agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;
import Observer.PricePredictor;
import Observer.Utility;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import configure.Configure;

public class C2 extends Agent {
	public int CP = 0;
	public int PCP = 1;
	public int Pr = 2;
	public int HA = 3;
	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	public PricePredictor pricePredictor;
	public double MIN_PR = 0.025;
	public double newMIN_PR = 0.005;
	public double MAX_PR = 0.975;
	public double newMAX_PR = 0.998;
	public double [] probability = {0.025, 0.069, 0.16, 0.30, 0.50, 0.69, 0.84, 0.932, 0.975};
	public double [] sigma = {-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2};
	public Utility utility;
	double totalLayerInput;
	double [] normalized;

	public C2(String name, int id, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.BROKER;
		pricePredictor = new PricePredictor(Agent.predictorName);
		utility = new Utility();
	}

	// Not working well: -586875.85
	public double parametricC2(Observer ob){
		// Brand new
		double [] newP = new double[ob.hourAhead+1];
		double threshold = 1.0;
		double newMax_PR = MAX_PR;
		for(int i = 0; i < newP.length; i++) {
			newP[i] = MIN_PR;
			if(i == 0)
				threshold *= MAX_PR;
			else
				threshold *= MIN_PR;
		}

		double totP = Double.MIN_VALUE;

		int lastCounter = 0;
		double newMAX_PR = MAX_PR-0.1;
		while(totP < threshold) {
			totP = 1;
			for(int i = 0; i < newP.length; i++) {
				totP *= newP[i];
			}

			if(totP < threshold)
			{
				// update newP[lastIndex]
				if(newP[lastCounter] >= newMAX_PR) {
					if((newMAX_PR-0.3)>MIN_PR)
						newMAX_PR -= 0.3;
					lastCounter++;
				}

				if(lastCounter >= newP.length)
					break;

				newP[lastCounter]+=MIN_PR;
			}
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
		double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
		double z = utility.calc_q(newP[index]);
		if(newP[index] < 0.5)
			z *= -1;
		System.out.println("Pr: " + newP[index] + " z: " + z);
		C1limitPrice = Math.abs(limitPrice+z*7.8);
		return C1limitPrice;
	}

	public void oldC2SoftMax() {
		/*
		double [] newP = new double[ob.hourAhead+1];
		double [] arrPredClearingPrice = new double[ob.hourAhead+1];
		normalized = new double[ob.hourAhead+1];
		double minCP = Double.MAX_VALUE;
		double maxCP = Double.MIN_VALUE;
		int minCPIndex = 0;
		int maxCPIndex = 0;
		double sum = 0.0;
		for(int HA = 0; HA < arrPredClearingPrice.length; HA++){
			double d = ob.pricepredictor.getPrice(HA);
			arrPredClearingPrice[HA] = d;
			normalized[HA] = d;
			if(minCP > d){
				minCP = d;
				minCPIndex = HA;
			}
			if(maxCP < d){
				maxCP = d;
				maxCPIndex = HA;
			}
			sum += d;
		}

		minCP = Double.MAX_VALUE;
		maxCP = Double.MIN_VALUE;

		for(int HA = 0; HA < arrPredClearingPrice.length; HA++){
			double nvalue = 1-(arrPredClearingPrice[HA]/sum);
			arrPredClearingPrice[HA] = nvalue;
			if(minCP > nvalue){
				minCP = nvalue;
				minCPIndex = HA;
			}
			if(maxCP < nvalue){
				maxCP = nvalue;
			}
		}

		double range = maxCP-minCP;
		for(int HA = 0; HA < arrPredClearingPrice.length; HA++){
			double nvalue = ((arrPredClearingPrice[HA]-minCP)/range);
			normalized[HA] = nvalue;
		}

		double tpr = 0;
		double pr = 0.0;

		pr = getOutput(normalized[ob.hourAhead]);

		double C1limitPrice = 0.0;
		double limitPrice = arrPredClearingPrice[ob.hourAhead];
		double z = utility.calc_q(pr);
		if(newP[0] < 0.5)
			z *= -1;
		//System.out.println("Pr: " + newP[0] + " z: " + z);
		C1limitPrice = Math.abs(limitPrice+z*7.8);
		 */
	} 

	public double oldC2(Observer ob) {

		// First method: Modified Working oKay
		int [] newPIndices = new int[ob.hourAhead+1];
		double threshold = 1;
		for(int i = 0; i < newPIndices.length; i++) {
			newPIndices[i] = 0;
			if(i == 0)
				threshold *= probability[8];
			else
				threshold *= probability[0];
		}

		double totP = Double.MIN_VALUE;

		int lastCounter = 0;
		while(totP < threshold) {
			totP = 1;
			for(int i = 0; i < newPIndices.length; i++) {
				totP *= probability[newPIndices[i]];
			}

			if(totP < threshold)
			{
				// update newP[lastIndex]
				if(newPIndices[lastCounter] == (probability.length-4))
					lastCounter++;

				if(lastCounter >= newPIndices.length)
					break;

				newPIndices[lastCounter]++;
			}
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
		System.out.println("Z: " + z);
		C1limitPrice = Math.abs(limitPrice+z*7.8);
		return C1limitPrice;

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
		System.out.println("Z: " + z);
		C1limitPrice = Math.abs(limitPrice+z*7.8);
		return C1limitPrice;
	}

	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob) {
		// Bidding configuration
		if(this.neededMWh > 0){

			double [][] info = new double[ob.hourAhead+1][4];
			/* C3=oldformula
			 * -: with -30% error: pp err 181
			 * -: with -20% error: pp err 85.64
			 * -: with -10% error: pp err 59.94
			 * -652414.42: with 0% error: pp err 46.05
			 * -: with 1% error: pp err 46.05
			 * -: with 5% error: pp err 46.05
			 * -: with 10% error: pp err 46.80
			 * -: with 20% error: pp err 45.193 
			 *  */
			//double C2limitPrice = C3(ob, info);
			
			//double [][] info = new double[ob.hourAhead+1][4];
			/* C4=oldvalues
			 * -750250.05: with -30% error: pp err 181
			 * -506558.92: with -20% error: pp err 85.64
			 * -509490.42: with -10% error: pp err 59.94
			 * -546350.88: with 0% error: pp err 46.05
			 * -549271.27: with 1% error: pp err 46.05
			 * -570655.81: with 5% error: pp err 46.05
			 * -662000.53: with 10% error: pp err 46.80
			 * -755765.96: with 20% error: pp err 45.193 
			 *  */
			//double C2limitPrice = C3(ob, info);
			/* C4=newValues
			 * -652472.29: with -30% error: pp err 181
			 * -535912.02: with -20% error: pp err 85.64
			 * -528382.12: with -10% error: pp err 59.94
			 * -538484.71: with 0% error: pp err 46.05
			 * -557276.66: with 1% error: pp err 46.05
			 * -567495.91: with 5% error: pp err 46.05
			 * -647616.38: with 10% error: pp err 46.80
			 * -708494.28: with 20% error: pp err 45.193 
			 *  */
			//double C2limitPrice = C3(ob, info); 

			//double C2limitPrice = parametricC2(ob); // -586875.85
			//double C2limitPrice = oldC2(ob); // -599145.19

			/* C2
			 * -794065.74: with -30% error: pp err 181
			 * -681485.69: with -20% error: pp err 85.64
			 * -542847.50: with -10% error: pp err 59.94
			 * -562273.71: with 0% error: pp err
			 * -668295.74: with 10% error: pp err 30.42
			 * -1123595.90: with 20% error: pp err 39.31
			 *  */
			double C2limitPrice = C2(ob);
			//System.out.println(" C2LP " + C2limitPrice);

			if((this.neededMWh-MIN_MWH) <= 0) {
				return;
			}

			Bid bid = new Bid(this.playerName, this.id, C2limitPrice, this.neededMWh, this.type);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			bids.add(bid);
		}
	}

	public double getOutput(double netInput) {
		totalLayerInput = 0;
		// add max here for numerical stability - find max netInput for all neurons in this lyer
		double max = 0;

		for (double neuron : normalized) {
			totalLayerInput += Math.exp(neuron-max);
		}

		double output = Math.exp(netInput-max) / totalLayerInput;

		//System.out.println("CP " + netInput + " : pr " + output);
		return output;
	}

	public void print2D(double [][] arr2D) {
		for(int i =0; i< arr2D.length;i++) {
			for(int j=0; j< arr2D[i].length; j++)
			{
				System.out.printf("%.3f ",arr2D[i][j]);
			}
			System.out.println();
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
	
	public boolean isThresholdRefined(double [][] p, double threshold) {
		double temp = 0;
		double tThreshold = 0;
		for(int i = 0; i < p.length; i++) {
			double var = p[i][Pr]*(1-tThreshold-temp);
			tThreshold += var;
			if(i>0)
				temp = var+temp; 
		}
		if(tThreshold >= threshold)
			return false;
		return true;
	}

	public boolean isThresholdC3(double [][] p, double threshold) {
		double totalP = p[0][Pr];
		for(int i = 1; i < p.length; i++) {
			totalP *= p[i][Pr]; 
		}
		if(totalP >= threshold)
			return false;
		return true;
	}

	public double C4(Observer ob, double [][] info) {
		//C4
		double threshold = MAX_PR;
		double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
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

		double std = mult*7.8;
		double C2limitPrice = Math.abs(limitPrice+std);

		return C2limitPrice;
	}

	public double C3(Observer ob, double [][] info) {
		//C3
		double threshold = 1;
		double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
		// Initialize
		// System.out.println("Initialize array");
		int thresholdIndex = 9;
		threshold = 1;
		for(int i = 0; i < info.length; i++) {
			info[i][Pr] = MIN_PR;
			double d =  ob.pricepredictor.getPrice(i);
			info[i][CP] = d;
			info[i][PCP] = d;
			info[i][HA] = i;
			if(thresholdIndex > 0)
				--thresholdIndex;
			threshold *= probability[thresholdIndex];
		}
		//threshold = probability[8];
		// print2D(info);

		int lastCounter = 0;
		while(isThresholdC3(info, threshold)) {
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

		double std = mult*7.8;
		double C2limitPrice = Math.abs(limitPrice+std);

		return C2limitPrice;
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
}