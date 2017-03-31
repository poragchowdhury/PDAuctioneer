package Observer;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class PricePredictor {
	public Classifier classifier;
	public FastVector attributes;
	
	public PricePredictor(String predictorVersion){
		Attribute currentTimeSlot = new Attribute("currentTimeSlot");
		Attribute hour = new Attribute("hour");
		Attribute hourAhead = new Attribute("hourAhead");
		Attribute date = new Attribute("date");
		Attribute month = new Attribute("month");
		Attribute year = new Attribute("year");
		Attribute nUMBER_OF_BROKERS = new Attribute("nUMBER_OF_BROKERS");
		Attribute nUMBER_OF_PRODUCERS = new Attribute("nUMBER_OF_PRODUCERS");
		Attribute clearingPrice = new Attribute("clearingPrice");

		attributes = new FastVector();
		attributes.addElement(currentTimeSlot);
		attributes.addElement(hour);
		attributes.addElement(hourAhead);
		attributes.addElement(date);
		attributes.addElement(month);
		attributes.addElement(year);
		attributes.addElement(nUMBER_OF_BROKERS);
		attributes.addElement(nUMBER_OF_PRODUCERS);
		attributes.addElement(clearingPrice);
		
		loadModel(predictorVersion);
	}
	
	
	public void loadModel(String predictorVersion){
		try {
			this.classifier = (Classifier) (new ObjectInputStream(new FileInputStream(predictorVersion))).readObject();
			//System.out.println("\n\nModel loaded successfully!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Instance getTestInstance(double [] values){
		 Instance myInstance = null;
		 try{
			 Instance inst = new Instance(1.0, values);
			 Instances data = new Instances("Dataset", attributes, 0);;
			 data.add(inst);
			 data.setClassIndex(data.numAttributes() - 1);
			 myInstance = data.lastInstance();
		 }
		 catch(Exception e){
			 e.printStackTrace();
		 }
		 return myInstance;
	 }
	 
	 public double getLimitPrice(double [] param){
		 	Instance myIns = getTestInstance(param);
			if (myIns == null)
				System.out.println("*****************My Ins Null : ");
			double limitPrice = 0;
			try{
				limitPrice = this.classifier.classifyInstance(myIns);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			return limitPrice;
	 }
}
