package test;


import static org.junit.Assert.*;

import crash.CrashException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.Car;
import server.Flight;
import server.ResourceManagerImpl;
import server.Room;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;

public class TestTMServer {


	ResourceManagerImpl server;
	@Before
	public void beforeTest() throws NamingException, IOException, ClassNotFoundException {
		server = new ResourceManagerImpl("car");
	}
    @After
    public void cleanup(){
        File dir = new File("./record/");
        deleteFolder(dir);
    }
	
	@Test
	public void writeWithoutStart(){
		String location = "MONTREAL";
		int id = 1;
		int numCars = 50;
		int priceCars = 101;
	
		server.addCars(id, location, numCars, priceCars);
		
		assertEquals(0,server.queryCars(id, location));

	}
	
	@Test
	public void testAbort(){
		String location = "MONTREAL";
		int id = 1;
		int numCars = 50;
		int priceCars = 101;
		
		server.start(id);
		server.addCars(id, location, numCars, priceCars);
		
		assertEquals(50,server.queryCars(id, location));
		
		server.start(2);
		int flightNumber = 25;
		int numSeats = 26;
		int priceFlight = 27;
		server.addFlight(2, flightNumber, numSeats, priceFlight);
		
		
		server.abort(id);

        id = 3;
        server.start(id);
		assertEquals(0, server.queryCars(id, location));
		assertEquals(priceFlight,server.queryFlightPrice(2, 25));
		
		server.abort(2);
		assertEquals(0,server.queryFlight(id, flightNumber));
		
	}
	
	@Test
	public void writeAfterCommit() throws CrashException {
		String location = "MONTREAL";
		int id = 1;
		int numCars = 50;
		int priceCars = 101;
		
		server.start(id);
		server.addCars(id, location, numCars, priceCars);
		assertEquals(numCars,server.queryCars(id, location));

		assertEquals(priceCars,server.queryCarsPrice(id, location));

		server.commit(id);

        int id2 = 2;
        server.start(id2);
		assertEquals(priceCars, server.queryCarsPrice(id2, location));

		server.addCars(id, location, numCars, 7575);
		
		assertEquals(priceCars,server.queryCarsPrice(id2, location));
	}
	
	@Test
	public void testAddCar() throws CrashException {
		String location = "MONTREAL";
		int id = 1;
		int numCars = 50;
		int priceCars = 101;
		
		server.start(id);
		server.addCars(id, location, numCars, priceCars);
		
		assertEquals(numCars, server.queryCars(id, location));
		assertEquals(priceCars, server.queryCarsPrice(id, location));
		
		
		server.commit(1);
		
		id = 5; 
		
		server.start(id);
		assertEquals(numCars,server.queryCars(id, location));
		server.commit(id);
	}

	@Test
	public void testAddPlane() throws CrashException {
		int id = 1;
		int numSeats = 50;
		int flightPrice = 101;
		int flightNumber = 52;
		server.start(id);
		server.addFlight(id, flightNumber, numSeats, flightPrice);
		
		server.commit(1);
		
		id = 5;
		
		server.start(id);
		assertEquals(numSeats, server.queryFlight(id, flightNumber));
		assertEquals(flightPrice, server.queryFlightPrice(id, flightNumber));
		server.commit(id);
	}

	@Test
	public void testAddRoom() throws CrashException {
		String location = "MONTREAL";
		int id = 1;
		int numRooms = 50;
		int roomPrice = 101;
		
		server.start(id);
		server.addRooms(id, location, numRooms, roomPrice);
		
		assertEquals(numRooms,server.queryRooms(id, location));
		assertEquals(roomPrice, server.queryRoomsPrice(id, location));
		
		
		server.commit(1);
		
		id = 5;
		server.start(id);
		assertEquals(numRooms,server.queryRooms(id, location));
		server.commit(id);
	}

	@Test
	public void testReserveRoom() throws CrashException {
		String location = "MONTREAL";
		int id = 1;
		int numRooms = 50;
		int roomPrice = 101;
		
		server.start(id);
		server.addRooms(id, location, numRooms, roomPrice);

		int customerId = server.newCustomer(id);
		
		server.reserveRoom(id, customerId, location);
		
		server.reserveCustomer(id, customerId, Room.getKey(location), location, server.queryRoomsPrice(id, location));
		assertEquals(numRooms - 1, server.queryRooms(id, location));
		String bill = String.format("1 %s $%d",Room.getKey(location) ,roomPrice);
		assertTrue(server.getCustomerReservations(id, customerId).contains(bill));
		
		server.commit(customerId);
		
		id++;
		
		server.start(id);
		assertTrue(server.getCustomerReservations(id, customerId).contains(bill));
		//delete customer and cancel reservation
		server.deleteCustomer(id, customerId);
		server.cancelReserveRoom(id, customerId, location);
		assertEquals(numRooms, server.queryRooms(id, location));
		assertNull(server.getCustomerReservations(id, customerId));
		server.commit(id);
		
		
		
		
	}

	@Test
	public void testReservePlane() throws CrashException {
		String location = "MONTREAL";
		int id = 1;
		int flightNumber = 50;
		int flightPrice = 101;
		int numSeats = 37;
		server.start(id);
		server.addFlight(id, flightNumber, numSeats, flightPrice);
		
		int customerId = server.newCustomer(id);
		
		server.reserveFlight(id, customerId, flightNumber);
		server.reserveCustomer(id, customerId, Flight.getKey(flightNumber), location, server.queryFlightPrice(id, flightNumber));
		
		server.reserveFlight(id, customerId, flightNumber);
		
		server.reserveCustomer(id, customerId, Flight.getKey(flightNumber), location, server.queryFlightPrice(id, flightNumber));
		assertEquals(numSeats - 2, server.queryFlight(id, flightNumber));
		String bill = String.format("2 %s $%d",Flight.getKey(flightNumber) ,flightPrice);
		assertTrue(server.getCustomerReservations(id, customerId).contains(bill));
		
		server.commit(id);
		
		id++;
		
		server.start(id);
		assertTrue(server.getCustomerReservations(id, customerId).contains(bill));
		//delete customer and cancel reservation
		server.deleteCustomer(id, customerId);
		server.cancelReserveFlight(id, customerId, flightNumber);
		server.cancelReserveFlight(id, customerId, flightNumber);
		assertEquals(numSeats, server.queryFlight(id, flightNumber));
		assertNull(server.getCustomerReservations(id, customerId));
		server.commit(id);
		
		
	}

	@Test
	public void testReserveCar() throws CrashException {
		String location = "MONTREAL";
		int id = 1;
		int numCars = 50;
		int carPrice = 101;
		
		server.start(id);
		server.addCars(id, location, numCars, carPrice);

		int customerId = server.newCustomer(id);
		
		server.reserveCar(id, customerId, location);
		
		server.reserveCustomer(id, customerId, Car.getKey(location), location, server.queryCarsPrice(id, location));
		assertEquals(numCars - 1, server.queryCars(id, location));
		String bill = String.format("1 %s $%d",Car.getKey(location) ,carPrice);
		assertTrue(server.getCustomerReservations(id, customerId).contains(bill));
		
		server.commit(id);
		
		id++;
		
		server.start(id);
		assertTrue(server.getCustomerReservations(id, customerId).contains(bill));
		//delete customer and cancel reservation
		server.deleteCustomer(id, customerId);
		server.cancelReserveCar(id, customerId, location);
		assertEquals(numCars, server.queryCars(id, location));
		assertNull(server.getCustomerReservations(id, customerId));
		server.abort(id);

        id = 2;
        server.start(id);
		assertTrue(server.getCustomerReservations(id, customerId).contains(bill));
		
	}
    //test if previous bug is fixed
    @Test public void testRevertToPreviousCommit() throws CrashException {
        server.start(1);
        server.addCars(1,"MONTREAL",1,1);
        server.addCars(1,"TORONTO",2,2);
        server.commit(1);

        server.start(3);
        server.addCars(3,"TORONTO",3,2);
        server.abort(3);

        server.start(2);
        assertEquals(2,server.queryCars(2,"TORONTO"));
        server.addCars(2,"MONTREAL",2,1);
        server.commit(2);

        server.start(4);
        assertEquals(2,server.queryCars(4,"TORONTO"));
        assertEquals(3,server.queryCars(4,"MONTREAL"));
    }
    private  void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
	
}
