package Agents;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import configure.Configure;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;

public abstract class Agent {
	public String playerName = "defaultPlayer"; //Overwrite this variable in your player subclass
	public double neededMWh = 0;
	public double initialNeededMWh = 0;
	public double greenPoint = 0;
	public int id = 0;
	public static enum agentType {
	    PRODUCER, BROKER 
	}
	public static String predictorName = Configure.getPREDICTOR_NAME();
	public agentType type;
	public abstract void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer ob);
	public Object getMCTS(){
		return null;
	}
	
	public void setFlag(boolean flag){}
	
}
