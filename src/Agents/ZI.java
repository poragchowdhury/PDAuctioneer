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

public class ZI extends Agent {

	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	public PricePredictor pricePredictor;

	public ZI(String name, int id, double neededMWh, double mean, double stddev){
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
		Random r = new Random();
		
		if(this.neededMWh > 0){
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
			double ZIlimitPrice = 0.0;
			
			if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.TRANINIG_WITH_5ZI.getValue())
			{
				ZIlimitPrice = Math.abs((r.nextGaussian()*stddevPrice)+meanBidPrice);
			}
			else{
				double [] param = new double[11];
				param = ob.getFeatures(param);
				double limitPrice = pricePredictor.getLimitPrice(param);
				ZIlimitPrice = Math.abs((r.nextGaussian()*5)+limitPrice);
			}
			
			
			Bid bid = new Bid(this.playerName, this.id, ZIlimitPrice, this.neededMWh, this.type);
			if(ob.DEBUG)
				System.out.println(bid.toString());
			bids.add(bid);
		}
	}
}