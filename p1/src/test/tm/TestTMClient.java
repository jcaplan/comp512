package test.tm;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import lockmanager.DeadlockException;
import lockmanager.LockManager;
import middleware.client.MWClientInterface;
import middleware.tm.TMClient;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	public void beforeTest(){
		servers.clear();
		clients.clear();
		lm = new LockManager();
		carServer = new ResourceManagerImpl();
		flightServer = new ResourceManagerImpl();
		roomServer = new ResourceManagerImpl();
		custServer = new ResourceManagerImpl();
		
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
		TMClient.deleteInstance();
		TMClient.getInstance().setClients(carClient, flightClient, roomClient, custClient);
		TMClient.getInstance().setLockManager(lm);
		
	}
	
	
	@Test
	public void initTest() throws DeadlockException{
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
}
