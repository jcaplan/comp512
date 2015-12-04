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
import crash.TestTMCrash;
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
		LockManager.reset();
		carServer = new ResourceManagerImpl("carRM");
		flightServer = new ResourceManagerImpl("flightRM");
		roomServer = new ResourceManagerImpl("roomRM");
		custServer = new ResourceManagerImpl("custRM");
		carServer.setTimeout(1000);
		flightServer.setTimeout(1000);
		roomServer.setTimeout(1000);
		custServer.setTimeout(1000);
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
		carServer.setTimeout(60000);
		roomServer.setTimeout(60000);
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
		tm.setCrash(new TestTMCrash());
		tm.setCrashLocation(crashLocation);
		try{
			result = tm.commit(id);
		} catch (CrashException e){
			TMClient.deleteInstance();
			LockManager.reset();
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
		carServer.setTimeout(120000);
		roomServer.setTimeout(120000);
		tm.setTimeout(120000);
		
		int id = tm.start();
		String location = "MONTREAL";
		int numCars = 63;
		int carPrice = 101;
		
		carClient.addCars(id, location, numCars, carPrice);
		roomClient.addRooms(id, location, numCars, carPrice);
		boolean result = tm.commit(id);
		
		result = false;
		id = tm.start();
		carPrice = 102;
		numCars = 37;
		carClient.addCars(id, location, numCars, carPrice);
		int roomPrice = 103;
		int numRooms = 99;
		roomClient.addRooms(id, location, numRooms, roomPrice);
		tm.setCrash(new TestTMCrash());
		tm.setCrashLocation(crashLocation);
		try{
			result = tm.commit(id);
		} catch (CrashException e){
			TMClient.deleteInstance();
			LockManager.reset();
			System.out.println(e.getMessage());
		}
		
		assertFalse(result); //transaction failed
		result = roomClient.start(id);
		assertTrue(result);
		result = carClient.start(id);
		assertFalse(result);
		
		
		
		//check persistence. should only work for car.
		ResourceManagerImpl newCarServer = new ResourceManagerImpl("carRM");
		newCarServer.start(100);
		assertEquals(100, newCarServer.queryCars(100, location));
		newCarServer.commit(100);
		ResourceManagerImpl newRoomServer = new ResourceManagerImpl("roomRM");
		newRoomServer.start(100);
		assertEquals(162,newRoomServer.queryRooms(100, location));
		newRoomServer.commit(100);

		
		
	}
	
	
	@Test
	public void crashTM3Test() throws DeadlockException, CrashException, InterruptedException, ClassNotFoundException, IOException {
		int crashLocation = 3;
		TMClient tm = TMClient.getInstance();
		
		int id = tm.start();
		String location = "MONTREAL";
		int numCars = 63;
		int carPrice = 101;
		
		carClient.addCars(id, location, numCars, carPrice);
		
		boolean result = tm.commit(id);
		assertTrue(result);
		result = false;
		id = tm.start();
		carPrice = 102;
		numCars = 37;
		carClient.addCars(id, location, numCars, carPrice);
		int roomPrice = 103;
		int numRooms = 99;
		roomClient.addRooms(id, location, numRooms, roomPrice);
		tm.setCrash(new TestTMCrash());
		tm.setCrashLocation(crashLocation);
		try{
			result = tm.commit(id);
		} catch (CrashException e){

			TMClient.deleteInstance();
			LockManager.reset();
			System.err.println(e.getMessage());
		}
		
//		assertFalse(result); //transaction failed
		
		//no timeout
//		result = carClient.start(id);
//		assertTrue(result);
//		carClient.abort(id);
//		result = roomClient.start(id);
//		assertTrue(result);
//		roomClient.abort(id);
//		
		
		// check persistence. both should commit.
		
		
		id = 100;
		carServer.start(id);
		assertEquals(id, carServer.queryCars(id, location));
		
		ResourceManagerImpl newCarServer = new ResourceManagerImpl("carRM");
		newCarServer.start(id);
		assertEquals(100, newCarServer.queryCars(id, location));
		newCarServer.commit(100);
		ResourceManagerImpl newRoomServer = new ResourceManagerImpl("roomRM");
		newRoomServer.start(id);
		assertEquals(99,newRoomServer.queryRooms(id, location));
		newRoomServer.commit(100);
		
		
	}
	
	@Test
	public void crashTM4Test() throws DeadlockException, CrashException, InterruptedException, ClassNotFoundException, IOException {
		int crashLocation = 4;
		TMClient tm = TMClient.getInstance();
		carServer.setTimeout(1000);
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
		tm.setCrash(new TestTMCrash());
		tm.setCrashLocation(crashLocation);
		try{
			result = tm.commit(id);
		} catch (CrashException e){
			TMClient.deleteInstance();
			LockManager.reset();
			System.out.println(e.getMessage());
		}
		resetTM();
		tm = TMClient.getInstance();
		carServer.start(101);
		assertEquals(63,carServer.queryCars(101, location));
		roomServer.start(101);
		assertEquals(0,roomServer.queryRooms(101, location));
		
		
		assertFalse(result); //transaction failed
		Thread.sleep(3000);
		
		//check persistence. both should commit.
		ResourceManagerImpl newRoomServer = new ResourceManagerImpl("roomRM");
		id = 100;
		newRoomServer.start(id);
		assertEquals(0,newRoomServer.queryRooms(id, location));
		newRoomServer.commit(id);
		ResourceManagerImpl newCarServer = new ResourceManagerImpl("carRM");
		newCarServer.start(id);
		assertEquals(63, newCarServer.queryCars(id, location));
		newCarServer.commit(id);
	}
	
	@Test
	public void crashRM1Test() throws DeadlockException, CrashException, InterruptedException, ClassNotFoundException, IOException {
		/*
		 * RM crashes before completing abort
		 */
		


		
		
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
		carClient.setCrashLocation(crashLocation);
		carClient.setCrashType(true);
		(new Thread(){
			public void run(){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					((MWTestClient) carClient).setRM(new ResourceManagerImpl("carRM"));
					System.out.println("RM back up!");
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		result = tm.abort(id);
		

		assertTrue(result); //transaction passes after retry
		Thread.sleep(500);
	}
	
	@Test
	public void crashRM2Test() throws DeadlockException, CrashException, InterruptedException, ClassNotFoundException, IOException {
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
		carClient.setCrashLocation(crashLocation);
		carClient.setCrashType(true);
		(new Thread(){
			public void run(){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					System.out.println("RM back up!");
					((MWTestClient) carClient).setRM(new ResourceManagerImpl("carRM"));
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		result = tm.commit(id);
		

		assertTrue(result); //transaction passes after retry
		
		
		ResourceManagerImpl newRoomServer = new ResourceManagerImpl("roomRM");
		id = 100;
		newRoomServer.start(id);
		assertEquals(99,newRoomServer.queryRooms(id, location));
		newRoomServer.commit(id);
		ResourceManagerImpl newCarServer = new ResourceManagerImpl("carRM");
		newCarServer.start(id);
		assertEquals(100, newCarServer.queryCars(id, location));
		newCarServer.commit(id);
		
	}
	
	@Test
	public void crashRM3Test() throws DeadlockException, CrashException, InterruptedException, ClassNotFoundException, IOException {
		int crashLocation = 3;
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
		carClient.setCrashLocation(crashLocation);
		carClient.setCrashType(true);
		result = tm.commit(id);
		

		assertFalse(result); //transaction passes after retry
		
	}
	

	
}
