package Agents;

import java.util.ArrayList;
import java.util.Random;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;
import Observer.PricePredictor;
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

public class C1 extends Agent {

	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	public PricePredictor pricePredictor;
	public double [] probability = {0.025, 0.069, 0.16, 0.30, 0.50, 0.69, 0.84, 0.93, 0.97};
	public double [] sigma = {-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2};
	public C1(String name, int id, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.BROKER;
		pricePredictor = new PricePredictor(Agent.predictorName);
	}

	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob) {
		// Bidding configuration
		if(this.neededMWh > 0){
			Random r = new Random();

			int [] newPIndices = new int[ob.hourAhead+1];
			double threshold = 1;
			for(int i = 0; i < newPIndices.length; i++) {
				newPIndices[i] = 0;
				if(i == 0)
					threshold *= probability[probability.length-1];
				else
					threshold *= probability[0];
			}
			
			threshold = roundup(threshold);

			double totP = Double.MIN_VALUE;
			
			int lastCounter = 0;
			while(totP < threshold) {
				totP = 1;
				for(int i = 0; i < newPIndices.length; i++) {
//					if(newPIndices[0] == 8 && newPIndices[1] == 8)
//						System.out.println("Ok");
					totP *= probability[newPIndices[i]];
				}
				
				totP = roundup(totP);
				
				if(totP < threshold)
				{
					// update newP[lastIndex]
					int index = lastCounter%newPIndices.length;
					newPIndices[index]++;
				}
				lastCounter++;
			}
			
			/*double [] param = new double[9];
			param = ob.getTime(param);
			Instance myIns = getTestInstance(param);
			if (myIns == null)
				System.out.println("*****************My Ins Null : ");
			try{
				limitPrice = this.classifier.classifyInstance(myIns);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}*/
			double C1limitPrice = 0.0;
			double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
			C1limitPrice = Math.abs(limitPrice+sigma[newPIndices[newPIndices.length-1]]*7.8);
			//System.out.println("C1limitPrice" + C1limitPrice);
			if((this.neededMWh-MIN_MWH) <= 0) {
				return;
			}

			Bid bid = new Bid(this.playerName, this.id, C1limitPrice, this.neededMWh, this.type);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			bids.add(bid);
		}
	}
	
	public double roundup(double totP) {
		
		// rounding values
		totP = totP*100;
		totP = Math.round(totP);
		totP = totP /100;
		return totP;
	}
}