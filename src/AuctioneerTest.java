
import java.util.ArrayList;

import Agents.Agent;
import Auctioneer.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import junit.framework.TestCase;


public class AuctioneerTest extends TestCase {
	
	public PDAuctioneer svc = new PDAuctioneer();
	
	@Before
	public void setup(){
	}
	
	@Test
	public void testAuction() throws Exception {
		// There will be 5 producers
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("DEBUG Auction: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 35, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 20, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)40, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)50, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = (double)(30+35)/2;
		double clearningpricefromfunc = svc.clearAuction(svc.asks, svc.bids);
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());
	}
	
	@Test
	public void testGreenAuction() throws Exception {

		// There will be 5 producers
		svc.observer.arrProducerGreenPoints[0]=20;
		svc.observer.arrProducerGreenPoints[1]=80;
		svc.observer.arrProducerGreenPoints[2]=20;
		svc.observer.arrProducerGreenPoints[3]=80;
		svc.observer.arrProducerGreenPoints[4]=20;
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("\nDEBUG Green Auction: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10 , Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 35, 10 , Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 20, 10 , Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10 , Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10/20, 10 , Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20/80, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)40/80, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)50/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = (double)(30+35)/2; 
		double clearningpricefromfunc = svc.clearGreenAuction(svc.asks, svc.bids);; 
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());
	}

	@Test
	public void testAuction2() throws Exception {
		// There will be 5 producers
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("DEBUG Auction 2: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 35, 5, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 30, 5, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)40, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)50, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = (double)(30+30)/2;
		double clearningpricefromfunc = svc.clearAuction(svc.asks, svc.bids);
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());
	}
	
	@Test
	public void testGreenAuction2() throws Exception {

		// There will be 5 producers
		svc.observer.arrProducerGreenPoints[0]=20;
		svc.observer.arrProducerGreenPoints[1]=80;
		svc.observer.arrProducerGreenPoints[2]=20;
		svc.observer.arrProducerGreenPoints[3]=80;
		svc.observer.arrProducerGreenPoints[4]=20;
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("\nDEBUG Green Auction 2: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 35, 5, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 30, 5, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20/80, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)40/80, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)50/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = (double)(30+30)/2; 
		double clearningpricefromfunc = svc.clearGreenAuction(svc.asks, svc.bids);; 
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());
	}

	@Test
	public void testAuction3() throws Exception {
		// There will be 5 producers
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("DEBUG Auction 3: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 35, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 20, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30, 5, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)35, 5, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)40, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = (double)(35+35)/2;
		double clearningpricefromfunc = svc.clearAuction(svc.asks, svc.bids);
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());
		
	}
	
	@Test
	public void testGreenAuction3() throws Exception {

		// There will be 5 producers
		svc.observer.arrProducerGreenPoints[0]=20;
		svc.observer.arrProducerGreenPoints[1]=80;
		svc.observer.arrProducerGreenPoints[2]=20;
		svc.observer.arrProducerGreenPoints[3]=80;
		svc.observer.arrProducerGreenPoints[4]=20;
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("\nDEBUG Green Auction 3: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 35, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 20, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20/80, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30/20, 5, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)35/80, 5, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)40/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = (double)(35+35)/2; 
		double clearningpricefromfunc = svc.clearGreenAuction(svc.asks, svc.bids);; 
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());
	}

	@Test
	public void testAuction4() throws Exception {
		// There will be 5 producers
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("DEBUG Auction 4: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 30, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 20, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		
		Ask ask = new Ask("p0", 0, (double)40, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)50, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)60, 5, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		
		double clearingprice = 0;
		double clearningpricefromfunc = svc.clearAuction(svc.asks, svc.bids);
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(0.00,svc.observer.getClearedVolume());
		
	}
	
	@Test
	public void testGreenAuction4() throws Exception {

		// There will be 5 producers
		svc.observer.arrProducerGreenPoints[0]=20;
		svc.observer.arrProducerGreenPoints[1]=80;
		svc.observer.arrProducerGreenPoints[2]=20;
		svc.observer.arrProducerGreenPoints[3]=80;
		svc.observer.arrProducerGreenPoints[4]=20;
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("\nDEBUG Green Auction 4: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 30, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 20, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		
		Ask ask = new Ask("p0", 0, (double)40, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)50, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)60, 5, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		
		double clearingprice = 0;
		double clearningpricefromfunc = svc.clearGreenAuction(svc.asks, svc.bids);
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(0.00,svc.observer.getClearedVolume());	
	}

	@Test
	public void testAuction5() throws Exception {
		// There will be 5 producers
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("DEBUG Auction 5: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 30, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 20, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)40, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)50, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = (double)(30+30)/2;
		double clearningpricefromfunc = svc.clearAuction(svc.asks, svc.bids);
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());		
	}
	
	@Test
	public void testGreenAuction5() throws Exception {

		// There will be 5 producers
		svc.observer.arrProducerGreenPoints[0]=20;
		svc.observer.arrProducerGreenPoints[1]=80;
		svc.observer.arrProducerGreenPoints[2]=20;
		svc.observer.arrProducerGreenPoints[3]=80;
		svc.observer.arrProducerGreenPoints[4]=20;
		svc.observer.setTime(0,0,0,0);
		svc.observer.DEBUG = true;

		System.out.println("\nDEBUG Green Auction 5: " + svc.observer.DEBUG);
		
		Bid bid = new Bid("b0", 0, 50, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b1", 1, 40, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b2", 2, 30, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b3", 3, 20, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		bid = new Bid("b4", 4, 10, 10, Agent.agentType.BROKER);
		svc.bids.add(bid);
		
		Ask ask = new Ask("p0", 0, (double)10/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p1", 1, (double)20/80, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p2", 2, (double)30/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p3", 3, (double)40/80, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		ask = new Ask("p4", 4, (double)50/20, 10, Agent.agentType.PRODUCER);
		System.out.println(ask.toString());
		svc.asks.add(ask);
		
		double clearingprice = 30.00;
		double clearningpricefromfunc = svc.clearGreenAuction(svc.asks, svc.bids);
		assertEquals(clearingprice, clearningpricefromfunc);
		assertEquals(30.00,svc.observer.getClearedVolume());	
	}

}

