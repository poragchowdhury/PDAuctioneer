package TacTex;

public class PriceMwhPair implements Comparable<PriceMwhPair> {
  
    Double price;
    double mwh;  
  
    /**
     * @param price
     * @param mwh
     */
    public PriceMwhPair(Double price, double mwh) {
      super();
      this.price = price;
      this.mwh = mwh;
    }
    
  
    public double getPricePerMwh() {
      return price;
    }
  
    public double getMwh() {
      return mwh;
    }    
    
    public double getPricePerKwh() {
      return price / 1000.0;
    }
    
    public double getKwh() {
      return mwh * 1000.0;
    }

    public void addMwh(double addedMwh) {
	  this.mwh += addedMwh;
	}


	@Override
    public int compareTo(PriceMwhPair o) {
      if (this.price < o.price) return -1;
      if (this.price > o.price) return 1;
      return 0;
    }
  }