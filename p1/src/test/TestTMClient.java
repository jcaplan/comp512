package test;

import static org.junit.Assert.*;
import middleware.client.MWClient;
import middleware.server.Middleware;
import middleware.tm.TMClient;

import org.junit.Before;
import org.junit.Test;




public class TestTMClient {

	MWClient carClient;
	MWClient flightClient;
	MWClient roomClient;
	MWClient custClient;
	
	
	@Before
	public void beforeTest(){
//		carClient = Middleware.getNewClient(Middleware.CAR_CLIENT);
//		custClient = Middleware.getNewClient(Middleware.CUSTOMER_CLIENT);
//		flightClient = Middleware.getNewClient(Middleware.FLIGHT_CLIENT);
//		roomClient = Middleware.getNewClient(Middleware.ROOM_CLIENT);
	}
	
	@Test
	public void testTransactionTimeout(){
		TMClient client = TMClient.getInstance();
		
		int id = client.start();
		
		
		for(int i = 0; i < 5; i++){
			System.out.println(System.currentTimeMillis() + ": waking up transaction");
			client.enlistCustomerRM(id);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

	
}
