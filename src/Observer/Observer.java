package Observer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import configure.Configure;
import Agents.Agent;
import Agents.Agent.agentType;
import Agents.TacTex;
import Auctioneer.Ask;
import Auctioneer.Bid;
import MCTS.TreeNode;
import TacTex.ChargeMwhPair;
import TacTex.PriceMwhPair;

public class Observer {
	public Configure config;
	public int day = 0;
	public int date = 23;
	public int month = 10;
	public int year = 2016;
	public int hour = 0;
	public int hourAhead = 0;
	public int currentTimeSlot = 0;
	public int SEED = 1;
	public int SUB_CASE_STUDY = 1;
	public HashMap<Integer, HashMap<String, ArrayList<Double>>> results;
	public double [] arrMCPMean;
	public double [] arrMCPCount;
	
	
	public int[] mctsDebugVals;
	public double mctsxRealCostPerHour = 0.0;
	public double mctsxPredictedCostPerHour = 0.0;
	public Double lowestAskPrice = null;
	public int [] minCPHourAhead;
	public double [] MCPrice;
	public double [] MCPriceCount;
	
	public double[][] mctsxRealCost;
	public double[][] mctsxPredictedCost;
	
	public double error = 0; // percentage error introduced to the predictor
	public double[] ma_err; // root mean square error
	public double[] rmse_err; // root mean square error
	public double[] pp_error_ha; // Percentage error
	public double[] pp_error_ha_count; // Percentage error counter
	
	public int[][] recordMCTSMove;
	public PredictorFactory pricepredictor;
	public PricePredictor REPTreePricePredictor;
	
	public HashMap <Integer, HashMap<Integer, ArrayList<PriceMwhPair>>> powerTACproducerOrders;
	public double [][][] weatherPrediction;
	public double [] movingAvgErrorMCP;

	public double [] STDDEV = {
			
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8,
			7.8
			
			/*
			7.6011651048203825-5,
			7.534673513797154-5,
			13.885980528858367-5,
			7.371604751094366-5,
			7.334558933426562-5,
			14.744177674603007-5,
			15.441179642538206-5,
			12.510704851503748-5,
			12.704920910230943-5,
			12.894321899454713-5,
			13.163495679667275-5,
			19.226502487085746-5,
			18.91134608023634-5,
			13.489131457995954-5,
			13.798582038642978-5,
			13.602595371178728-5,
			12.655378913965938-5,
			12.883158738012872-5,
			13.444526714625912-5,
			14.21377759556834-5,
			15.750774630084477-5,
			17.190269133890784-5,
			7.462504821928083-5,
			7.315001774695123-5
			*/
	};
	
	
	public static enum COST_ARRAY_INDICES{
		VOL_BUY(0),
		TOT_BUY(1),
		VOL_SELL(2),
		TOT_SELL(3),
		VOL_BAL(4),
		TOT_BAL(5);
		
		private final int id;
		COST_ARRAY_INDICES(int id) { this.id = id; }
	    public int getValue() { return id; }
	}
	public String getTime(){
		return currentTimeSlot + "," + hour + "," + hourAhead + "," + date + "," + month + "," + year + "," + Configure.getNUMBER_OF_BROKERS() + "," + Configure.getNUMBER_OF_PRODUCERS() + ",";
	}
	
	public String getWeatherPrediction(){
		return weatherPrediction[hour][hourAhead][0] + "," + weatherPrediction[hour][hourAhead][1] + "," + weatherPrediction[hour][hourAhead][2] + "," + weatherPrediction[hour][hourAhead][3];
	}
	
	
	public boolean DEBUG = false;
	public double neededEneryMCTSBroker = 0;
	public double initialNeededEneryMCTSBroker = 0;
	public int NUM_MCTS_SIM = 0;
	public double [][] arrProducerBidPrice;
	public double [][] arrProducerBidVolume;
	public double [][] arrMarketClearingPriceHistory;
	// Green Energy 80; Red energy 20
	public final double GREEN_POINTS = 80.00;
	public final double RED_POINTS = 20.00;
	public double [] arrProducerGreenPoints;
	public int ERROR_PERCENTAGE;
	public boolean GREEN_AUCTION_FLAG = false;
	public int MCTSSimulation = 100;
	public double nanoTime = 0;
	public double nanoTimeCount = 0;
	public int printNamesCount = 0;
	public double meanClearingPrice = 0;
	public int clearedAuctionCount = 0;
	
	public double totalGreenAskVolumeCleared = 0;
	public double totalAskVolumeCleared = 0;
	public boolean printFlag;
	
	public HashMap<String, Double> clearedVolumes;
	public HashMap<String, Double> clearedVolumesBids;
	public HashMap<String, Double> clearedVolumesAsks;
	
	public HashMap<String, Double> clearedTotalVolumes;
	public HashMap<String, Double> clearedTotalBidVolumes;
	public HashMap<String, Double> clearedTotalAskVolumes;
	
	public ArrayList<Ask> clearedAsks;
	public ArrayList<Bid> clearedBids;
	
	public HashMap<String, Double> neededVolumes;
	public HashMap<String, Double> neededTotalVolumes;
	
	public HashMap<String, double[]> costTotal;
	
	public ArrayList<Agent> agents;
	public ArrayList<Agent> printableAgents;
	
	public double [] arrBalacingPrice;
	
	public double[] getFeatures(double[] param, int ha){
		param[0] = currentTimeSlot;
		param[1] = hour;
		param[2] = ha;
		param[3] = date;
		param[4] = month;
		param[5] = year;
		param[6] = Configure.getNUMBER_OF_BROKERS();
		param[7] = Configure.getNUMBER_OF_PRODUCERS();
		if(currentTimeSlot >= Configure.getHOURS_IN_A_DAY())
			param[8] = arrMarketClearingPriceHistory[currentTimeSlot-Configure.getHOURS_IN_A_DAY()][ha];
		if(ha < Configure.getTOTAL_HOUR_AHEAD_AUCTIONS())
			param[9] = arrMarketClearingPriceHistory[currentTimeSlot][ha+1];
		param[10] = weatherPrediction[hour][ha][0];
		param[11] = weatherPrediction[hour][ha][1];
		param[12] = weatherPrediction[hour][ha][2];
		param[13] = weatherPrediction[hour][ha][3];
		
		return param;
	}
	
	public Observer(){
		config = new Configure();
		arrMarketClearingPriceHistory = new double[(Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY())+1][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		mctsDebugVals = new int[6];
	
		clearedVolumes = new HashMap<String, Double>();
		clearedVolumesBids = new HashMap<String, Double>();
		clearedVolumesAsks = new HashMap<String, Double>();
		
		clearedTotalVolumes = new HashMap<String, Double>();
		clearedTotalBidVolumes = new HashMap<String, Double>();
		clearedTotalAskVolumes = new HashMap<String, Double>();
		
		neededTotalVolumes = new HashMap<String, Double>();
		costTotal = new HashMap<String, double[]>();
		
		clearedAsks = new ArrayList<Ask>();
		clearedBids = new ArrayList<Bid>();
		
		agents = new ArrayList<Agent>();
		printableAgents = new ArrayList<Agent>();
		arrBalacingPrice = new double[Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY()];
		
		arrProducerBidPrice = new double[Configure.getNUMBER_OF_PRODUCERS()][Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY()];
		arrProducerBidVolume = new double[Configure.getNUMBER_OF_PRODUCERS()][Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY()];
		arrProducerGreenPoints = new double[Configure.getNUMBER_OF_PRODUCERS()];
		
		mctsxRealCost = new double[Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY()][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		mctsxPredictedCost = new double[Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY()][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()];
		ma_err = new double [Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		rmse_err = new double [Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		pp_error_ha = new double [Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		pp_error_ha_count = new double [Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		
		recordMCTSMove = new int[Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()][26];
		REPTreePricePredictor = new PricePredictor(Configure.getPREDICTOR_NAME());
		pricepredictor = new PredictorFactory(this);
		
		minCPHourAhead = new int[24];
		MCPrice = new double [24];
		MCPriceCount = new double [24];
		arrMCPMean = new double [24];
		arrMCPCount = new double [24];
		weatherPrediction = new double[(Configure.getHOURS_IN_A_DAY())*Configure.getTOTAL_SIM_DAYS()][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()][4];
		movingAvgErrorMCP = new double[Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()];
	}
	
	public void setTime(int day, int hour, int hourAhead, int currentTimeSlot){
		this.day = day;
		this.hour = hour;
		this.hourAhead = hourAhead;
		this.currentTimeSlot = currentTimeSlot;
		this.date = this.date + this.day;
	}
	
	public void recordBestMove(TreeNode n){
		int action = n.actionName;
		recordMCTSMove[hourAhead][action] += 1;
	}
	
	public void updateMean(double mcp) {
		if(mcp != 0) {
			arrMCPMean[hourAhead] = arrMCPMean[hourAhead]*arrMCPCount[hourAhead]+mcp;
			arrMCPCount[hourAhead]++;
			arrMCPMean[hourAhead]  /= arrMCPCount[hourAhead];
		}
	}
	
	public void printSTDDEV() {
		for(int i = 0; i < 24; i++)
			System.out.println(STDDEV[i]);
	}
	
	public void updateSTDDEV() {
		double oldStdev = STDDEV[hourAhead];
		double newStddev = calcSTDDEV();
		newStddev = oldStdev * 0.95 + newStddev * 0.05;
		STDDEV[hourAhead] = newStddev;
	}
	
	public double calcSTDDEV() {
		double stddev = 7.8;
		double mean = arrMCPMean[hourAhead];
		double sum = 0.0;
		double count = 0.0;
		for(int i = currentTimeSlot-1; i >= 0 ; i--) {
			if(arrMarketClearingPriceHistory[i][hourAhead] != 0) {
				count++;
				double diff = (mean - arrMarketClearingPriceHistory[i][hourAhead]);
				diff = diff * diff;
				sum += diff;
			}
		}
		if(count > 1) {
			sum = sum / (count-1);
			stddev = Math.sqrt(sum);
		}
		//System.out.println("********************STDDEV "+ stddev);
		return stddev;
	}
	
	public void importPowerTACData() {
		try {
			powerTACproducerOrders = new HashMap<Integer, HashMap<Integer, ArrayList<PriceMwhPair>>>();
			//boolean initialized = false;
	        //File gFile = new File("boots//"+Configure.getBootName());
	        File gFile = new File("boots-misobuyer//boot"+(SEED-1)+".csv");
	        if(!gFile.exists()){
	            System.out.println("Load file doesn't exist");
	        	return;
	        }
	        CSVParser parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			int prevhour = -1;
			int prevhourAhead = -1;
			Random r = new Random(SEED);
			
	        for (CSVRecord csvRecord : parser) {
	            Iterator<String> itr = csvRecord.iterator();
	            // Hour	
	            String strHour = itr.next();
	            int hour = Integer.parseInt(strHour);
	            
	            if(hour >= Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY())
	            	break;
	            
	            // HourAhead
	            String strHourAhead = itr.next();
	            int hourAhead = Integer.parseInt(strHourAhead);
	            
	            if(hourAhead >= Configure.getTOTAL_HOUR_AHEAD_AUCTIONS())
	            	continue;
	            	            
	            HashMap<Integer, ArrayList<PriceMwhPair>> hourAheadOrders = powerTACproducerOrders.get(hour);
	            if(hourAheadOrders == null) {
	            	hourAheadOrders = new HashMap<Integer, ArrayList<PriceMwhPair>>();
	            }
	            
	            ArrayList<PriceMwhPair> orders = hourAheadOrders.get(hourAhead);
	            if(orders == null) {
	            	orders = new ArrayList<PriceMwhPair>();
	            }
	            
	            // mwh
	            String strMWh = itr.next();
	            Double mwh = Double.parseDouble(strMWh);
	            // $
	            String strPrice = itr.next();
	            Double price;
	            if(strPrice.equalsIgnoreCase("null"))
	            	price = 0.0;
	            else
	            	price = Double.parseDouble(strPrice);
	            
	            PriceMwhPair order = new PriceMwhPair(price, mwh.doubleValue());
	            orders.add(order);
	            hourAheadOrders.put(hourAhead, orders);
	            powerTACproducerOrders.put(hour, hourAheadOrders);
	            
	            // cloud coverage
	            String cc = itr.next();
	            Double couldCoverage = Double.parseDouble(cc);
	            // temp
	            String temp = itr.next();
	            Double temperature = Double.parseDouble(temp);
	            // wspeed
	            String wspeed = itr.next();
	            Double speed = Double.parseDouble(wspeed);
	            // wdirection
	            String wdirection = itr.next();
	            Double direction = Double.parseDouble(wdirection);
	            
	            weatherPrediction[hour][hourAhead][0] = couldCoverage;
	            weatherPrediction[hour][hourAhead][1] = temperature;
	            weatherPrediction[hour][hourAhead][2] = speed;
	            weatherPrediction[hour][hourAhead][3] = direction;
	            
	            //initialized = true;
	            
	            prevhour = hour;
	            prevhourAhead = hourAhead;
	        }
	        parser.close();
		}
		catch(Exception ex) {
			System.out.println("what?" + ex.getMessage());
		}
	}
	
	public void importProducerData(){
		try
        {
			boolean initialized = false;
            File gFile = new File("loads.csv");
            if(!gFile.exists()){
                System.out.println("Load file doesn't exist");
            	return;
            }
            
			CSVParser parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			int hour = 0;
			int meanBidPrice = 30;
			int stddevPrice = 10;
			Random r = new Random(SEED);
            for (CSVRecord csvRecord : parser) {
                Iterator<String> itr = csvRecord.iterator();
                // Time Stamp	
                String strTimeStamp = itr.next();
                
                for(int i = 0; i < Configure.getNUMBER_OF_PRODUCERS(); i++){
	                // Name
	                String strName = itr.next();
	                if(!initialized){
		                if(strName.equalsIgnoreCase("N.Y.C.") || strName.equalsIgnoreCase("CAPITL") || strName.equalsIgnoreCase("WEST"))
		                	arrProducerGreenPoints[i] = RED_POINTS;
		                else
		                	arrProducerGreenPoints[i] = GREEN_POINTS;
	                }
	                // LBMP ($/MWHr)
	                String strLBMP = itr.next();
	                
	                // Add a gaussian noise to the volume
	                double noise = (r.nextGaussian()*200)+ 0;
	                
	                arrProducerBidVolume[i][hour] = (Double.parseDouble(strLBMP)+noise);
	            }
                initialized = true;
                hour++;
                if(hour >= Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY())
                	break;
            }
            parser.close();
            
            gFile = new File("prices.csv");
            if(!gFile.exists()){
                System.out.println("Price file doesn't exist");
            	return;
            }
            
			parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			hour = 0;
            for (CSVRecord csvRecord : parser) {
                Iterator<String> itr = csvRecord.iterator();
                // Time Stamp	
                String strTimeStamp = itr.next();
                for(int i = 0; i < Configure.getNUMBER_OF_PRODUCERS(); i++){
	                // Name
	                String strName = itr.next();
	                // LBMP ($/MWHr)
	                String strLBMP = itr.next();
	                arrProducerBidPrice[i][hour] = Double.parseDouble(strLBMP);
                }
                hour++;
                if(hour >= Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY())
                	break;
            }
            parser.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}
	
	public void generateBalancingPrices(int mean, int stddev){
        if(mean == 0)
        	mean = 350*2;
        if(stddev == 0) 
        	stddev = 5;
		Random r = new Random(SEED);
		
		double NUMBER_OF_AUCTIONS = Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY();
		
		for(int i = 0; i < NUMBER_OF_AUCTIONS; i++){
	    	arrBalacingPrice[i] = Math.abs(Math.abs((r.nextGaussian()*stddev))+(mean));
		}
		
   	}
	
	public double randError(int errorRate) {

		int min = 100 - errorRate;
		int max = 100 + errorRate;
	    // NOTE: This will (intentionally) not run as written so that folks
	    // copy-pasting have to think about how to initialize their
	    // Random instance.  Initialization of the Random instance is outside
	    // the main scope of the question, but some decent options are to have
	    // a field that is initialized once and then re-used as needed or to
	    // use ThreadLocalRandom (if using at least Java 1.7).
	    Random rand = new Random(SEED);

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    double randomNum = (rand.nextInt((max - min) + 1) + min);

	    double randomGauss = (rand.nextGaussian()*errorRate)+0;
	    
	    randomGauss = 100 + randomGauss;
	    
	    return randomGauss/100;
	}
	
	public void addClearedTrades(Bid bid){
		Double vol = clearedVolumesBids.get(bid.agentName); 
		if(vol == null)
			vol = bid.amount;
		else
			vol = vol.doubleValue() + bid.amount;
	
		clearedVolumesBids.put(bid.agentName, vol);		
		clearedBids.add(bid);
	}
	
	public void addClearedTrades(Ask ask){
		Double vol = clearedVolumesAsks.get(ask.agentName); 
		if(vol == null)
			vol = ask.amount;
		else
			vol = vol.doubleValue() + ask.amount;
	
		clearedVolumesAsks.put(ask.agentName, vol);
		clearedAsks.add(ask);
	}
	
	public void addneededTotalVolumes(String brokerName, double neededMWh){
		Double neededTotalMWh = neededTotalVolumes.get(brokerName);
		if(neededTotalMWh == null){
			neededTotalMWh = neededMWh;
		}
		else{
			neededTotalMWh += neededMWh;
		}
		neededTotalVolumes.put(brokerName, neededTotalMWh);
	}
	
	public void addTotalClearTrade(Bid bid){
		Double volTotal = clearedTotalBidVolumes.get(bid.agentName); 
		if(volTotal == null){
			volTotal = bid.amount;
		}
		else{
			volTotal += bid.amount;
		}
		clearedTotalBidVolumes.put(bid.agentName, volTotal);
	}

	public void addTotalClearTrade(Ask ask){
		Double volTotal = clearedTotalAskVolumes.get(ask.agentName); 
		if(volTotal == null){
			volTotal = ask.amount;
		}
		else{
			volTotal += ask.amount;
		}
		clearedTotalAskVolumes.put(ask.agentName, volTotal);
	}
	
	public void addTotalTradeCost(Bid bid, int VOL, int TOT_PRICE){
		double[] volTotal = costTotal.get(bid.agentName); 
		if(volTotal == null){
			volTotal = new double[6];
			volTotal[VOL] = bid.amount;
			volTotal[TOT_PRICE] = (bid.amount*bid.price);
		}
		else{
			volTotal[VOL] += bid.amount;
			volTotal[TOT_PRICE] += (bid.amount*bid.price);
		}
		costTotal.put(bid.agentName, volTotal);
	}
	
	public void addTotalTradeCost(Ask ask, int VOL, int TOT_PRICE){
		double[] volTotal = costTotal.get(ask.agentName); 
		if(volTotal == null){
			volTotal = new double[6];
			volTotal[VOL] = Math.abs(ask.amount);
			volTotal[TOT_PRICE] = ask.amount*ask.price;
		}
		else{
			volTotal[VOL] += Math.abs(ask.amount);
			volTotal[TOT_PRICE] += (Math.abs(ask.amount)*ask.price);
		}
		costTotal.put(ask.agentName, volTotal);
	}
	
	public void addAgents(Agent agent){	
		agents.add(agent);
	}
	
	public void setNeededVolumes(){
		// Set agents neededMWh
		if(DEBUG)
			System.out.println("Settingup needed volume");
		for(Agent agent : agents){
			if(agent.type == Agent.agentType.BROKER)
				if(agent.playerName.equalsIgnoreCase("MCTSX")){
					agent.neededMWh = (Configure.getPERHOURENERGYDEMAND() * Configure.avgSupply_REPTree[SEED-1])*Configure.getMCTSBROKERDEMANDPERC();//Configure.getPERHOURENERGYDEMAND()*Configure.getMCTSBROKERDEMANDPERC();
				}
				else{
					agent.neededMWh = (Configure.getPERHOURENERGYDEMAND() * Configure.avgSupply_REPTree[SEED-1])*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));//Configure.getPERHOURENERGYDEMAND()*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));//Configure.getPERHOURENERGYDEMAND()*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));
				}
			else{
				agent.neededMWh = arrProducerBidVolume[agent.id][currentTimeSlot];
			}
			
			if(DEBUG)
				System.out.println(agent.playerName + " " + agent.neededMWh);
			
			agent.initialNeededMWh = agent.neededMWh;
		}
	}
	
	public double getAgentNeededMWh(String brokerName){
		for(Agent agent : agents){
			if(agent.playerName.equalsIgnoreCase(brokerName)){
				return agent.neededMWh;
			}
		}
		return 0;
	}
	
	public void adjustNeededVolumes(){
		// Set agents neededMWh
		for(Agent agent : agents){
			String brokerName = agent.playerName;
			Double volumeCleared = 0.0;
			if(Agent.agentType.BROKER == agent.type){
				volumeCleared = getClearedBidVolume(brokerName);

				if(volumeCleared == null)
				{
					// check if agent is ZIP to say that the volume is not cleared
					if(agent.playerName == "ZIP")
						agent.setFlag(false);
					volumeCleared = 0.0;
				}
				else{
					if(agent.playerName == "ZIP")
						agent.setFlag(true);
				}
				
				Double volumeAskCleared = 0.0;
				volumeAskCleared = getClearedAskVolume(brokerName);
				if(volumeAskCleared == null)
					volumeAskCleared = 0.0;
				
				volumeCleared -= volumeAskCleared;
				
			}
			else{
				volumeCleared = getClearedAskVolume(brokerName);
				if(volumeCleared == null)
					volumeCleared = 0.0;
			}
			agent.neededMWh = agent.neededMWh-volumeCleared.doubleValue();
			
			clearedVolumes.put(brokerName,0.00);
		    clearedVolumesBids.put(brokerName,0.00);
		    clearedVolumesAsks.put(brokerName,0.00);
		}
	}
	
	public Double getClearedBidVolume(String brokerName){
		return clearedVolumesBids.get(brokerName);
	}
	public Double getClearedAskVolume(String brokerName){
		return clearedVolumesAsks.get(brokerName);
	}
	
	public Double getClearedVolume(String brokerName){
		return clearedVolumes.get(brokerName);
	}
	
	public double getClearedVolume(){
		double clrVol = 0.0;
		
		for (Map.Entry<String, Double> entry : clearedVolumesBids.entrySet()) {
		    clrVol += entry.getValue().doubleValue();
		}
		
		for (Map.Entry<String, Double> entry : clearedVolumesAsks.entrySet()) {
		    clrVol += entry.getValue().doubleValue();
		}
		
		return (double) clrVol/2;
	}
	
	public double getTotalBidClearedVolume(String brokerName){
		if(clearedTotalBidVolumes.get(brokerName) == null)
			return 0.0;
		return clearedTotalBidVolumes.get(brokerName).doubleValue();
	}
	
	public double getTotalAskClearedVolume(String brokerName){
		if(clearedTotalAskVolumes.get(brokerName) == null)
			return 0.0;
		return clearedTotalAskVolumes.get(brokerName).doubleValue();
	}
	
	public double[] getTotalCost(String brokerName){
		if(costTotal.get(brokerName) == null)
		{
			double[] costArr = new double[6];
			return costArr;
		}
		
		return costTotal.get(brokerName);
	}
	
	public double getTotalClearedVolume(String brokerName){
		return clearedTotalVolumes.get(brokerName).doubleValue();
	}
	
	public void printClearedVolume(double clearingPrice){
		
		arrMarketClearingPriceHistory[currentTimeSlot][hourAhead] = clearingPrice;
		Double totalClearedVolume = 0.0;
		if(DEBUG)
			System.out.println("PrintClearedVolume");
		
		for (Agent a : agents) {
		    String brokerName = a.playerName;
		    int brokerid = a.id;
		    if(a.type == Agent.agentType.BROKER){
			    Double clearedBidMWh = getClearedBidVolume(brokerName);
			    if(clearedBidMWh == null)
			    	clearedBidMWh = 0.00;
			    
			    totalClearedVolume += clearedBidMWh;
			    
			    addTotalClearTrade(new Bid(brokerName,brokerid,0.0,clearedBidMWh,a.type));
			    addTotalTradeCost(new Bid(brokerName,brokerid,clearingPrice,clearedBidMWh,a.type), COST_ARRAY_INDICES.VOL_BUY.getValue(), COST_ARRAY_INDICES.TOT_BUY.getValue());
			    if(DEBUG)
			    	System.out.println(brokerName + " MWh : " + clearedBidMWh + " $ : " + clearingPrice);
		    
			    Double clearedAskMWh = getClearedAskVolume(brokerName);
			    if(clearedAskMWh == null)
			    	clearedAskMWh = 0.00;
			    addTotalClearTrade(new Ask(brokerName,brokerid,0.0,clearedAskMWh,a.type));
			    addTotalTradeCost(new Ask(brokerName,brokerid,clearingPrice,clearedAskMWh,a.type), COST_ARRAY_INDICES.VOL_SELL.getValue(), COST_ARRAY_INDICES.TOT_SELL.getValue());
			    
			    if(a.playerName.equalsIgnoreCase("MCTSX")){
			    	mctsxRealCostPerHour += (Math.abs(clearingPrice)*(Math.abs(clearedAskMWh)-Math.abs(clearedBidMWh)));
			    	mctsxRealCost[(day*Configure.getHOURS_IN_A_DAY())+hour][hourAhead] = (Math.abs(clearingPrice)*(Math.abs(clearedAskMWh)-Math.abs(clearedBidMWh)));
			    }
		    } else {
			    Double clearedAskMWh = getClearedAskVolume(brokerName);
			    if(clearedAskMWh == null)
			    	clearedAskMWh = 0.00;
			    
			    if(arrProducerGreenPoints[a.id] == GREEN_POINTS){
			    	totalGreenAskVolumeCleared += clearedAskMWh;
			    }
			    totalAskVolumeCleared += clearedAskMWh;
			    
			    addTotalClearTrade(new Ask(brokerName,brokerid,0.0,clearedAskMWh,a.type));
			    addTotalTradeCost(new Ask(brokerName,brokerid,clearingPrice,clearedAskMWh,a.type), COST_ARRAY_INDICES.VOL_SELL.getValue(), COST_ARRAY_INDICES.TOT_SELL.getValue());
			    if(DEBUG)
			    	System.out.println(brokerName + " MWh : " + clearedAskMWh + " $ : " + clearingPrice);
		    }
		}
		if(totalClearedVolume > 0) {
//			if(hourAhead <= 10)
//				System.out.println("Cleared Volume " + totalClearedVolume + " cPrice " + clearingPrice);
			pricepredictor.recordTradeResult(currentTimeSlot, currentTimeSlot+hourAhead+1, clearingPrice, totalClearedVolume);
		}
	}
	
	public void calculateError() throws IOException{
		// Print to the error log
		FileWriter fwOutput = new FileWriter("Results_Price_20actions.csv", true);
		PrintWriter pwOutput = new PrintWriter(new BufferedWriter(fwOutput));
		//pwOutput.println("MCTS_SIM,TS,Day,Hour,HourAhead,Error");
		for(int totHours = 0; totHours < Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY(); totHours++){
			for(int totHA = 0; totHA < Configure.getTOTAL_HOUR_AHEAD_AUCTIONS(); totHA++){
				
				// Do the summations to get tot_realcost upto balancing
				double realCost = 0.0;
				for(int j = 0; j <= totHA; j++)
					realCost += mctsxRealCost[totHours][j];
				
				realCost += mctsxRealCost[totHours][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()];
				
				//pwOutput.println(MCTSSimulation + "," + totHours + "," + totHours/Configure.getHOURS_IN_A_DAY() + "," + totHours%Configure.getHOURS_IN_A_DAY() + "," + totHA + "," + (realCost - mctsxPredictedCost[totHours][totHA]));
			}
		}
		double avg_err = 0; // percentage error
		double mae = 0;
		double rmse = 0;
		double count = 0;
		for(int i = 0; i < pp_error_ha.length; i++) {
			avg_err += pp_error_ha[i];
			mae += ma_err[i];
			rmse += rmse_err[i];
			count += pp_error_ha_count[i];
		}
		avg_err /= count;
		mae /= count;
		rmse /= count;
		System.out.println("Error: Percentage err " + avg_err + " MAE " + mae + " RMSE " + rmse);
		pwOutput.println("Error: Percentage err " + avg_err + " MAE " + mae + " RMSE " + rmse);
		pwOutput.close();
		fwOutput.close();
		
		mctsxRealCost = new double[Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY()][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		mctsxPredictedCost = new double[Configure.getTOTAL_SIM_DAYS()*Configure.getHOURS_IN_A_DAY()][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()+1];
		
	}
	
	public void writeMCTSMoves() throws IOException{
		String file = Configure.getRESULT_FILE();
		FileWriter fwOutput = new FileWriter(file + "-mcts-moves.csv", true);
		PrintWriter pwOutput = new PrintWriter(new BufferedWriter(fwOutput));
		pwOutput.println("HourAhead,Action,MoveTakenCount,avgmcp,minmAuctioncount,PercentageErr,MAE,RMSE");
		
		//pwOutput.println("ha,avgmcp,count,err");
		double avg_err = 0;
		double mae = 0;
		double rmse = 0;
		double count = 0;
		
		for(int totHA = 0; totHA < Configure.getTOTAL_HOUR_AHEAD_AUCTIONS(); totHA++){
			if(MCPriceCount[totHA] == 0)
				MCPriceCount[totHA]=1;
			for(int j = 0; j < 10; j++) {
				pwOutput.println(totHA + "," + j + "," + recordMCTSMove[totHA][j]+","+MCPrice[totHA]/MCPriceCount[totHA]+","+minCPHourAhead[totHA]+","+pp_error_ha[totHA]/pp_error_ha_count[totHA]+","+ma_err[totHA]/pp_error_ha_count[totHA]+","+rmse_err[totHA]/pp_error_ha_count[totHA]);
				recordMCTSMove[totHA][j] = 0;
			}
			MCPrice[totHA]=0;
			MCPriceCount[totHA]=0;
			minCPHourAhead[totHA]=0;
			avg_err += pp_error_ha[totHA];
			mae += ma_err[totHA];
			rmse += rmse_err[totHA];
			count += pp_error_ha_count[totHA];
			
			// reseting the error and counter values
			ma_err[totHA] = 0;
			rmse_err[totHA] = 0;
			pp_error_ha[totHA] = 0;
			pp_error_ha_count[totHA] = 0;
		}
		
		avg_err /= count;
		mae /= count;
		rmse /= count;
		System.out.println("Error: Percentage err " + avg_err + " MAE " + mae + " RMSE " + rmse);
		pwOutput.println("Error,Percentage err," + avg_err + ",MAE," + mae + ",RMSE," + rmse);
		
		pwOutput.close();
		fwOutput.close();
	}
	
	public void printTotalClearedVolume() throws IOException{
		String filename = Configure.getRESULT_FILE()+".csv";
		FileWriter fwOutput = new FileWriter(filename, true);
		PrintWriter pwOutput = new PrintWriter(new BufferedWriter(fwOutput));
		
		//FileWriter fwOutputV = new FileWriter("Results_Volume.csv", true);
		//PrintWriter pwOutputV = new PrintWriter(new BufferedWriter(fwOutputV));
		
		
		if(printFlag)
		{
			printFlag = false;
			System.out.print("Seed,\tCSDTY,\tMCTS#,\tAUCS#,\tHA#,\t");
			pwOutput.println(config.toString());
			pwOutput.print("Seed, CaseStudy, MCTSimulations, Auctions,HourAhead,");
			//pwOutputV.print("Seed, CaseStudy, MCTSimulations, Auctions,HourAhead,");
			for(Agent agent : printableAgents){
				String brokerName = agent.playerName;
		    	if(arrProducerGreenPoints[agent.id] == GREEN_POINTS && agent.type == agentType.PRODUCER){
		    		//System.out.print(brokerName + "(RES),\t");
		    		//pwOutput.print(brokerName + "(RES),");
		    		//pwOutputV.print(brokerName + "(RES),");
		    	}
		    	else if(agent.type == agentType.BROKER){
		    		System.out.print(brokerName + ",\tVol_Buy,\tTot_Buy,\tUnitBuy,\tVol_Sell,\tTot_Sell,\tUnitSell,\tPenalty,\tProfit,\t\tNet,\t\tPercBuy\t");
		    		pwOutput.print(brokerName + ",Vol_Buy,Tot_Buy,UnitBuy,Vol_Sell,Tot_Sell,UnitSell,Penalty,Profit,Net,Percentage,");
		    		//pwOutputV.print(brokerName + ",");
		    	}
			}
			pwOutput.println();
			//pwOutputV.println();
		    System.out.println();
		    printNamesCount++;
		}
		
		int printTrack = 0;
		
		//for (Map.Entry<String, Double> entry : costTotal.entrySet()) {
		for(Agent agent : printableAgents){
			//String brokerName = entry.getKey();
			String brokerName = agent.playerName;
			
			double neededTotalMWh = 0.0;
			if(neededTotalVolumes.get(brokerName) != null)
			    neededTotalMWh = neededTotalVolumes.get(brokerName).doubleValue();
		    
		    double clearedTotalMWh = 0;
		    
		    double[] totalCost = getTotalCost(brokerName);
		    double volbuy = totalCost[COST_ARRAY_INDICES.VOL_BUY.getValue()];
		    double buy = totalCost[COST_ARRAY_INDICES.TOT_BUY.getValue()]*(-1);
		    double unitcost = buy/volbuy;
		    double volsell = totalCost[COST_ARRAY_INDICES.VOL_SELL.getValue()];
		    double sell = totalCost[COST_ARRAY_INDICES.TOT_SELL.getValue()];
		    double unitsell = 0;
		    if(volsell <= 0){
		    	unitsell = sell/1;
		    }
		    else{
		    	unitsell = sell/volsell;
		    }
		    double volbal = totalCost[COST_ARRAY_INDICES.VOL_BAL.getValue()];
		    double penalty = totalCost[COST_ARRAY_INDICES.TOT_BAL.getValue()]*(-1);
		    double profit = sell+buy;
		    double net = profit+penalty;
		    
		    
		    
		    if(agent.type == Agent.agentType.BROKER){
		    	clearedTotalMWh = getTotalBidClearedVolume(brokerName);
		    }
		    else {
		    	clearedTotalMWh = getTotalAskClearedVolume(brokerName);
		    }
		    
		    //System.out.println(brokerName + "'s bid cleared " + ((clearedTotalBidMWh/neededTotalMWh)*100) + "% ," + " ask cleared " + ((clearedTotalAskMWh/neededTotalMWh)*100) + "%");
		    //System.out.println(brokerName + "'s unitCost " + (totalCost/totalPower) + "$ ");
		    if(printTrack == 0){
			    System.out.print(SEED + ",\t" + SUB_CASE_STUDY + ",\t" + MCTSSimulation + ",\t" +(currentTimeSlot)+ ",\t"
			    		+ Configure.getTOTAL_HOUR_AHEAD_AUCTIONS() + "," // HourAhead
			    		);
			    		//+ (totalCost/totalPower) + ", ");
			    pwOutput.print(SEED + "," + SUB_CASE_STUDY + "," + MCTSSimulation + ",\t" +(currentTimeSlot)+ ","
			    		+ Configure.getTOTAL_HOUR_AHEAD_AUCTIONS() + "," // HourAhead
			    		);
			    //pwOutputV.print(SEED + "," + SUB_CASE_STUDY + "," + MCTSSimulation + ",\t" +(currentTimeSlot)+ ","
			    //		+ Configure.getTOTAL_HOUR_AHEAD_AUCTIONS() + "," // HourAhead
			    //		);
			    if(agent.type == Agent.agentType.BROKER){
			    	System.out.printf("\t%s,\t%.2f,\t%.2f,\t%.2f,\t\t%.2f,\t\t%.2f,\t\t%.2f,\t\t%.2f,\t\t%.2f,\t%.2f,\t%.2f",agent.playerName, volbuy, buy, unitcost, volsell, sell, unitsell, penalty, profit, net,((clearedTotalMWh/neededTotalMWh)*100));
			    	pwOutput.printf("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,",agent.playerName, volbuy, buy, unitcost, volsell, sell, unitsell, penalty, profit, net,((clearedTotalMWh/neededTotalMWh)*100));
			    	//pwOutputV.printf("%.2f,",((clearedTotalMWh/neededTotalMWh)*100));
			    }
			    else{
			    	//System.out.printf("%.2f (%.2f),\t",net,((clearedTotalMWh/totalAskVolumeCleared)*100));
			    	//pwOutput.printf("%.2f,",net);
			    	//pwOutputV.printf("%.2f,",((clearedTotalMWh/totalAskVolumeCleared)*100));
			    }
			    printTrack++;
		    }
		    else{
		    	//System.out.print((totalCost/totalPower) + ", ");
		    	if(agent.type == Agent.agentType.BROKER){
		    		System.out.printf("\t%s,\t%.2f,\t%.2f,\t%.2f,\t\t%.2f,\t\t%.2f,\t\t%.2f,\t\t%.2f,\t\t%.2f,\t%.2f,\t%.2f",agent.playerName, volbuy, buy, unitcost, volsell, sell, unitsell, penalty, profit, net,((clearedTotalMWh/neededTotalMWh)*100));
		    		pwOutput.printf("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,",agent.playerName, volbuy, buy, unitcost, volsell, sell, unitsell, penalty, profit, net,((clearedTotalMWh/neededTotalMWh)*100));
		    		//pwOutputV.printf("%.2f,",((clearedTotalMWh/neededTotalMWh)*100));
		    	}
		    	else{
		    		//System.out.printf("%.2f (%.2f),\t",net,((clearedTotalMWh/totalAskVolumeCleared)*100));
		    		//pwOutput.printf("%.2f,",net);
		    		//pwOutputV.printf("%.2f,",((clearedTotalMWh/totalAskVolumeCleared)*100));
		    	}
			    //pwOutputVolume.print(((clearedTotalMWh/neededTotalMWh)*100) + "%,");
		    }
		    
		    if(agent.type == Agent.agentType.BROKER){
			    HashMap<String, ArrayList<Double>> track = results.get(SUB_CASE_STUDY);
				
				if(track == null)
				{
					track = new HashMap<String, ArrayList<Double>>();
					ArrayList<Double> vals = new ArrayList<Double>();
					vals.add(net);
					vals.add(1.00);
					track.put(brokerName, vals);
				}
				else{
					Double newVal = (net);
					ArrayList<Double> vals = track.get(brokerName);
					if(vals != null){
						vals.set(0,(vals.get(0)+newVal));
						Double counter = vals.get(1);
						vals.set(1,++counter);
						track.put(brokerName, vals);
					}
					else{
						vals = new ArrayList<Double>();
						vals.add(newVal);
						vals.add(1.00);
						track.put(brokerName, vals);
					}
					
				}
				
				results.put(SUB_CASE_STUDY, track);
		    }
		    
		    costTotal.put(brokerName, new double[6]);
		    neededTotalVolumes.put(brokerName, 0.0);
		    clearedTotalBidVolumes.put(brokerName, 0.0);
		    clearedTotalAskVolumes.put(brokerName, 0.0);
		}

		costTotal.clear();
		neededTotalVolumes.clear();
		clearedTotalBidVolumes.clear();
		clearedTotalAskVolumes.clear();
		currentTimeSlot = 0;
		
		System.out.printf("Mean Clearing Price : " + meanClearingPrice);
		pwOutput.println("MCTS RunTime,"+(nanoTime/nanoTimeCount));
		//pwOutput.println();
		//pwOutput.println("Mean Clearing Price," + meanClearingPrice);
		System.out.println(" Social Welfare Point : " + totalGreenAskVolumeCleared/totalAskVolumeCleared*100);
		//pwOutputV.println("Green Energy Cleared," + totalGreenAskVolumeCleared/totalAskVolumeCleared*100);
		/*
		// Printing error in prediction
		pwOutput.println("ha,avgmcp,count,err");
		double avg_err = 0;
		double count = 0;
		for(int i=0; i<Configure.getTOTAL_HOUR_AHEAD_AUCTIONS();i++)
		{
			if(MCPriceCount[i] == 0)
				MCPriceCount[i]=1;
			pwOutput.println(i+","+MCPrice[i]/MCPriceCount[i]+","+minCPHourAhead[i]+","+pp_error_ha[i]/pp_error_ha_count[i]);
			MCPrice[i]=0;
			MCPriceCount[i]=0;
			minCPHourAhead[i]=0;
			avg_err += pp_error_ha[i];
			count += pp_error_ha_count[i];
			pp_error_ha[i] = 0;
			pp_error_ha_count[i] = 0;
		}
		avg_err /= count;
		System.out.println("PP err " + avg_err);
		pwOutput.println("Avg Error " + avg_err);
		*/
		pwOutput.close();
		fwOutput.close();
		//pwOutputV.close();
		//fwOutputV.close();
		totalAskVolumeCleared = 0;
		totalGreenAskVolumeCleared = 0;
		meanClearingPrice = 0;
		clearedAuctionCount = 0;
		//printNamesCount = 0;
		nanoTime = 0;
	    nanoTimeCount = 0;
	}
	
	public void doBalancing(){
		double balancingPrice = arrBalacingPrice[currentTimeSlot];
		boolean flag = true;
		for (Agent a : agents) {
		    
			String brokerName = a.playerName;
			int brokerid = a.id;
		    double clearedBalancingMWh = Math.abs(a.neededMWh);
		    if(clearedBalancingMWh > 0) {
			    if(a.playerName.equalsIgnoreCase("MCTSX")){
			    	mctsxRealCostPerHour -= (clearedBalancingMWh*balancingPrice);
			    	mctsxPredictedCostPerHour += (clearedBalancingMWh*balancingPrice);
			    	mctsxRealCost[(day*Configure.getHOURS_IN_A_DAY())+hour][Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()] = clearedBalancingMWh*balancingPrice*(-1);
			    }
			    if(flag) {//a.playerName.equalsIgnoreCase("TacTex")) {
			    	pricepredictor.shortBalanceTransactionsData.add(new ChargeMwhPair(balancingPrice*clearedBalancingMWh, clearedBalancingMWh));
			    	flag = false;
			    }
			    //addTotalClearTrade(new Bid(brokerName,0,clearedBalancingMWh));
			    //addTotalClearTrade(new Ask(brokerName,0,clearedAskMWh));
			    if(a.type == Agent.agentType.BROKER)
			    	addTotalTradeCost(new Bid(brokerName,brokerid,balancingPrice,clearedBalancingMWh,a.type),COST_ARRAY_INDICES.VOL_BAL.getValue(),COST_ARRAY_INDICES.TOT_BAL.getValue());
			    else 
			    	addTotalTradeCost(new Ask(brokerName,brokerid,balancingPrice,clearedBalancingMWh,a.type),COST_ARRAY_INDICES.VOL_BAL.getValue(),COST_ARRAY_INDICES.TOT_BAL.getValue());
		    }
		}
	}
	
	public double getMinClearingPrice(double [] arrClearingPrice){
		double minClrPrice = Double.MAX_VALUE;
		for(int i = 0; i <= Configure.getTOTAL_HOUR_AHEAD_AUCTIONS(); i++){
			if(minClrPrice > arrClearingPrice[i])
				minClrPrice = arrClearingPrice[i];
		}
		return minClrPrice;
	}
	
	
	public void clear(){
		clearedAsks.clear();
		clearedBids.clear();
	}
	
	public int getNumberOfClearedBids(String brokerName){
		int count = 0;
		for(Bid bid : clearedBids){
			if(bid.agentName.equalsIgnoreCase(brokerName))
				count++;
		}
		return count;
	}
}
