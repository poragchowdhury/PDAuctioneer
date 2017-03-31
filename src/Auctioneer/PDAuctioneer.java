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

	public Observer observer;


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
		observer.importProducerData();

		observer.setTime(day,hour,hourAhead, currentTimeSlot);
		
		for(observer.SUB_CASE_STUDY = 1; observer.SUB_CASE_STUDY <=Configure.getMAX_OF_SUB_CASE_STUDY(); observer.SUB_CASE_STUDY++){
			observer.printFlag = true;
			
			for(int simCase = 0; simCase <= Configure.getNO_OF_MCTS_CASES(); simCase++) {
				
				observer.results = new HashMap<Integer, HashMap<String, ArrayList<Double>>>();
				
				if(simCase == 0){
					// for normal cases this will execute but there is a 
					// DIFF_MCTS_SIM_CASE_EXP check which will not assign this value
					observer.MCTSSimulation = 10;
				}
				else if(simCase == 1){
					observer.MCTSSimulation = 100;
				}
				else if(simCase == 2){
					observer.MCTSSimulation = 1000;
				}
				else if(simCase == 3){
					observer.MCTSSimulation = 5000;
				}
				else if(simCase == 4){
					observer.MCTSSimulation = 10000;
				}
				else if(simCase == 5){
					observer.MCTSSimulation = 15000;
				}
				else if(simCase == 6){
					observer.MCTSSimulation = 25000;
				}
				else{
					System.out.println("Mcts simulation case missmatch!");
					return;
				}
				
				// Different seed means different simulation cases
				for(observer.SEED = 1; observer.SEED <= Configure.getSEED_NO(); observer.SEED++) {

					// Simulate balancing prices
					observer.generateBalancingPrices(0,0);
					
					// randomizing the case studies
					//observer.CASE_STUDY = observer.SEED/4 + 1;

					/////////////////////////////////////
					// Generate the configuration of the simulation
					double neededMWhBroker = new CaseStudies().configureSimulation(observer);// configureSimulationTraining();

					for(int i = 0; i < Configure.getNUMBER_OF_PRODUCERS(); i++){
						Agent producer = new Producer("Producer"+i, i, 0, 0, 0);
						observer.addAgents(producer);
					}

					currentTimeSlot = 0;
					for(day = 0; day < Configure.getTOTAL_SIM_DAYS(); day++){
						for(hour = 0; hour < Configure.getHOURS_IN_A_DAY(); hour++){
							for(Agent agent:observer.agents){
								if(agent.type == agentType.BROKER){
									if(agent.playerName.equalsIgnoreCase("MCTSAgent"))
										neededMWhBroker = Configure.getPERHOURENERGYDEMAND()*Configure.getMCTSBROKERDEMANDPERC();
									else{
										neededMWhBroker = Configure.getPERHOURENERGYDEMAND()*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));
									}
										
									observer.addneededTotalVolumes(agent.playerName,neededMWhBroker);
								}
								else{
									observer.addneededTotalVolumes(agent.playerName,observer.arrProducerBidVolume[agent.id][observer.currentTimeSlot]);
								}
							}

							observer.setNeededVolumes();

							for(hourAhead = Configure.getTOTAL_HOUR_AHEAD_AUCTIONS(); hourAhead >= 0; hourAhead--){

								observer.setTime(day,hour,hourAhead, currentTimeSlot);

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

								// update clear trades for corresponding agents
								// Get the values from clearedVolumesBids and clearedVolumesAsks
								// Updates to clearedTotalBidVolumes, clearedTotalAskVolumes
								// Add to costTotal
								observer.printClearedVolume(clearingPrice);

								// Adjust agent's needed energy
								// rest clearedVolumes, clearedVolumesBids and clearedVolumesAsks
								observer.adjustNeededVolumes();

								//observer.updateNumberOfSuccessfullBids(SPOT2.playerName);

								// clean auctioner's bids asks
								asks.clear();
								bids.clear();
								observer.clear();

								////System.out.println("**************************************\n");
							}

							observer.doBalancing();

							currentTimeSlot++;
							observer.currentTimeSlot++;
							if(observer.DEBUG)
								System.out.println();
						}
					}

					//System.out.println("******************FINAL******************");
					try {
						// Printing the output to file
						// Reset clearedTotalBidVolumes and clearedTotalAskVolumes
						// Reset neededTotalVolumes
						// Rest costTotal
						observer.printTotalClearedVolume();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				//System.out.println("**************************************\n");
				observer.printFlag = true;
				// Print the average results
				//printAvgResults();
			}
		}
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

	
	/*
	public void run4brokerGames(double neededMWhBroker){
		observer.agents.clear();
		observer.NUMBER_OF_BROKERS = 4;
		observer.NUMBER_OF_PRODUCERS = 5;
		observer.GREEN_AUCTION_FLAG = false;

		Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);
		Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 30, 10);
		Agent SPOT = new SPOT("SPOT", 0, neededMWhBroker, 30, 10);
		Agent MCTSAgent1K = new MCTSAgent("MCTSAgent",0, neededMWhBroker,30,10,1000);

		Random r = new Random();
		int  n = r.nextInt(6) + 0;

		if(n == 0){
			observer.addAgents(ZIP);
			observer.addAgents(ZI);
			observer.addAgents(MCTSAgent1K);
			observer.addAgents(SPOT);
		}
		else if(n == 1){
			observer.addAgents(ZIP);
			observer.addAgents(MCTSAgent1K);
			observer.addAgents(ZI);
			observer.addAgents(SPOT);
		}
		else if(n == 2)
		{
			observer.addAgents(ZIP);
			observer.addAgents(MCTSAgent1K);
			observer.addAgents(SPOT);
			observer.addAgents(ZI);
		}
		else if(n == 3){
			observer.addAgents(ZIP);
			observer.addAgents(ZI);
			observer.addAgents(SPOT);
			observer.addAgents(MCTSAgent1K);
		}
		else if(n == 4){
			observer.addAgents(MCTSAgent1K);
			observer.addAgents(ZIP);
			observer.addAgents(ZI);
			observer.addAgents(SPOT);
		}
		else{
			observer.addAgents(MCTSAgent1K);
			observer.addAgents(ZI);
			observer.addAgents(ZIP);
			observer.addAgents(SPOT);
		}
		observer.NUM_MCTS_SIM = 3;
	}
*/

	
/*
	public void configureSimulation2(double neededMWhBroker) throws Exception{
		observer.NUM_MCTS_SIM = 0;
		if(observer.CASE_STUDY == 1){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 5;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS_10K = new MCTSAgent("MCTSAgent-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS_10K);

			Agent MCTS1_10K = new MCTSAgent("MCTSAgent1-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS1_10K);

			Agent MCTS2_10K = new MCTSAgent("MCTSAgent2-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS2_10K);

			Agent MCTS3_10K = new MCTSAgent("MCTSAgent3-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS3_10K);

			Agent MCTS4_10K = new MCTSAgent("MCTSAgent4-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS4_10K);
		}
		else if(observer.CASE_STUDY == 2){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 5;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZI = new ZI("ZI",0, neededMWhBroker,30,10);
			observer.addAgents(ZI);

			Agent MCTS1_10K = new MCTSAgent("MCTSAgent1-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS1_10K);

			Agent MCTS2_10K = new MCTSAgent("MCTSAgent2-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS2_10K);

			Agent MCTS3_10K = new MCTSAgent("MCTSAgent3-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS3_10K);

			Agent MCTS4_10K = new MCTSAgent("MCTSAgent4-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS4_10K);
		}
		else if(observer.CASE_STUDY == 3){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 5;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZIP = new ZI("ZIP",0, neededMWhBroker,30,10);
			observer.addAgents(ZIP);

			Agent MCTS1_10K = new MCTSAgent("MCTSAgent1-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS1_10K);

			Agent MCTS2_10K = new MCTSAgent("MCTSAgent2-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS2_10K);

			Agent MCTS3_10K = new MCTSAgent("MCTSAgent3-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS3_10K);

			Agent MCTS4_10K = new MCTSAgent("MCTSAgent4-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS4_10K);
		}
		else if(observer.CASE_STUDY == 4){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 5;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent SPOT = new SPOT("SPOT",0, neededMWhBroker,30,10);
			observer.addAgents(SPOT);

			Agent MCTS1_10K = new MCTSAgent("MCTSAgent1-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS1_10K);

			Agent MCTS2_10K = new MCTSAgent("MCTSAgent2-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS2_10K);

			Agent MCTS3_10K = new MCTSAgent("MCTSAgent3-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS3_10K);

			Agent MCTS4_10K = new MCTSAgent("MCTSAgent4-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS4_10K);
		}
		else if(observer.CASE_STUDY == 5){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 5;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS_1K = new MCTSAgent("MCTSAgent-1K",0, neededMWhBroker,30,10,1000);
			observer.addAgents(MCTS_1K);

			Agent MCTS1_10K = new MCTSAgent("MCTSAgent1-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS1_10K);

			Agent MCTS2_10K = new MCTSAgent("MCTSAgent2-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS2_10K);

			Agent MCTS3_10K = new MCTSAgent("MCTSAgent3-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS3_10K);

			Agent MCTS4_10K = new MCTSAgent("MCTSAgent4-10K",0, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS4_10K);
		}
	}
*/

/*
	public void configureSimulation1(double neededMWhBroker) throws Exception{
		if(observer.CASE_STUDY == 1){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);
			observer.addAgents(ZI);

			Agent ZI1 = new ZI("ZI1", 1, neededMWhBroker, 30, 10);
			observer.addAgents(ZI1);

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 2){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZIP = new ZI("ZIP", 0, neededMWhBroker, 30, 10);
			observer.addAgents(ZIP);

			ZIP = new ZI("ZIP1", 1, neededMWhBroker, 30, 10);
			observer.addAgents(ZIP);

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 3){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent SPOT = new SPOT("SPOT", 0, neededMWhBroker, 30, 10);
			observer.addAgents(SPOT);

			SPOT = new SPOT("SPOT1",1, neededMWhBroker,30,10);
			observer.addAgents(SPOT);

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 4){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS1K = new MCTSAgent("MCTSAgent1-1K", 0, neededMWhBroker, 30, 10,1000);
			observer.addAgents(MCTS1K);

			MCTS1K = new MCTSAgent("MCTSAgent2-1K",1, neededMWhBroker,30,10,1000);
			observer.addAgents(MCTS1K);

			observer.NUM_MCTS_SIM = 3;
		}
		else if(observer.CASE_STUDY == 5){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS10K = new MCTSAgent("MCTSAgent1-10K", 0, neededMWhBroker, 30, 10,10000);
			observer.addAgents(MCTS10K);

			MCTS10K = new MCTSAgent("MCTSAgent2-10K",1, neededMWhBroker,30,10,10000);
			observer.addAgents(MCTS10K);

			observer.NUM_MCTS_SIM = 3;
		}
		else if(observer.CASE_STUDY == 6){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZIP = new ZIP("ZIP",1, neededMWhBroker,30,10);
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(ZI);
				observer.addAgents(ZIP);
			}
			else{
				observer.addAgents(ZIP);
				observer.addAgents(ZI);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 7){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent SPOT = new SPOT("SPOT",1, neededMWhBroker,30,10);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 30, 10);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(SPOT);
				observer.addAgents(ZIP);
			}
			else{
				observer.addAgents(ZIP);
				observer.addAgents(SPOT);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 8){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS1K = new MCTSAgent("MCTSAgent1K",1, neededMWhBroker,30,10,1000);
			Agent SPOT = new SPOT("SPOT", 0, neededMWhBroker, 30, 10);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(SPOT);
				observer.addAgents(MCTS1K);
			}
			else{
				observer.addAgents(MCTS1K);
				observer.addAgents(SPOT);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 9){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS1K = new MCTSAgent("MCTSAgent1K",1, neededMWhBroker,30,10,1000);
			Agent MCTS10K = new MCTSAgent("MCTSAgent10K", 0, neededMWhBroker, 30, 10, 10000);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(MCTS10K);
				observer.addAgents(MCTS1K);
			}
			else{
				observer.addAgents(MCTS1K);
				observer.addAgents(MCTS10K);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 10){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent SPOT = new SPOT("SPOT",1, neededMWhBroker,30,10);
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(SPOT);
				observer.addAgents(ZI);
			}
			else{
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 11){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS1K = new MCTSAgent("MCTSAgent1K",1, neededMWhBroker,30,10,1000);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 30, 10);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(ZIP);
				observer.addAgents(MCTS1K);
			}
			else{
				observer.addAgents(MCTS1K);
				observer.addAgents(ZIP);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 12){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent SPOT = new SPOT("SPOT",1, neededMWhBroker,30,10);
			Agent MCTS10K = new MCTSAgent("MCTSAgent10K", 0, neededMWhBroker, 30, 10, 10000);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(MCTS10K);
				observer.addAgents(SPOT);
			}
			else{
				observer.addAgents(SPOT);
				observer.addAgents(MCTS10K);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 13){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent MCTS1K = new MCTSAgent("MCTSAgent1K",1, neededMWhBroker,30,10,1000);
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(ZI);
				observer.addAgents(MCTS1K);
			}
			else{
				observer.addAgents(MCTS1K);
				observer.addAgents(ZI);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 14){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZIP = new ZIP("ZIP",1, neededMWhBroker,30,10);
			Agent MCTS10K = new MCTSAgent("MCTSAgent10K", 0, neededMWhBroker, 30, 10, 10000);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(MCTS10K);
				observer.addAgents(ZIP);
			}
			else{
				observer.addAgents(ZIP);
				observer.addAgents(MCTS10K);
			}

			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 15){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 2;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZI = new ZI("ZI",1, neededMWhBroker,30,10);
			Agent MCTS10K = new MCTSAgent("MCTSAgent10K", 0, neededMWhBroker, 30, 10, 10000);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(MCTS10K);
				observer.addAgents(ZI);
			}
			else{
				observer.addAgents(ZI);
				observer.addAgents(MCTS10K);
			}
			observer.NUM_MCTS_SIM = 0;
		}
		else if(observer.CASE_STUDY == 16){
			observer.agents.clear();
			observer.NUMBER_OF_BROKERS = 5;
			observer.NUMBER_OF_PRODUCERS = 5;
			observer.GREEN_AUCTION_FLAG = false;

			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 30, 10);
			Agent SPOT = new SPOT("SPOT", 0, neededMWhBroker, 30, 10);
			Agent MCTSAgent1K = new MCTSAgent("MCTSAgent1K",0, neededMWhBroker,30,10,1000);
			Agent MCTSAgent10K = new MCTSAgent("MCTSAgent10K",0, neededMWhBroker,30,10,10000);

			Random r = new Random();
			int  n = r.nextInt(2) + 0;
			if(n == 0){
				observer.addAgents(ZI);
				observer.addAgents(ZIP);
				observer.addAgents(SPOT);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(MCTSAgent10K);
			}
			else{
				observer.addAgents(MCTSAgent10K);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(ZI);
				observer.addAgents(ZIP);
				observer.addAgents(SPOT);
			}

			observer.NUM_MCTS_SIM = 3;
		}
		else{
			throw new Exception("Casestudy missmatch!");
		}

		//Disable MCTS_SIM_RUN
		observer.NUM_MCTS_SIM = 0;
	}

*/

	public static void main(String [] args) throws IOException{
		PDAuctioneer auctioneer = new PDAuctioneer();
		
		fwriter = new FileWriter("log.arff", true);
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
		Collections.sort(asks);
		Collections.sort(bids);

		if(observer.currentTimeSlot > 23)
			PrevDayHAMarketClearingPrice = observer.arrMarketClearingPriceHistory[observer.currentTimeSlot-24][observer.hour][observer.hourAhead];
		
		if(observer.hourAhead != Configure.getTOTAL_HOUR_AHEAD_AUCTIONS())
			PrevHAMarketClearingPrice = observer.arrMarketClearingPriceHistory[observer.currentTimeSlot][observer.hour][observer.hourAhead+1];
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

				if(oldask.price == 0 || oldbid.price == 0){
					if(observer.DEBUG)
						System.out.println(observer.getTime() + "0 prices! Auction didn't clear.");
					return 0;
				}

				clearingPrice = (double) ((Math.abs(oldask.price)+oldbid.price)/2);
				if(observer.DEBUG) 
					System.out.println(observer.getTime() + "1 Clearing price is : " + clearingPrice + " askid " + oldask.agentID + ", price " + oldask.price);
				observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
				observer.clearedAuctionCount++;
				if(clearingPrice != 0)
					logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," + clearingPrice);
				return clearingPrice;
			}
			for(;bidid < bids.size();){
				if(askid >= asks.size()){
					// ask is crossing its own limit size
					if(oldask.price == 0 || oldbid.price == 0){
						if(observer.DEBUG)
							System.out.println(observer.getTime() + "0 prices! Auction didn't clear.");
						return 0;
					}
					clearingPrice = (double) ((Math.abs(oldask.price)+oldbid.price)/2);
					if(observer.DEBUG) 
						System.out.println(observer.getTime() + "2 Clearing price is : " + clearingPrice);
					observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
					observer.clearedAuctionCount++;
					if(clearingPrice != 0)
						logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," +clearingPrice);
					return clearingPrice;
				}
				Bid bid = bids.get(bidid);
				Ask ask = asks.get(askid);
				// Check prices
				if(bid.price < Math.abs(ask.price)){
					if(bidid == 0 && askid == 0){
						// Auction will not clear;
						if(observer.DEBUG)
							System.out.println(observer.getTime() + "No bids left. Auction did not clear.");
						return 0;
					}
					else{
						clearingPrice = ((Math.abs(oldask.price)+oldbid.price)/2);
						if(observer.DEBUG)
							System.out.println(observer.getTime() + "3 Clearing price is : " + clearingPrice);
						observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
						observer.clearedAuctionCount++;
						if(clearingPrice != 0)
							logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," +clearingPrice);
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
						observer.addClearedTrades(new Bid(bid.agentName,-1, 0, ask.amount));
						observer.addClearedTrades(new Ask(ask.agentName,-1, 0, ask.amount));
					}
					else if(bid.amount > ask.amount){
						// clear part of the bid and ask
						askid++;
						if(observer.DEBUG){
							System.out.println(bid.toString() + " Bid Partially cleared :" + ask.amount);
							System.out.println(ask.toString());
						}
						observer.addClearedTrades(new Bid(bid.agentName, -1, 0, ask.amount));
						observer.addClearedTrades(new Ask(ask.agentName, -1, 0, ask.amount));
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
						observer.addClearedTrades(new Bid(bid.agentName,-1, 0, bid.amount));
						observer.addClearedTrades(new Ask(ask.agentName,-1, 0, bid.amount));
						ask.amount = ask.amount - bid.amount;
						bid.amount = 0;
					}
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
			double clearingprice = (double)(Math.abs(oldask.price*observer.arrProducerGreenPoints[oldask.agentID])+oldbid.price)/2;
			if(observer.DEBUG)
				System.out.println(observer.getTime() + " Clearing price: " + clearingprice);
			if(clearingPrice != 0)
				logger.println(observer.getTime()+PrevDayHAMarketClearingPrice +"," + PrevHAMarketClearingPrice + "," +clearingPrice);
			return clearingprice;
		}
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
						observer.addClearedTrades(new Bid(bid.agentName,bid.agentID, 0, ask.amount));
						observer.addClearedTrades(new Ask(ask.agentName,ask.agentID, 0, ask.amount));
					}
					else if(bid.amount > ask.amount){
						// clear part of the bid and ask
						askid++;
						if(observer.DEBUG){
							System.out.println(bid.toString() + " Bid Partially cleared :" + ask.amount);
							System.out.println(ask.toString());
						}
						observer.addClearedTrades(new Bid(bid.agentName,bid.agentID, 0, ask.amount));
						observer.addClearedTrades(new Ask(ask.agentName,ask.agentID, 0, ask.amount));
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
						observer.addClearedTrades(new Bid(bid.agentName,bid.agentID, 0, bid.amount));
						observer.addClearedTrades(new Ask(ask.agentName,ask.agentID, 0, bid.amount));
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
