package Observer;

import java.util.Random;

import Agents.Agent;
import Agents.MCTSAgent;
import Agents.Producer;
import Agents.SPOT;
import Agents.ZI;
import Agents.ZIP;
import configure.Configure;

public class CaseStudies {
	public double configureSimulation(Observer observer){
		double neededMCTSMWhBroker = Configure.getPERHOURENERGYDEMAND()*Configure.getMCTSBROKERDEMANDPERC();
		double neededMWhBroker = Configure.getPERHOURENERGYDEMAND()*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));
		// neededMWhBroker = Configure.getPERHOURENERGYDEMAND()/Configure.getNUMBER_OF_BROKERS();
		if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.BY_NO_OF_MCTS_SIM_5BrokerGame.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			observer.GREEN_AUCTION_FLAG = false;
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(ZI);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(ZIP);
			Agent SPOT = new SPOT("SPOT", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(SPOT);
			Agent MCTSAgent1K = new MCTSAgent("MCTSAgent",0, neededMCTSMWhBroker,30,10,1000);
			observer.printableAgents.add(MCTSAgent1K);
			Agent varMCTSAgent = new MCTSAgent("varMCTSAgent"+Configure.getVARMCTSAGENTITERATION(),0, neededMWhBroker,30,10,Configure.getVARMCTSAGENTITERATION());
			observer.printableAgents.add(varMCTSAgent);
			
			Random r = new Random(observer.SEED);
			int  n = r.nextInt(6) + 0;
			if(n == 0){
				observer.addAgents(ZIP);
				observer.addAgents(ZI);
				observer.addAgents(varMCTSAgent);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(SPOT);
			}
			else if(n == 1){
				observer.addAgents(ZIP);
				observer.addAgents(varMCTSAgent);
				observer.addAgents(ZI);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(SPOT);
			}
			else if(n == 2)
			{
				observer.addAgents(ZIP);
				observer.addAgents(varMCTSAgent);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
			else if(n == 3){
				observer.addAgents(ZIP);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(varMCTSAgent);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
			else if(n == 4){
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(varMCTSAgent);
				observer.addAgents(ZIP);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
			else{
				observer.addAgents(varMCTSAgent);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(ZIP);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
		}
		else if (Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.TRANINIG_WITH_5ZI.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			
			observer.GREEN_AUCTION_FLAG = false;
			
			Agent ZI1 = new ZI("ZI1",0, neededMWhBroker,30,10);
			observer.addAgents(ZI1);
			observer.printableAgents.add(ZI1);

			Agent ZI2 = new ZI("ZI2",0, neededMWhBroker,30,10);
			observer.addAgents(ZI2);
			observer.printableAgents.add(ZI2);

			Agent ZI3 = new ZI("ZI3",0, neededMWhBroker,30,10);
			observer.addAgents(ZI3);
			observer.printableAgents.add(ZI3);

			Agent ZI4 = new ZI("ZI4",0, neededMWhBroker,30,10);
			observer.addAgents(ZI4);
			observer.printableAgents.add(ZI4);

			Agent ZI5 = new ZI("ZI5",0, neededMWhBroker,30,10);
			observer.addAgents(ZI5);
			observer.printableAgents.add(ZI5);

			/*
			{	observer.agents.clear();
				observer.NUMBER_OF_BROKERS = 5;
				observer.NUMBER_OF_PRODUCERS = 5;
				return neededMWhBroker;
		}
		*/
		}
		else if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.TRANINIG_WITH_5BrokerGame.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			observer.GREEN_AUCTION_FLAG = false;
			
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(ZI);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(ZIP);
			Agent SPOT = new SPOT("SPOT", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(SPOT);
			Agent MCTSAgent1K = new MCTSAgent("MCTSAgent1K",0, neededMWhBroker,30,10,1000);
			observer.printableAgents.add(MCTSAgent1K);
			Agent VarMCTSAgent = new MCTSAgent("varMCTSAgent",0, neededMWhBroker,30,10,Configure.getVARMCTSAGENTITERATION());
			observer.printableAgents.add(VarMCTSAgent);
			Random r = new Random(observer.SEED);
			int  n = r.nextInt(6) + 0;
			if(n == 0){
				observer.addAgents(ZIP);
				observer.addAgents(ZI);
				observer.addAgents(VarMCTSAgent);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(SPOT);
			}
			else if(n == 1){
				observer.addAgents(ZIP);
				observer.addAgents(VarMCTSAgent);
				observer.addAgents(ZI);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(SPOT);
			}
			else if(n == 2)
			{
				observer.addAgents(ZIP);
				observer.addAgents(VarMCTSAgent);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
			else if(n == 3){
				observer.addAgents(ZIP);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(VarMCTSAgent);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
			else if(n == 4){
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(VarMCTSAgent);
				observer.addAgents(ZIP);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
			else{
				observer.addAgents(VarMCTSAgent);
				observer.addAgents(MCTSAgent1K);
				observer.addAgents(ZIP);
				observer.addAgents(ZI);
				observer.addAgents(SPOT);
			}
		}

		// Add all the producers
		for(int i = 0; i < Configure.getNUMBER_OF_PRODUCERS(); i++){
			Agent producer = new Producer("Producer"+i, i, 0, 0, 0);
			observer.printableAgents.add(producer);
		}
		
		return neededMWhBroker;
		
	}

}
