package configure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configure {
	
	private static int START_SEED_NO = 0;
	private static int END_SEED_NO = 0;
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
	private static boolean USEDPPREDICTOR = true;
	private static boolean THRESHOLDFILTER = false;
	
	
	///////////////////////////////
	// Simulation configuration ///
	///////////////////////////////
	/*
	boots-misobuyer\boot0.csv Avg ,18058.026559324575, Min, 2113.9954963407617, Max,26622.598796735547
	boots-misobuyer\boot1.csv Avg ,17823.889356603955, Min, 2087.8586480202407, Max,25263.601956207007
	boots-misobuyer\boot2.csv Avg ,17340.669759841992, Min, 2076.4806189956194, Max,25096.951094140153
	boots-misobuyer\boot3.csv Avg ,17561.182118107197, Min, 2161.9372973244326, Max,24963.41980524013
	boots-misobuyer\boot4.csv Avg ,17613.07431552127, Min, 2344.7455081431176, Max,26208.60607085464
	boots-misobuyer\boot5.csv Avg ,18010.304975503634, Min, 12155.579572264502, Max,25610.88671930853
	boots-misobuyer\boot6.csv Avg ,17772.813933901514, Min, 2101.3888085835733, Max,25407.339636140387
	boots-misobuyer\boot7.csv Avg ,17769.137971210494, Min, 10520.034513471352, Max,25776.125400553203
	boots-misobuyer\boot8.csv Avg ,17827.814741832117, Min, 11906.410045460563, Max,25646.33990753635
	boots-misobuyer\boot9.csv Avg ,18017.616571826526, Min, 2161.1537546838877, Max,24340.98894012339
	boots-misobuyer\boot10.csv Avg ,17466.570528809953, Min, 11125.233395739882, Max,25161.860669991598
	boots-misobuyer\boot11.csv Avg ,18116.239012176902, Min, 2229.648086430167, Max,26092.31308189775
	boots-misobuyer\boot12.csv Avg ,17403.67279010823, Min, 2126.7429100813433, Max,25294.89120543867
	boots-misobuyer\boot13.csv Avg ,17965.79916577206, Min, 11711.599615379495, Max,26753.612784983983
	boots-misobuyer\boot14.csv Avg ,17613.962871373988, Min, 9478.352385130103, Max,25108.62360006229
	boots-misobuyer\boot15.csv Avg ,17594.554985317038, Min, 2189.0660113971626, Max,25744.02021034002
	boots-misobuyer\boot16.csv Avg ,17659.761399403862, Min, 11647.646643969207, Max,25359.898602368372
	boots-misobuyer\boot17.csv Avg ,17319.30409593115, Min, 2295.0753006228483, Max,24950.23868105887
	boots-misobuyer\boot18.csv Avg ,17780.567575818986, Min, 2024.2812087867596, Max,24571.36456217865
	boots-misobuyer\boot19.csv Avg ,17472.556201256415, Min, 2047.316174482063, Max,24791.891819917775
	boots-misobuyer\boot20.csv Avg ,17363.478549186806, Min, 11227.822425584905, Max,26067.806940091796
	boots-misobuyer\boot21.csv Avg ,17583.831844349294, Min, 2079.483240619104, Max,25883.11286908956
	boots-misobuyer\boot22.csv Avg ,17675.423777188542, Min, 2036.225416763132, Max,25925.546102990447
	boots-misobuyer\boot23.csv Avg ,17609.004226993115, Min, 2055.286382123141, Max,24712.457680467385
	boots-misobuyer\boot24.csv Avg ,17712.06131968377, Min, 2148.609634131263, Max,24580.038420393048
	boots-misobuyer\boot25.csv Avg ,17186.607728875293, Min, 2075.236176904945, Max,25004.757920661028
	boots-misobuyer\boot26.csv Avg ,17461.97105780761, Min, 2229.0538505655486, Max,25478.440964063368
	boots-misobuyer\boot27.csv Avg ,17689.034681228743, Min, 2220.528674525673, Max,25156.113419565336
	boots-misobuyer\boot28.csv Avg ,17797.09100557944, Min, 2093.4729305191017, Max,25173.57947626865
	boots-misobuyer\boot29.csv Avg ,17916.662147747193, Min, 2107.63236734164, Max,26263.269524371543
	*/
	
	public static double [] avgSupply_REPTree = {
			18058.026559324575/300, // boot0
			17823.889356603955/300, // boot1
			17340.669759841992/300, // boot2
			17561.182118107197/300, // boot3
			17613.07431552127/300, // boot4
			18010.304975503634/300, // boot5
			17772.813933901514/300, // boot6
			17769.137971210494/300, // boot7
			17827.814741832117/300, // boot8
			18017.616571826526/300, // boot9
		
			17466.570528809953/300, // boot10
			18116.239012176902/300, // boot11
			17403.67279010823/300, // boot12
			17965.79916577206/300, // boot13
			17613.962871373988/300, // boot14
			17594.554985317038/300, // boot15
			17659.761399403862/300, // boot16
			17319.30409593115/300, // boot17
			17780.567575818986/300, // boot18
			17472.556201256415/300, // boot19
		
			17363.478549186806/300, // boot20
			17583.831844349294/300, // boot21
			17675.423777188542/300, // boot22
			17609.004226993115/300, // boot23
			17712.06131968377/300, // boot24
			17186.607728875293/300, // boot25
			17461.97105780761/300, // boot26
			17689.034681228743/300, // boot27
			17797.09100557944/300, // boot28
			17916.662147747193/300 // boot29
	};
	
	public static double [] STDDEV = {
			
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

	
	public static enum CASE_STUDY{
		HEAD2HEAD_2BrokerGames(1),
		RANDOM_5BrokerGames(2),
		BY_NO_OF_MCTS_SIM_5BrokerGame(3),
		TRANINIG_WITH_5ZI(4),
		TRANINIG_WITH_5BrokerGame(5),
		TRAINING_WITH_4ZI(6),
		TRANINIG_WITH_4BrokerGame(7),
		TRAINING_WITH_2ZI(8),
		TRANINIG_WITH_2BrokerGame(9),
		TACTEX(10),
		TACTEX_4Agents(11),
		MCTS_4Agents(12);
		
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
			START_SEED_NO = Integer.parseInt(prop.getProperty("startseednumber"));
			END_SEED_NO = Integer.parseInt(prop.getProperty("endseednumber"));
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
			USEDPPREDICTOR = Boolean.parseBoolean(prop.getProperty("usedppredictor"));
			THRESHOLDFILTER = Boolean.parseBoolean(prop.getProperty("thresholdfilter"));
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
	
	public static int getSTART_SEED_NO(){
		return START_SEED_NO;
	}
	
	public static int getEND_SEED_NO(){
		return END_SEED_NO;
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
	
	public static boolean getUSE_DP_PREDICTOR(){
		return USEDPPREDICTOR;
	}
	
	public static boolean getTHRESHOLDFILTER(){
		return THRESHOLDFILTER;
	}
	
	public String toString() {
		String str = "START_SEED_NO," + START_SEED_NO +
				",END_SEED_NO," + END_SEED_NO +
			",NO_OF_MCTS_CASES," + NO_OF_MCTS_CASES +
			",CASE_STUDY_NO," + CASE_STUDY_NO +
			",MAX_OF_SUB_CASE_STUDY,"+MAX_OF_SUB_CASE_STUDY+
			",DIFF_MCTS_SIM_CASES_EXP,"+DIFF_MCTS_SIM_CASES_EXP+
		",PEDICTOR_NAME,"+PEDICTOR_NAME+
		",TOTAL_SIM_DAYS,"+TOTAL_SIM_DAYS+
		",HOURS_IN_A_DAY,"+HOURS_IN_A_DAY+
		",TOTAL_HOUR_AHEAD_AUCTIONS,"+TOTAL_HOUR_AHEAD_AUCTIONS+
		",NUMBER_OF_BROKERS,"+NUMBER_OF_BROKERS+
		",NUMBER_OF_PRODUCERS,"+NUMBER_OF_PRODUCERS+
		",PERHOURENERGYDEMAND,"+PERHOURENERGYDEMAND+
		",VARMCTSAGENTITERATION,"+VARMCTSAGENTITERATION+
		",MCTSBROKERDEMANDPERC,"+MCTSBROKERDEMANDPERC+
		",USEDPPREDICTOR,"+USEDPPREDICTOR+
		",THRESHOLDFILTER,"+THRESHOLDFILTER;
		return str;
	}
}
