package test.tm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import lockmanager.DeadlockException;
import lockmanager.LockManager;
import middleware.client.MWClientInterface;
import middleware.tm.TMClient;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import crash.CrashException;
import crash.TestCrash;
import server.RMPersistence;
import server.ResourceManagerImpl;
import server.ws.ResourceManager;


public class TestTMClient {

	//four MWClients
	
	MWClientInterface carClient;
	MWClientInterface flightClient;
	MWClientInterface roomClient;
	MWClientInterface custClient;
	static List<MWClientInterface> clients;
	
	ResourceManagerImpl carServer;
	ResourceManagerImpl flightServer;
	ResourceManagerImpl roomServer;
	ResourceManagerImpl custServer;
	static List<ResourceManager> servers;
	
	
	LockManager lm;
	TMClient tm;
	@BeforeClass
	public static void beforeClass(){
		clients = new ArrayList<>();
		servers = new ArrayList<>();
	}
	
	@Before
	public void beforeTest() throws ClassNotFoundException, NamingException, IOException{

		RMPersistence.deleteAllRecords();
		servers.clear();
		clients.clear();
		lm = new LockManager();
		carServer = new ResourceManagerImpl("carRM");
		flightServer = new ResourceManagerImpl("flightRM");
		roomServer = new ResourceManagerImpl("roomRM");
		custServer = new ResourceManagerImpl("custRM");
		
		servers.add(carServer);
		servers.add(flightServer);
		servers.add(roomServer);
		servers.add(custServer);
		
		carClient = new MWTestClient(lm, carServer);
		flightClient = new MWTestClient(lm, flightServer);
		roomClient = new MWTestClient(lm, roomServer);
		custClient = new MWTestClient(lm, custServer);
		
		clients.add(carClient);
		clients.add(flightClient);
		clients.add(roomClient);
		clients.add(custClient);
		resetTM();
		
	}
	
	
	private void resetTM() {
		TMClient.deleteInstance();
		TMClient.getInstance().setClients(carClient, flightClient, roomClient, custClient);
		TMClient.getInstance().setLockManager(lm);	
	}

	@Test
	public void initTest() throws DeadlockException, CrashException{
		//Simulating middleware input to TM and RM
		
		TMClient tm = TMClient.getInstance();
		
		int id = tm.start();
		String location = "MONTREAL";
		int numCars = 63;
		int carPrice = 101;
		
		carClient.addCars(id, location, numCars, carPrice);
		
		boolean result = tm.commit(id);
		
		
		id = tm.start();
		carPrice = 102;
		numCars = 37;
		carClient.addCars(id, location, numCars, carPrice);
		int roomPrice = 103;
		int numRooms = 99;
		roomClient.addRooms(id, location, numRooms, roomPrice);
		result = tm.commit(id);
		assertTrue(result);
		
		
		id = tm.start();
		assertEquals(carPrice,carClient.queryCarsPrice(id, location));
		assertEquals(numRooms,roomClient.queryRooms(id, location));
		result = tm.commit(id);
		
		assertTrue(result);
		
	}
	
	
	/*
	 * Test 1: send votes to all RMs... should wait indefinitely...
	 */
	@Test
	public void crashTM1Test() throws DeadlockException, CrashException, InterruptedException {
		int crashLocation = 1;
		TMClient tm = TMClient.getInstance();
		
		int id = tm.start();
		String location = "MONTREAL";
		int numCars = 63;
		int carPrice = 101;
		
		carClient.addCars(id, location, numCars, carPrice);
		
		boolean result = tm.commit(id);
		
		result = false;
		id = tm.start();
		carPrice = 102;
		numCars = 37;
		carClient.addCars(id, location, numCars, carPrice);
		int roomPrice = 103;
		int numRooms = 99;
		roomClient.addRooms(id, location, numRooms, roomPrice);
		tm.setCrash(new TestCrash());
		tm.setCrashLocation(crashLocation);
		try{
			result = tm.commit(id);
		} catch (CrashException e){
			System.out.println("commit crashed!");
		}
		resetTM();
		tm = TMClient.getInstance();
		
		assertFalse(result); //transaction failed
		Thread.sleep(6000);
		
		//no timeout
		result = carClient.start(id);
		assertFalse(result);
		result = roomClient.start(id);
		assertFalse(result);
	}
	
	/*
	 * Partial result committed
	 */
	@Test
	public void crashTM2Test() throws DeadlockException, CrashException, InterruptedException, ClassNotFoundException, IOException {
		int crashLocation = 2;
		TMClient tm = TMClient.getInstance();
		
		int id = tm.start();
		String location = "MONTREAL";
		int numCars = 63;
		int carPrice = 101;
		
		carClient.addCars(id, location, numCars, carPrice);
		
		boolean result = tm.commit(id);
		
		result = false;
		id = tm.start();
		carPrice = 102;
		numCars = 37;
		carClient.addCars(id, location, numCars, carPrice);
		int roomPrice = 103;
		int numRooms = 99;
		roomClient.addRooms(id, location, numRooms, roomPrice);
		tm.setCrash(new TestCrash());
		tm.setCrashLocation(crashLocation);
		try{
			result = tm.commit(id);
		} catch (CrashException e){
			System.out.println("commit crashed!");
		}
		resetTM();
		tm = TMClient.getInstance();
		
		assertFalse(result); //transaction failed
		Thread.sleep(6000);
		
		//no timeout
		result = carClient.start(id);
		assertTrue(result);
		carClient.abort(id);
		result = roomClient.start(id);
		assertFalse(result);
		
		
		
		//check persistence. should only work for car.
		id = tm.start();
		ResourceManagerImpl newCarServer = new ResourceManagerImpl("carRM");

		assertEquals(100, newCarServer.queryCars(id, location));
		ResourceManagerImpl newRoomServer = new ResourceManagerImpl("roomRM");
		assertEquals(0,newRoomServer.queryRooms(id, location));
		tm.commit(id);
		
		
	}
}
