package Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Agents.Agent;
import Agents.C1;
import Agents.C2;
import Agents.MCTSAgent;
import Agents.Producer;
import Agents.SPOT;
import Agents.TacTex;
import Agents.ZI;
import Agents.ZIP;
import configure.Configure;

public class CaseStudies {
	public List<List<Integer>> list = new ArrayList<List<Integer>>();
	public double configureSimulation(Observer observer){
		double neededMCTSMWhBroker = (Configure.getPERHOURENERGYDEMAND() * Configure.avgSupply_REPTree[observer.SEED-1])*Configure.getMCTSBROKERDEMANDPERC();//Configure.getPERHOURENERGYDEMAND()*Configure.getMCTSBROKERDEMANDPERC();
		double neededMWhBroker = (Configure.getPERHOURENERGYDEMAND() * Configure.avgSupply_REPTree[observer.SEED-1])*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));//Configure.getPERHOURENERGYDEMAND()*((1-Configure.getMCTSBROKERDEMANDPERC())/(Configure.getNUMBER_OF_BROKERS()-1));
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
			Agent MCTSAgent1K = new MCTSAgent("MCTSX",0, neededMCTSMWhBroker,30,10,1000);
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
		else if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.TACTEX.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			observer.GREEN_AUCTION_FLAG = false;
			/*
			List<Integer> available = new ArrayList<Integer>();
	        List<Integer> visitedNodes = new ArrayList<Integer>();
	        
	        for(int i = 0; i < 4; i++){
	            available.add(i);
	        }
	        dfs(available,visitedNodes);
	        */
			
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 40, 10);
			observer.printableAgents.add(ZI);
			Agent TacTex = new TacTex("TacTex", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(TacTex);
			Agent MCTSX = new MCTSAgent("MCTSX-5act-dyn-C2",0, neededMCTSMWhBroker,30,10,Configure.getVARMCTSAGENTITERATION());
			observer.printableAgents.add(MCTSX);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 40, 10);
			observer.printableAgents.add(ZIP);
			
			observer.addAgents(ZI);
			observer.addAgents(TacTex);
			observer.addAgents(MCTSX);
			observer.addAgents(ZIP);
			/*
			List<Integer> combinationIndex = list.get((observer.SEED-1)%24);
			for(Integer i:combinationIndex) {
				observer.addAgents(observer.printableAgents.get(i.intValue()));
			}
			*/
		}
		else if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.C1.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			observer.GREEN_AUCTION_FLAG = false;
			/*
			List<Integer> available = new ArrayList<Integer>();
	        List<Integer> visitedNodes = new ArrayList<Integer>();
	        
	        for(int i = 0; i < 4; i++){
	            available.add(i);
	        }
	        dfs(available,visitedNodes);
	        */
			
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 40, 10);
			observer.printableAgents.add(ZI);
			Agent TacTex = new TacTex("TacTex", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(TacTex);
			Agent c1 = new C1("C1",0, neededMCTSMWhBroker,30,10);
			observer.printableAgents.add(c1);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 40, 10);
			observer.printableAgents.add(ZIP);
			
			observer.addAgents(ZI);
			observer.addAgents(TacTex);
			observer.addAgents(c1);
			observer.addAgents(ZIP);
			/*
			List<Integer> combinationIndex = list.get((observer.SEED-1)%24);
			for(Integer i:combinationIndex) {
				observer.addAgents(observer.printableAgents.get(i.intValue()));
			}
			*/
		}
		else if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.C2.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			observer.GREEN_AUCTION_FLAG = false;
			/*
			List<Integer> available = new ArrayList<Integer>();
	        List<Integer> visitedNodes = new ArrayList<Integer>();
	        
	        for(int i = 0; i < 4; i++){
	            available.add(i);
	        }
	        dfs(available,visitedNodes);
	        */
			
			Agent ZI = new ZI("ZI", 0, neededMWhBroker, 40, 10);
			observer.printableAgents.add(ZI);
			Agent TacTex = new TacTex("TacTex", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(TacTex);
			Agent c2 = new C2("C2",0, neededMCTSMWhBroker,30,10);
			observer.printableAgents.add(c2);
			Agent ZIP = new ZIP("ZIP", 0, neededMWhBroker, 40, 10);
			observer.printableAgents.add(ZIP);
			
			observer.addAgents(ZI);
			observer.addAgents(TacTex);
			observer.addAgents(c2);
			observer.addAgents(ZIP);
			/*
			List<Integer> combinationIndex = list.get((observer.SEED-1)%24);
			for(Integer i:combinationIndex) {
				observer.addAgents(observer.printableAgents.get(i.intValue()));
			}
			*/
		}
		else if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.MCTS_4Agents.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			observer.GREEN_AUCTION_FLAG = false;
			
			List<Integer> available = new ArrayList<Integer>();
	        List<Integer> visitedNodes = new ArrayList<Integer>();
	        
	        for(int i = 0; i < 4; i++){
	            available.add(i);
	        }
	        dfs(available,visitedNodes);
	        
	        Agent MCTSX = new MCTSAgent("MCTSX",0, neededMCTSMWhBroker,30,10,Configure.getVARMCTSAGENTITERATION());
			observer.printableAgents.add(MCTSX);
			Agent MCTSX1 = new MCTSAgent("MCTSX1",0, neededMWhBroker,30,10,Configure.getVARMCTSAGENTITERATION());
			observer.printableAgents.add(MCTSX1);
			Agent MCTSX2 = new MCTSAgent("MCTSX2",0, neededMWhBroker,30,10,Configure.getVARMCTSAGENTITERATION());
			observer.printableAgents.add(MCTSX2);
			Agent MCTSX3 = new MCTSAgent("MCTSX3",0, neededMWhBroker,30,10,Configure.getVARMCTSAGENTITERATION());
			observer.printableAgents.add(MCTSX3);
			
			List<Integer> combinationIndex = list.get((observer.SEED-1)%24);
			for(Integer i:combinationIndex) {
				observer.addAgents(observer.printableAgents.get(i.intValue()));
			}
		}
		else if(Configure.getCASE_STUDY_NO() == Configure.CASE_STUDY.TACTEX_4Agents.getValue()){
			observer.agents.clear();
			observer.printableAgents.clear();
			observer.GREEN_AUCTION_FLAG = false;
			
			List<Integer> available = new ArrayList<Integer>();
	        List<Integer> visitedNodes = new ArrayList<Integer>();
	        
	        for(int i = 0; i < 4; i++){
	            available.add(i);
	        }
	        dfs(available,visitedNodes);
	        
			Agent TacTex1 = new TacTex("TacTex1", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(TacTex1);
			Agent TacTex2 = new TacTex("TacTex2", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(TacTex2);
			Agent TacTex3 = new TacTex("TacTex3", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(TacTex3);
			Agent TacTex4 = new TacTex("TacTex4", 0, neededMWhBroker, 30, 10);
			observer.printableAgents.add(TacTex4);
			
			List<Integer> combinationIndex = list.get((observer.SEED-1)%24);
			for(Integer i:combinationIndex) {
				observer.addAgents(observer.printableAgents.get(i.intValue()));
			}
		}
		// Add all the producers
		for(int i = 0; i < Configure.getNUMBER_OF_PRODUCERS(); i++){
			Agent producer = new Producer("Producer"+i, i, 0, 0, 0);
			observer.printableAgents.add(producer);
			observer.addAgents(producer);
		}
		
		return neededMWhBroker;
		
	}

    public void dfs(List<Integer> available, List<Integer> visitedNodes){
        if(available.isEmpty()){
            list.add(new ArrayList<Integer>(visitedNodes));
            return;
        }
            
        for(int j = 0; j < available.size(); j++){
            Integer n = available.get(j);
            visitedNodes.add(n);
            available.remove(j);
            dfs(available,visitedNodes);
            available.add(j,n);
            visitedNodes.remove(visitedNodes.size()-1);
        }
        
    }
}
