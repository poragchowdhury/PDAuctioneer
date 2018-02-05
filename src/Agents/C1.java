package Agents;

import java.util.ArrayList;
import java.util.Random;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import MCTS.MCTS;
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

public class C1 extends Agent {

	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	public PricePredictor pricePredictor;
	public Utility utility;
	public double MIN_PR = 0.025;
	public double MAX_PR = 0.975;
	public double [] probability = {0.025, 0.069, 0.160, 0.300, 0.500, 0.690, 0.840, 0.930, 0.975};
	public double [] sigma = {-2, -1.5, -1, -0.5, 0, 0.5, 1, 1.5, 2};
	public C1(String name, int id, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.BROKER;
		pricePredictor = new PricePredictor(Agent.predictorName);
		utility = new Utility();
	}

	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob) {

		// Bidding configuration
		if(this.neededMWh > 0){
			double z = newC1(ob);
			double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
			double C1limitPrice = Math.abs(limitPrice+z*7.8);
			/*
			double [] newP = new double[ob.hourAhead+1];
			double threshold = 1;
			
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

			double C1limitPrice = 0.0;
			double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
			int index = newP.length-1;// 0;//
			double z = utility.calc_q(newP[index]);
			if(newP[index] < 0.5)
				z *= -1;
			System.out.println("Pr: " + newP[index] + " z: " + z);
			C1limitPrice = Math.abs(limitPrice+z*7.8);
			*/
			/*
			// Initial algorithm
			int [] newPIndices = new int[ob.hourAhead+1];
			double threshold = 1;
			for(int i = 0; i < newPIndices.length; i++) {
				newPIndices[i] = 0;
				if(i == 0)
					threshold *= probability[probability.length-1];
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
					int index = lastCounter%newPIndices.length;
					newPIndices[index]++;
				}
				lastCounter++;
			}
			

			double C1limitPrice = 0.0;
			double limitPrice = ob.pricepredictor.getPrice(ob.hourAhead);
			double z = sigma[newPIndices[0]];
			System.out.println(" z: " + z);
			C1limitPrice = Math.abs(limitPrice+z*7.8);
			*/
			
			System.out.println("C1limitPrice" + C1limitPrice);
			if((this.neededMWh-MIN_MWH) <= 0) {
				return;
			}

			Bid bid = new Bid(this.playerName, this.id, C1limitPrice, this.neededMWh, this.type);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			bids.add(bid);
		}
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
	
    public double newC1(Observer ob) {
    	//newC1
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

		double mult = utility.calc_q(newP[newP.length-1]);
		if(newP[newP.length-1] < 0.5)
			mult *= -1;

		return mult;
    }

}