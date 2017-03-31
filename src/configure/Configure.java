package configure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configure {
	
	private static int SEED_NO = 0;
	private static int NO_OF_MCTS_CASES = 0;
	private static int CASE_STUDY_NO = 0;
	private static int MAX_OF_SUB_CASE_STUDY = 1;
	private static boolean DIFF_MCTS_SIM_CASES_EXP = false;
	private static String PEDICTOR_NAME = null;
	private static int TOTAL_SIM_DAYS = 0;
	private static int HOURS_IN_A_DAY = 0;
	private static int TOTAL_HOUR_AHEAD_AUCTIONS = 0;
	private static int NUMBER_OF_BROKERS = 0;
	private static int NUMBER_OF_PRODUCERS = 0;
	private static double PERHOURENERGYDEMAND = 0;
	private static int VARMCTSAGENTITERATION = 0;
	private static double MCTSBROKERDEMANDPERC = 0;
	
	public static enum CASE_STUDY{
		HEAD2HEAD_2BrokerGames(1),
		RANDOM_5BrokerGames(2),
		BY_NO_OF_MCTS_SIM_5BrokerGame(3),
		TRANINIG_WITH_5ZI(4),
		TRANINIG_WITH_5BrokerGame(5),
		TRAINING_WITH_4ZI(6),
		TRANINIG_WITH_4BrokerGame(7),
		TRAINING_WITH_2ZI(7),
		TRANINIG_WITH_2BrokerGame(8);
		
		private final int id;
		CASE_STUDY(int id) { this.id = id; }
	    public int getValue() { return id; }
	}
	
	public Configure(){
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
			// get the property value and print it out
			CASE_STUDY_NO = Integer.parseInt(prop.getProperty("casestudy"));
			PEDICTOR_NAME = prop.getProperty("predictorname");
			SEED_NO = Integer.parseInt(prop.getProperty("seednumber"));
			NO_OF_MCTS_CASES = Integer.parseInt(prop.getProperty("mctscases"));
			DIFF_MCTS_SIM_CASES_EXP = Boolean.parseBoolean(prop.getProperty("diffmctscasesexp"));
			TOTAL_SIM_DAYS = Integer.parseInt(prop.getProperty("totalsimdays"));
			HOURS_IN_A_DAY = Integer.parseInt(prop.getProperty("hoursinaday"));
			TOTAL_HOUR_AHEAD_AUCTIONS = Integer.parseInt(prop.getProperty("houraheadwindow"));
			NUMBER_OF_PRODUCERS = Integer.parseInt(prop.getProperty("numberofproducers"));
			NUMBER_OF_BROKERS = Integer.parseInt(prop.getProperty("numberofbrokers"));
			PERHOURENERGYDEMAND = Double.parseDouble(prop.getProperty("perhourenergydemand"));
			VARMCTSAGENTITERATION = Integer.parseInt(prop.getProperty("varmctsagentiteration"));
			MCTSBROKERDEMANDPERC = Double.parseDouble(prop.getProperty("mctsbrokerdemand"));
			loadConfiguration();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void loadConfiguration() {
		if(CASE_STUDY_NO == CASE_STUDY.BY_NO_OF_MCTS_SIM_5BrokerGame.getValue()){
			MAX_OF_SUB_CASE_STUDY = 1;
		}
		else if(CASE_STUDY_NO == CASE_STUDY.TRANINIG_WITH_5ZI.getValue()){
			MAX_OF_SUB_CASE_STUDY = 1;
		}
	}

	public static int getHOURS_IN_A_DAY() {
		return HOURS_IN_A_DAY;
	}

	public static int getTOTAL_SIM_DAYS() {
		return TOTAL_SIM_DAYS;
	}
	
	public static int getTOTAL_HOUR_AHEAD_AUCTIONS() {
		return TOTAL_HOUR_AHEAD_AUCTIONS;
	}

	public static int getNUMBER_OF_BROKERS() {
		return NUMBER_OF_BROKERS;
	}
	
	public static int getNUMBER_OF_PRODUCERS() {
		return NUMBER_OF_PRODUCERS;
	}

	public static String getPREDICTOR_NAME(){
		return PEDICTOR_NAME;
	}
	
	public static int getCASE_STUDY_NO(){
		return CASE_STUDY_NO;
	}
	
	public static int getSEED_NO(){
		return SEED_NO;
	}
	
	public static double getPERHOURENERGYDEMAND(){
		return PERHOURENERGYDEMAND;
	}
	
	public static boolean getDIFF_MCTS_SIM_CASES_EXP(){
		return DIFF_MCTS_SIM_CASES_EXP;
	}
	
	public static int getNO_OF_MCTS_CASES(){
		return NO_OF_MCTS_CASES;
	}
	
	public static int getMAX_OF_SUB_CASE_STUDY(){
		return MAX_OF_SUB_CASE_STUDY;
	}
	
	public static int getVARMCTSAGENTITERATION(){
		return VARMCTSAGENTITERATION;
	}
	
	public static double getMCTSBROKERDEMANDPERC(){
		return MCTSBROKERDEMANDPERC;
	}
}
