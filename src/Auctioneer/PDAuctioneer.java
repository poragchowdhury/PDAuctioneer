package Auctioneer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import configure.Configure;

import Agents.Agent;
import Agents.Agent.agentType;
import Agents.SPOT;
import Agents.ZI;
import Agents.MCTSAgent;
import Agents.Producer;
import Agents.ZIP;
import Observer.CaseStudies;
import Observer.Observer;

public class PDAuctioneer {
	private int day;
	private int hour;
	private int currentTimeSlot;
	private int hourAhead;
	public ArrayList<Bid> bids;
	public ArrayList<Ask> asks;
	private double defaultMargin = 0.05;
	private double sellerSurplusRatio = 0.5;
	private double sellerMaxMargin = 0.05;
	public Observer observer;
	public boolean booPrintData = true;
	public static FileWriter fwriter;
	public static PrintWriter logger;

	public PDAuctioneer(){
		observer = new Observer();
		day = 0;
		hour = 0;
		currentTimeSlot = 0;
		hourAhead = 0;
		observer.GREEN_AUCTION_FLAG = false;

		bids = new ArrayList<Bid>();
		asks = new ArrayList<Ask>();
	}

	public void startSimulation() throws Exception{

		// import agent data from the csv files
		//observer.importProducerData();
		observer.setTime(day,hour,hourAhead, currentTimeSlot);
		
		for(observer.SUB_CASE_STUDY = 1; observer.SUB_CASE_STUDY <=Configure.getMAX_OF_SUB_CASE_STUDY(); observer.SUB_CASE_STUDY++){
			observer.printFlag = true;
			
			for(int simCase = 0; simCase <= Configure.getNO_OF_MCTS_CASES(); simCase++) {
				
				observer.results = new HashMap<Integer, HashMap<String, ArrayList<Double>>>();
				
				if(simCase == 0){
					// for normal cases this will execute but there is a 
					// DIFF_MCTS_SIM_CASE_EXP check which will not assign this value
					observer.MCTSSimulation = Configure.getVARMCTSAGENTITERATION();//100;
				}
				else if(simCase == 1){
					observer.MCTSSimulation = 1000;
				}
				else if(simCase == 2){
					observer.MCTSSimulation = 5000;
				}
				else if(simCase == 3){
					observer.MCTSSimulation = 10000;
				}
				else if(simCase == 4){
					observer.MCTSSimulation = 15000;
				}
				else if(simCase == 5){
					observer.MCTSSimulation = 25000;
				}
				else if(simCase == 6){
					observer.MCTSSimulation = 50000;
				}
				else{
					System.out.println("Mcts simulation case missmatch!");
					return;
				}
				
				// Different seed means different simulation cases
				for(observer.SEED = Configure.getSTART_SEED_NO(); observer.SEED <= Configure.getEND_SEED_NO(); observer.SEED++) {

					//observer.SEED = Configure.getSEED_NO();
					
					observer.importPowerTACData();
					// Simulate balancing prices
					observer.generateBalancingPrices(0,0);
					
					// randomizing the case studies
					//observer.CASE_STUDY = observer.SEED/4 + 1;

					/////////////////////////////////////
					// Generate the configuration of the simulation
					double neededMWhBroker = new CaseStudies().configureSimulation(observer);// configureSimulationTraining();

					currentTimeSlot = 0;
					for(day = 0; day < Configure.getTOTAL_SIM_DAYS(); day++){
						
						for(hour = 0; hour < Configure.getHOURS_IN_A_DAY(); hour++){
							for(Agent agent:observer.agents){
								if(agent.type == agentType.BROKER){
									if(agent.playerName.equalsIgnoreCase("MCTSX")) {
										//neededMWhBroker = Configure.getPERHOURENERGYDEMAND()*Configure.getMCTSBROKERDEMANDPERC();
										neededMWhBroker = (Configure.getPERHOURENERGYDEMAND() * Configure.avgSupply_REPTree[observer.SEED-1])*Configure.getMCTSBROKERDEMANDPERC();
									}
									else{
										//neededMWhBroker = Configure.getPERHOURENERGYDEMAND()*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));
										neededMWhBroker = (Configure.getPERHOURENERGYDEMAND() * Configure.avgSupply_REPTree[observer.SEED-1])*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));
									}
										
									observer.addneededTotalVolumes(agent.playerName,neededMWhBroker);
								}
								else{
									observer.addneededTotalVolumes(agent.playerName,observer.arrProducerBidVolume[agent.id][observer.currentTimeSlot]);
								}
							}

							observer.setNeededVolumes();
							
							double minClearingPrice = 1000.0;
							int minHourAhead = 0;
							
							for(hourAhead = Configure.getTOTAL_HOUR_AHEAD_AUCTIONS()-1; hourAhead >= 0; hourAhead--){

								observer.setTime(day,hour,hourAhead, currentTimeSlot);

								// Use dppredictor or not
								if(Configure.getUSE_MDP_PREDICTOR()) {
									if (observer.pricepredictor.canRunDP()) {
										if(!observer.pricepredictor.dpCache2013.isValid(observer.currentTimeSlot))
											observer.pricepredictor.runDP2013(100, observer.currentTimeSlot);
									}
								}
								////System.out.println("*******************AUCTION*******************");
								//System.out.println("Day "+ day + " Hour " + hour + " HourAhead " + hourAhead);

								// get the bids and asks from the agents
								for(Agent agent : observer.agents){
									agent.submitOrders(bids, asks, observer);
								}

								// clear the auction
								// Add cleared asks and bids to clearBids , clearAsks, clearedVolumesBids & clearedVolumesAsks
								double clearingPrice = 0.0;
								if(observer.GREEN_AUCTION_FLAG)
									clearingPrice = clearGreenAuction(asks, bids);
								else
									clearingPrice = clearAuction(asks, bids);

								if(minClearingPrice > clearingPrice)
								{
									if(clearingPrice == 0)
										System.out.println("This should not happen. Clearing Price 0");
									else {
										minClearingPrice = clearingPrice;
										minHourAhead = observer.hourAhead;
									}
								}
								double pcp = observer.pricepredictor.getPrice(observer.hourAhead);
								if(clearingPrice != 0) {
									observer.MCPrice[observer.hourAhead] += clearingPrice; 
									observer.MCPriceCount[observer.hourAhead]++; 
									observer.movingAvgErrorMCP[observer.hourAhead] = 0;//(observer.movingAvgErrorMCP[observer.hourAhead] * 0.95) + ((pcp-clearingPrice) * 0.05);
									//observer.updateMean(clearingPrice);
									//observer.updateSTDDEV();
								}
								
								// update clear trades for corresponding agents
								// Get the values from clearedVolumesBids and clearedVolumesAsks
								// Updates to clearedTotalBidVolumes, clearedTotalAskVolumes
								// Add to costTotal
								observer.printClearedVolume(clearingPrice);

								// Adjust agent's needed energy
								// rest clearedVolumes, clearedVolumesBids and clearedVolumesAsks
								observer.adjustNeededVolumes();

								//observer.updateNumberOfSuccessfullBids(SPOT2.playerName);
								// Debug clearing price
								
								double percentage_error = Math.abs((clearingPrice-pcp)/pcp)*100;
								if(percentage_error!=0) {
									observer.pp_error_ha[observer.hourAhead]+=percentage_error;
									observer.pp_error_ha_count[observer.hourAhead]++;
								}
								System.out.println("hour " + observer.hour + " hourAhead " + observer.hourAhead + " cp " + clearingPrice + " pcp " + pcp + " err " + percentage_error + " mvn err " + observer.movingAvgErrorMCP[observer.hourAhead]);
								
								// clean auctioner's bids asks
								asks.clear();
								bids.clear();
								observer.clear();
							}
							observer.minCPHourAhead[minHourAhead]++;
							observer.doBalancing();
							currentTimeSlot++;
							observer.currentTimeSlot++;
							if(observer.DEBUG)
								System.out.println();
							
							// Print to the error log
							/*
							FileWriter fwOutput = new FileWriter("mcts_prediction_error.csv", true);
							PrintWriter pwOutput = new PrintWriter(new BufferedWriter(fwOutput));
							//pwOutput.println(observer.MCTSSimulation + "," + observer.hour + "," + (observer.mctsxRealCostPerHour + observer.mctsxPredictedCostPerHour));
							pwOutput.println(observer.MCTSSimulation + "," + observer.hour + "," + (observer.mctsxRealCostPerHour + observer.mctsxPredictedCostPerHour));
							pwOutput.close();
							fwOutput.close();
							*/
							// Reset the calculcation flags
							observer.mctsxPredictedCostPerHour = 0;
							observer.mctsxRealCostPerHour = 0;
							
						}
					}

					//System.out.println("******************FINAL******************");
					try {
						// Printing the output to file
						// Reset clearedTotalBidVolumes and clearedTotalAskVolumes
						// Reset neededTotalVolumes
						// Rest costTotal
						observer.printTotalClearedVolume();
						//observer.calculateError();
						observer.writeMCTSMoves();
						//logger.println("SEED COMPLETED " + observer.SEED);

					} catch (IOException e) {
						e.printStackTrace();
					}
					//break;
				}
				//System.out.println("**************************************\n");
				observer.printFlag = true;
				// Print the average results
				//printAvgResults();
				observer.printSTDDEV();
			}
		}
		//System.out.println("TOT_HA " + observer.pricepredictor.hourAheadAuctions.size() + " Array : " + observer.pricepredictor.hourAheadAuctions.toString());
	}

	public void printAvgResults(){
		FileWriter fwOutput;
		try {
			fwOutput = new FileWriter("Results_Price.csv", true);
			PrintWriter pwOutput = new PrintWriter(new BufferedWriter(fwOutput));
			for(Integer key : observer.results.keySet()){
				pwOutput.printf("Case Study," + key + ",");
				System.out.printf("Case Study," + key + ",");
				HashMap<String, ArrayList<Double>> track = observer.results.get(key);
				for(String Agentkey : track.keySet()){
					pwOutput.printf(",Agent," + Agentkey + ",");
					System.out.printf("Agent," + Agentkey + ",");
					ArrayList<Double> vals = track.get(Agentkey);
					pwOutput.printf("Cost," + Agentkey + "," + vals.get(0)/vals.get(1) + ",");
					System.out.printf("Cost," + Agentkey + "," + vals.get(0)/vals.get(1) + ",");
				}
			}
			pwOutput.println();
			System.out.println();
			pwOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String [] args) throws IOException{
		PDAuctioneer auctioneer = new PDAuctioneer();
		String fileName = Configure.getRESULT_FILE() + "-training.arff";
		fwriter = new FileWriter(fileName, true);
		logger = new PrintWriter(new BufferedWriter(fwriter));

		logger.println("@RELATION training");
		logger.println();
		logger.println("@ATTRIBUTE currentTimeSlot NUMERIC");
		logger.println("@ATTRIBUTE hour NUMERIC");
		logger.println("@ATTRIBUTE hourAhead NUMERIC");
		logger.println("@ATTRIBUTE date NUMERIC");
		logger.println("@ATTRIBUTE month NUMERIC");
		logger.println("@ATTRIBUTE year NUMERIC");
		logger.println("@ATTRIBUTE NUMBER_OF_BROKERS NUMERIC");
		logger.println("@ATTRIBUTE NUMBER_OF_PRODUCERS NUMERIC");
		logger.println("@ATTRIBUTE PrevDayHAMarketClearingPrice NUMERIC");
		logger.println("@ATTRIBUTE PrevHAMarketClearingPrice NUMERIC");
		logger.println("@ATTRIBUTE CloudCoverage NUMERIC");
		logger.println("@ATTRIBUTE Temperature NUMERIC");
		logger.println("@ATTRIBUTE WSpeed NUMERIC");
		logger.println("@ATTRIBUTE WDirection NUMERIC");
		logger.println("@ATTRIBUTE MarketClearingPrice NUMERIC");
		logger.println();
		logger.println("@DATA");
		logger.println();

		try{
			auctioneer.startSimulation();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		logger.close();
		fwriter.close();
	}



	public double clearAuction(ArrayList<Ask> asks, ArrayList<Bid> bids){
		double clearingPrice = 0;
		double PrevDayHAMarketClearingPrice = 0;
		double PrevHAMarketClearingPrice = 0;
		Collections.shuffle(asks);
		Collections.shuffle(bids);
		Collections.sort(asks);
		Collections.sort(bids);
		
		if(observer.currentTimeSlot > 23)
			PrevDayHAMarketClearingPrice = observer.arrMarketClearingPriceHistory[observer.currentTimeSlot-24][observer.hourAhead];
		
		if(observer.hourAhead != Configure.getTOTAL_HOUR_AHEAD_AUCTIONS())
			PrevHAMarketClearingPrice = observer.arrMarketClearingPriceHistory[observer.currentTimeSlot][observer.hourAhead+1];
		Ask oldask = new Ask();
		Bid oldbid = new Bid();

		int askid = 0;
		int bidid = 0;

		for(;askid < asks.size();){

			if(bidid >= bids.size()){
				// bid is crossing its own limit
				if(bidid == 0 && bids.size() == 0){
					if(observer.DEBUG)
						System.out.println(observer.getTime() + "Empty bid quque! Auction didn't clear.");
					return 0;
				}

				if((oldask.price !=null && oldbid.price!= null) && (oldask.price == Double.MIN_VALUE || oldbid.price == 0)){
					if(observer.DEBUG)
						System.out.println(observer.getTime() + "0 prices! Auction didn't clear.");
					return 0;
				}
				
				clearingPrice = getTheClearingPrice(oldask, oldbid, 0.0);
				/*
				if(oldbid.price == null)
					clearingPrice = (double) (Math.abs(oldask.price)*(1+defaultMargin));
				else
					clearingPrice = (double) ((Math.abs(oldask.price)+oldbid.price)/2);
				*/	
				if(observer.DEBUG) 
					System.out.println(observer.getTime() + "1 Clearing price is : " + clearingPrice + " askid " + oldask.agentID + ", price " + oldask.price);
				observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
				observer.clearedAuctionCount++;
				if(clearingPrice != 0 && booPrintData)
					logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," + observer.getWeatherPrediction() + "," +clearingPrice);
				return clearingPrice;
			}
			for(;bidid < bids.size();){
				if(askid >= asks.size()){
					// ask is crossing its own limit size
					if((oldask.price !=null && oldbid.price!= null) && (oldask.price == Double.MIN_VALUE || oldbid.price == 0)){
						if(observer.DEBUG)
							System.out.println(observer.getTime() + "0 prices! Auction didn't clear.");
						return 0;
					}
					
					clearingPrice = getTheClearingPrice(oldask, oldbid, 0.0);
					/*
					if(oldbid.price == null)
						clearingPrice = (double) (Math.abs(oldask.price)*(1+defaultMargin));
					else
						clearingPrice = (double) ((Math.abs(oldask.price)+oldbid.price)/2);
					*/
					
					if(observer.DEBUG) 
						System.out.println(observer.getTime() + "2 Clearing price is : " + clearingPrice);
					observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
					observer.clearedAuctionCount++;
					if(clearingPrice != 0 && booPrintData)
						logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," + observer.getWeatherPrediction() + "," +clearingPrice);
					return clearingPrice;
				}
				Bid bid = bids.get(bidid);
				Ask ask = asks.get(askid);
				// Check prices
				if(bid.price != null && ask.price != null && bid.price < Math.abs(ask.price)){
					if(bidid == 0 && askid == 0){
						// Auction will not clear;
						if(observer.DEBUG)
							System.out.println(observer.getTime() + "No bids left. Auction did not clear.");
						return 0;
					}
					else{
						clearingPrice = getTheClearingPrice(oldask, oldbid, ask.price);
/*						if(oldbid.price == null)
							clearingPrice = (double) (Math.abs(oldask.price)*(1+defaultMargin));
						else {
							clearingPrice = Math.abs(oldask.price) + sellerSurplusRatio * (Math.abs(oldbid.price) - Math.abs(oldask.price));
						    clearingPrice = Math.min(clearingPrice, Math.abs(oldask.price) * (1.0 + sellerMaxMargin));
////						clearingPrice = ((Math.abs(oldask.price)+oldbid.price)/2);
						}
*/
						if(observer.DEBUG)
							System.out.println(observer.getTime() + "3 Clearing price is : " + clearingPrice);
						observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
						observer.clearedAuctionCount++;
						if(clearingPrice != 0 && booPrintData)
							logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," + observer.getWeatherPrediction() + "," +clearingPrice);
						return clearingPrice;
					}
				}
				// Clear Auction
				else
				{
					// Check quantities
					if(bid.amount == ask.amount){
						// clear both bid and ask
						askid++;
						bidid++;
						if(observer.DEBUG){
							System.out.println("Clearing bid :" + bid.toString());
							System.out.println("Clearing ask :" + ask.toString());
						}
						observer.addClearedTrades(new Bid(bid.agentName,-1, 0.0, ask.amount, bid.agentType));
						observer.addClearedTrades(new Ask(ask.agentName,-1, 0.0, ask.amount, ask.agentType));
					}
					else if(bid.amount > ask.amount){
						// clear part of the bid and ask
						askid++;
						if(observer.DEBUG){
							System.out.println(bid.toString() + " Bid Partially cleared :" + ask.amount);
							System.out.println(ask.toString());
						}
						observer.addClearedTrades(new Bid(bid.agentName, -1, 0.0, ask.amount,bid.agentType));
						observer.addClearedTrades(new Ask(ask.agentName, -1, 0.0, ask.amount,ask.agentType));
						bid.amount = bid.amount - ask.amount;
						ask.amount = 0;
					}
					else{
						// clear part of the ask and whole bid
						bidid++;
						if(observer.DEBUG){
							System.out.println(bid.toString());
							System.out.println(ask.toString() + " Ask Partially cleared :" + bid.amount);
						}
						observer.addClearedTrades(new Bid(bid.agentName,-1, 0.0, bid.amount,bid.agentType));
						observer.addClearedTrades(new Ask(ask.agentName,-1, 0.0, bid.amount,ask.agentType));
						ask.amount = ask.amount - bid.amount;
						bid.amount = 0;
					}
					oldask = ask;
					oldbid = bid;
				}
			}
		}

		if(oldask.price == Double.MIN_VALUE)
		{
			if(observer.DEBUG)
				System.out.println(observer.getTime() + "Auction didn't clear.");
			return 0;
		}
		else{
			//double clearingprice = (double)(Math.abs(oldask.price+oldbid.price)/2;
			clearingPrice = getTheClearingPrice(oldask, oldbid, 0.0);
/*			if(oldbid.price == null)
				clearingPrice = (double) (Math.abs(oldask.price)*(1+defaultMargin));
			else
				clearingPrice = ((Math.abs(oldask.price)+oldbid.price)/2);
*/		
			//if(observer.DEBUG)
				System.out.println(observer.getTime() + " Clearing price: " + clearingPrice);
				if(clearingPrice != 0 && booPrintData)
					logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," + observer.getWeatherPrediction() + "," +clearingPrice);
				return clearingPrice;
		}
	}

	double getTheClearingPrice(Ask oldask, Bid oldbid, Double askprice) {
		  double clearingPrice;
		  double defaultClearingPrice = 40.0;
	      if (oldbid.price != null) {
		        if (oldask.price != null) {
		          clearingPrice =
		              oldask.price + sellerSurplusRatio * (oldbid.price - oldask.price);
		          clearingPrice =
		              Math.min(clearingPrice, oldask.price * (1.0 + sellerMaxMargin));
		        }
		        else {
		          // ask price is null
		          clearingPrice = oldbid.price / (1.0 + defaultMargin);
		          //log.info("market clears at " + clearingPrice + " with null ask price");
		        }
	      }
	      else {
		        // bid price is null
		        if (oldask.price != null) {
		          clearingPrice = oldask.price * (1.0 + defaultMargin);
		          //log.info("market clears at " + clearingPrice + " with null bid price");
		        }
		        else {
		          // both bid and ask are null
		          clearingPrice = defaultClearingPrice;
		          //log.info("market clears at default clearing price"  + clearingPrice);
		        }
	      }
	      
	      if(oldask.price != null)
	    	  observer.lowestAskPrice = null;
	      else { 
	    	  if(oldask.price == null || askprice == null)
	    		  observer.lowestAskPrice = null;
	    	  else if(askprice > oldask.price)
	    		  observer.lowestAskPrice = askprice;
	    	  else 
	    		  observer.lowestAskPrice = oldask.price;
	      }
	      return clearingPrice;
	}
	
	public double clearGreenAuction(ArrayList<Ask> asks, ArrayList<Bid> bids){
		double clearingPrice = 0;
		Collections.sort(asks);
		Collections.sort(bids);

		Ask oldask = new Ask();
		Bid oldbid = new Bid();

		int askid = 0;
		int bidid = 0;

		for(;askid < asks.size();){

			if(bidid >= bids.size()){
				// bid is crossing its own limit size
				if(bidid == 0 && bids.size() == 0){
					if(observer.DEBUG)
						System.out.println(observer.getTime() + "Empty bid quque! Auction didn't clear.");
					return 0;
				}

				if(oldask.price == 0 || oldbid.price == 0){
					if(observer.DEBUG)
						System.out.println(observer.getTime() + "0 prices! Auction didn't clear.");
					return 0;
				}
				clearingPrice = (double)((Math.abs(oldask.price*observer.arrProducerGreenPoints[oldask.agentID])+oldbid.price)/2);
				if(observer.DEBUG)
					System.out.println(observer.getTime() + "Clearing price is : " + clearingPrice + " askid " + oldask.agentID + ", price " + Math.abs(oldask.price*observer.arrProducerGreenPoints[oldask.agentID]));
				observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
				observer.clearedAuctionCount++;
				return clearingPrice;
			}

			// iterate all valid bids			
			for(;bidid < bids.size();){
				if(askid >= asks.size()){
					// ask is crossing its own limit size
					if(oldask.price == 0 || oldbid.price == 0){
						if(observer.DEBUG)
							System.out.println(observer.getTime() + "0 prices! Auction didn't clear.");
						return 0;
					}
					clearingPrice = (double) ((Math.abs(oldask.price*observer.arrProducerGreenPoints[oldask.agentID])+oldbid.price)/2);
					if(observer.DEBUG)
						System.out.println(observer.getTime() + "Clearing price is : " + clearingPrice);
					observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
					observer.clearedAuctionCount++;
					return clearingPrice;
				}
				Bid bid = bids.get(bidid);
				Ask ask = asks.get(askid);
				// Check prices
				double realaskprice = (double) Math.abs(ask.price*observer.arrProducerGreenPoints[ask.agentID]);
				if(bid.price < realaskprice){
					askid++;
					if(observer.DEBUG)
						System.out.println("Break and Increasing ask id");
					break;
				}
				// Clear Auction
				else
				{
					// Check amounts to clear
					if(bid.amount == ask.amount){
						// clear both bid and ask
						askid++;
						bidid++;
						if(observer.DEBUG){
							System.out.println("Clearing bid: " + bid.toString());
							System.out.println("Clearing ask: " + ask.toString());
						}
						observer.addClearedTrades(new Bid(bid.agentName,bid.agentID, 0.0, ask.amount, bid.agentType));
						observer.addClearedTrades(new Ask(ask.agentName,ask.agentID, 0.0, ask.amount, ask.agentType));
					}
					else if(bid.amount > ask.amount){
						// clear part of the bid and ask
						askid++;
						if(observer.DEBUG){
							System.out.println(bid.toString() + " Bid Partially cleared :" + ask.amount);
							System.out.println(ask.toString());
						}
						observer.addClearedTrades(new Bid(bid.agentName,bid.agentID, 0.0, ask.amount, bid.agentType));
						observer.addClearedTrades(new Ask(ask.agentName,ask.agentID, 0.0, ask.amount, ask.agentType));
						bid.amount = bid.amount - ask.amount;
						ask.amount = 0;
					}
					else{
						// clear part of the ask and whole bid
						bidid++;
						if(observer.DEBUG){
							System.out.println(bid.toString());
							System.out.println(ask.toString() + " Ask Partially cleared :" + bid.amount);
						}
						observer.addClearedTrades(new Bid(bid.agentName,bid.agentID, 0.0, bid.amount,bid.agentType));
						observer.addClearedTrades(new Ask(ask.agentName,ask.agentID, 0.0, bid.amount,ask.agentType));
						ask.amount = ask.amount - bid.amount;
						bid.amount = 0;
					}

					if(oldask.price*observer.arrProducerGreenPoints[oldask.agentID] < ask.price*observer.arrProducerGreenPoints[ask.agentID])
						oldask = ask;	

					oldbid = bid;
				}
			}
		}

		if(oldask.price == Integer.MIN_VALUE)
		{
			if(observer.DEBUG)
				System.out.println(observer.getTime() + "Auction didn't clear.");
			return 0;
		}
		else{
			double clearingprice = (double) (Math.abs(oldask.price*observer.arrProducerGreenPoints[oldask.agentID])+oldbid.price)/2;
			if(observer.DEBUG)
				System.out.println(observer.getTime() + " Clearing price: " + clearingprice);
			return clearingprice;
		}
	}
}
