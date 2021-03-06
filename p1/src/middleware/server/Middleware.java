// -------------------------------
// Adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package middleware.server;

import java.io.PrintStream;
import java.net.Socket;
import java.util.*;

import javax.jws.WebService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import crash.CrashException;
import crash.WSCrash;
import lockmanager.DeadlockException;
import lockmanager.LockManager;
import server.*;
import middleware.client.*;
import middleware.tm.TMClient;

@WebService(endpointInterface = "middleware.server.ResourceManager")
public class Middleware implements ResourceManager {

	protected RMHashtable m_itemHT = new RMHashtable();

	/*
	 * Set up the client stuff
	 */

	public static final int NUM_CLIENTS = 4;
	public static final int FLIGHT_CLIENT = 2;
	public static final int ROOM_CLIENT = 1;
	public static final int CAR_CLIENT = 0;
	public static final int CUSTOMER_CLIENT = 3;
	private int middleWareShutdownPort = 4;

	private LockManager lockManager;

	static volatile String[] clientServiceName = { "rmCar", "rmRoom",
			"rmFlight", "rmCustomer" };
	static volatile String[] clientServiceHost = { "localhost", "localhost",
			"localhost", "localhost" };
	static volatile int[] clientPort = { 18081, 18082, 18083, 18084 };
	
	static int[] shutdownPort = {18085,18086,18087,18088};

	public static void setParameters(String pclientServiceName,
			String pclientServiceHost, int pclientPort, int pclientType) {
		clientServiceName[pclientType] = pclientServiceName;
		clientServiceHost[pclientType] = pclientServiceHost;
		clientPort[pclientType] = pclientPort;
	}

	MWClient carClient;
	MWClient flightClient;
	MWClient roomClient;
	MWClient custClient;

	public MWClient getNewClient(int clientType) {
		MWClient client = null;
		try {
			client = new MWClient(clientServiceName[clientType],
					clientServiceHost[clientType], clientPort[clientType],
					lockManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (client == null) {
			System.out
					.println("OHNO***************************************************************");
			System.out.println("client: " + clientServiceName[clientType]
					+ ", " + clientServiceHost[clientType] + ", "
					+ clientPort[clientType]);
			System.exit(-1);
		}
		return client;
	}

	public static void setFlightClient(String clientServiceName,
			String clientServiceHost, int clientPort) {
		setParameters(clientServiceName, clientServiceHost, clientPort,
				FLIGHT_CLIENT);
		System.out.println("new thread... " + Thread.currentThread().getId());
	}

	public static void setRoomClient(String clientServiceName,
			String clientServiceHost, int clientPort) {
		setParameters(clientServiceName, clientServiceHost, clientPort,
				ROOM_CLIENT);
	}

	public static void setCarClient(String clientServiceName,
			String clientServiceHost, int clientPort) {
		setParameters(clientServiceName, clientServiceHost, clientPort,
				CAR_CLIENT);
	}

	public static void setCustomerClient(String clientServiceName,
			String clientServiceHost, int clientPort) {
		setParameters(clientServiceName, clientServiceHost, clientPort,
				CUSTOMER_CLIENT);
	}

	private MWClient getNewFlightClient() {
		return getNewClient(FLIGHT_CLIENT);
	}

	private MWClient getNewRoomClient() {
		return getNewClient(ROOM_CLIENT);
	}

	private MWClient getNewCarClient() {
		return getNewClient(CAR_CLIENT);
	}

	public MWClient getNewCustomerClient() {
		return getNewClient(CUSTOMER_CLIENT);
	}

	public Middleware() throws NamingException {
		this.lockManager = new LockManager();

		Context env = (Context) new InitialContext().lookup("java:comp/env");

		String carServiceHost = (String) env.lookup("car-service-host");
		Integer carServicePort = (Integer) env.lookup("car-service-port");
		setCarClient(clientServiceName[CAR_CLIENT], carServiceHost,
				carServicePort);
		shutdownPort[CAR_CLIENT] = (Integer) env.lookup("car-shutdown-port");

		String roomServiceHost = (String) env.lookup("room-service-host");
		Integer roomServicePort = (Integer) env.lookup("room-service-port");
		setRoomClient(clientServiceName[ROOM_CLIENT], roomServiceHost,
				roomServicePort);
		shutdownPort[ROOM_CLIENT] = (Integer) env.lookup("room-shutdown-port");

		String flightServiceHost = (String) env.lookup("flight-service-host");
		Integer flightServicePort = (Integer) env.lookup("flight-service-port");
		setFlightClient(clientServiceName[FLIGHT_CLIENT], flightServiceHost,
				flightServicePort);

		shutdownPort[FLIGHT_CLIENT] = (Integer) env.lookup("flight-shutdown-port");

		String customerServiceHost = (String) env
				.lookup("customer-service-host");
		Integer customerServicePort = (Integer) env
				.lookup("customer-service-port");
		setCustomerClient(clientServiceName[CUSTOMER_CLIENT],
				customerServiceHost, customerServicePort);
		shutdownPort[CUSTOMER_CLIENT] = (Integer) env.lookup("customer-shutdown-port");
		
		middleWareShutdownPort = (Integer) env.lookup("middleware-shutdown-port");
		
		carClient = getNewCarClient();
		roomClient = getNewRoomClient();
		flightClient = getNewFlightClient();
		custClient = getNewCustomerClient();

		TMClient.getInstance().setClients(carClient, flightClient, roomClient,
				custClient);
		TMClient.getInstance().setLockManager(lockManager);

		TMClient.getInstance().setCrash(new WSCrash());
	}

	@Override
	public int start() {
		return TMClient.getInstance().start();
	}

	@Override
	public boolean commit(int id) throws CrashException{
		boolean result = false;

		result = TMClient.getInstance().commit(id);
		return result;
	}

	@Override
	public boolean abort(int id) {
		boolean result = TMClient.getInstance().abort(id);
		return result;
	}

	@Override
	public boolean shutdown() {
		//check that no transactions are active first
		if(TMClient.getInstance().txnsActive()){
			System.out.println("MW::Cannot shut down txns still active");
			return false;
		}
		try{
			
			for(int i = 0; i < NUM_CLIENTS; i++){
				Socket socket = new Socket(clientServiceHost[i],shutdownPort[i]);
				PrintStream ps = new PrintStream(socket.getOutputStream());
				ps.println("SHUTDOWN");
				socket.close();
			}
			
			//now shutdown self
			Socket socket = new Socket("localhost",middleWareShutdownPort);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.println("SHUTDOWN");
			socket.close();
			
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void setCrashRM(int rm, int location){
		switch(rm){
		case FLIGHT_CLIENT:
			flightClient.setCrashType(false);
			flightClient.setCrashLocation(location);
			break;
		case ROOM_CLIENT:
			roomClient.setCrashType(false);
			roomClient.setCrashLocation(location);
			break;
		case CAR_CLIENT:
			carClient.setCrashType(false);
			carClient.setCrashLocation(location);
			break;
		case CUSTOMER_CLIENT:
			custClient.setCrashType(false);
			custClient.setCrashLocation(location);
			break;
		}
	}

	public void setCrashTM(int location){
		TMClient.getInstance().setCrash(new WSCrash());
		TMClient.getInstance().setCrashLocation(location);
	}
	// Flight operations //

	// Create a new flight, or add seats to existing flight.
	// Note: if flightPrice <= 0 and the flight already exists, it maintains
	// its current price.
	@Override
	public boolean addFlight(int id, int flightNumber, int numSeats,
			int flightPrice) throws DeadlockException {
		Trace.info("RM::addFlight(" + id + ", " + flightNumber + ", $"
				+ flightPrice + ", " + numSeats + ") called.");
		boolean result = false;

		result = flightClient
				.addFlight(id, flightNumber, numSeats, flightPrice);

		return result;

	}

	@Override
	public boolean deleteFlight(int id, int flightNumber)
			throws DeadlockException {
		Trace.info("RM::deleteFlight(" + id + ", " + flightNumber + ") called.");
		return flightClient.deleteFlight(id, flightNumber);
	}

	// Returns the number of empty seats on this flight.
	@Override
	public int queryFlight(int id, int flightNumber) throws DeadlockException {
		Trace.info("RM::queryFlight(" + id + ", " + flightNumber + ") called.");
		return flightClient.queryFlight(id, flightNumber);
	}

	// Returns price of this flight.
	public int queryFlightPrice(int id, int flightNumber)
			throws DeadlockException {
		Trace.info("RM::queryFlightPrice(" + id + ", " + flightNumber
				+ ") called.");
		return flightClient.queryFlightPrice(id, flightNumber);
	}

	/*
	 * // Returns the number of reservations for this flight. public int
	 * queryFlightReservations(int id, int flightNumber) {
	 * Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNumber +
	 * ") called."); RMInteger numReservations = (RMInteger) readData(id,
	 * Flight.getNumReservationsKey(flightNumber)); if (numReservations == null)
	 * { numReservations = new RMInteger(0); }
	 * Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNumber +
	 * ") = " + numReservations); return numReservations.getValue(); }
	 */

	/*
	 * // Frees flight reservation record. Flight reservation records help us //
	 * make sure we don't delete a flight if one or more customers are //
	 * holding reservations. public boolean freeFlightReservation(int id, int
	 * flightNumber) { Trace.info("RM::freeFlightReservations(" + id + ", " +
	 * flightNumber + ") called."); RMInteger numReservations = (RMInteger)
	 * readData(id, Flight.getNumReservationsKey(flightNumber)); if
	 * (numReservations != null) { numReservations = new RMInteger( Math.max(0,
	 * numReservations.getValue() - 1)); } writeData(id,
	 * Flight.getNumReservationsKey(flightNumber), numReservations);
	 * Trace.info("RM::freeFlightReservations(" + id + ", " + flightNumber +
	 * ") OK: reservations = " + numReservations); return true; }
	 */

	// Car operations //

	// Create a new car location or add cars to an existing location.
	// Note: if price <= 0 and the car location already exists, it maintains
	// its current price.
	@Override
	public boolean addCars(int id, String location, int numCars, int carPrice)
			throws DeadlockException {
		Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars
				+ ", $" + carPrice + ") called.");
		boolean result = false;
		result = carClient.addCars(id, location, numCars, carPrice);
		Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars
				+ ", $" + carPrice + ") OK.");
		return result;
	}

	// Delete cars from a location.
	@Override
	public boolean deleteCars(int id, String location) throws DeadlockException {
		Trace.info("RM::deleteCars(" + id + ", " + location + ") called.");
		boolean result = false;
		result = carClient.deleteCars(id, location);
		Trace.info("RM::addCars(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns the number of cars available at a location.
	@Override
	public int queryCars(int id, String location) throws DeadlockException {
		Trace.info("RM::queryCars(" + id + ", " + location + ") called.");
		int result = 0;
		result = carClient.queryCars(id, location);
		Trace.info("RM::queryCars(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns price of cars at this location.
	@Override
	public int queryCarsPrice(int id, String location) throws DeadlockException {
		Trace.info("RM::queryCarsPrice(" + id + ", " + location + ") called.");
		int result = 0;
		result = carClient.queryCarsPrice(id, location);
		Trace.info("RM::queryCarsPrice(" + id + ", " + location + ") OK.");
		return result;
	}

	// Room operations //

	// Create a new room location or add rooms to an existing location.
	// Note: if price <= 0 and the room location already exists, it maintains
	// its current price.
	@Override
	public boolean addRooms(int id, String location, int numRooms, int roomPrice)
			throws DeadlockException {
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms
				+ ", $" + roomPrice + ") called.");
		boolean result = false;
		result = roomClient.addRooms(id, location, numRooms, roomPrice);
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms
				+ ", $" + roomPrice + ") OK.");
		return result;
	}

	// Delete rooms from a location.
	@Override
	public boolean deleteRooms(int id, String location)
			throws DeadlockException {
		Trace.info("RM::deleteRooms(" + id + ", " + location + ") called.");
		boolean result = false;
		result = roomClient.deleteRooms(id, location);
		Trace.info("RM::deleteRooms(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns the number of rooms available at a location.
	@Override
	public int queryRooms(int id, String location) throws DeadlockException {
		Trace.info("RM::queryRooms(" + id + ", " + location + ") called.");
		int result = 0;
		result = roomClient.queryRooms(id, location);
		Trace.info("RM::queryRooms(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns room price at this location.
	@Override
	public int queryRoomsPrice(int id, String location)
			throws DeadlockException {
		Trace.info("RM::queryRoomsPrice(" + id + ", " + location + ") called.");
		int result = 0;
		result = roomClient.queryRoomsPrice(id, location);
		Trace.info("RM::queryRoomsPrice(" + id + ", " + location + ") OK.");
		return result;
	}

	// Customer operations //

	@Override
	public int newCustomer(int id) {
		Trace.info("INFO: RM::newCustomer(" + id + ") called.");
		// Generate a globally unique Id for the new customer.
		int customerId = custClient.newCustomer(id);
		Trace.info("RM::newCustomer(" + id + ") OK: " + customerId);
		return customerId;

	}

	// This method makes testing easier.
	@Override
	public boolean newCustomerId(int id, int customerId) {
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId
				+ ") called.");
		boolean result = custClient.newCustomerId(id, customerId);
		if (result) {
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId
					+ ") OK.");
		} else {
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId
					+ ") failed: customer already exists.");
		}
		return result;
	}

	// Delete customer from the database.
	@Override
	public boolean deleteCustomer(int id, int customerId)
			throws DeadlockException {
		Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") called.");
		// Check if customer exists
		String customerInfo = queryCustomerInfo(id, customerId);
		// Check if customer exists
		if (customerInfo.isEmpty()) {
			Trace.info("Customer does not exist");
			return false;
		} else {
			Trace.info("Found customer: " + customerInfo);
		}

		// Get the bill for the customer
		String reservations = customerInfo;
		System.out.println(reservations);

		// Now parse the bill
		// First parse the string by lines
		StringTokenizer tokenizer = new StringTokenizer(reservations, "\n");
		while (tokenizer.hasMoreTokens()) {

			// Now parse the bill using spaces
			String line = tokenizer.nextToken().trim();
			String[] tokens = line.split(" ");
			// Skip lines that are not relevant
			if (tokens[0].equals("Bill") || tokens.length < 2) {
				continue;
			}

			// The key will be "type-arg" so split it around the '-' and
			// then cancel the item
			for (String t : tokens) {
				System.out.print(t);
			}
			System.out.println();
			int numItems = Integer.parseInt(tokens[0]);
			String[] keyArray = tokens[1].split("-");
			MWClient client;
			if (keyArray[0].equals("flight")) {
				for (int i = 0; i < numItems; i++) {
					flightClient.cancelReserveFlight(id, customerId,
							Integer.parseInt(keyArray[1]));
				}
			} else if (keyArray[0].equals("car")) {
				for (int i = 0; i < numItems; i++) {
					carClient.cancelReserveCar(id, customerId, keyArray[1]);
				}
			} else if (keyArray[0].equals("room")) {
				for (int i = 0; i < numItems; i++) {
					roomClient.cancelReserveRoom(id, customerId, keyArray[1]);
				}
			}
		}

		return custClient.deleteCustomer(id, customerId);

	}

	// Return data structure containing customer reservation info.
	// Returns null if the customer doesn't exist.
	// Returns empty RMHashtable if customer exists but has no reservations.
	public RMHashtable getCustomerReservations(int id, int customerId) {
		return null;
	}

	// Return a bill.
	@Override
	public String queryCustomerInfo(int id, int customerId)
			throws DeadlockException {
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId
				+ ") called.");
		String result = null;
		result = custClient.queryCustomerInfo(id, customerId);
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId + ") OK.");
		return result;
	}

	// Add flight reservation to this customer.
	@Override
	public boolean reserveFlight(int id, int customerId, int flightNumber)
			throws DeadlockException {
		Trace.info("RM::reserveFlight(" + id + ", " + customerId + ","
				+ flightNumber + ") called.");
		Trace.info("Retrieving customer data");
		String customerInfo = queryCustomerInfo(id, customerId);
		// Check if customer exists
		if (customerInfo.isEmpty()) {
			Trace.info("Custome does not exist");
			return false;
		} else {
			Trace.info("Found customer: " + customerInfo);
		}
		return reserveFlightNoCustCheck(id, customerId, flightNumber);
	}

	private boolean reserveFlightNoCustCheck(int id, int customerId,
			int flightNumber) throws DeadlockException {
		// Try to reserver a flight with the flight RM
		int price = 0;
		boolean flightStatus = flightClient.reserveFlight(id, customerId,
				flightNumber);
		if (flightStatus) {
			price = flightClient.queryFlightPrice(id, flightNumber);
		} else {
			return false;
		}
		// Add reservation to customer
		custClient.reserveCustomer(id, customerId, Flight.getKey(flightNumber),
				String.valueOf(flightNumber), price);

		return true;
	}

	// Add car reservation to this customer.
	@Override
	public boolean reserveCar(int id, int customerId, String location)
			throws DeadlockException {
		Trace.info("RM::reserveCar(" + id + ", " + customerId + "," + location
				+ ") called.");
		Trace.info("Retrieving customer data");
		String customerInfo = queryCustomerInfo(id, customerId);
		// Check if customer exists
		if (customerInfo.isEmpty()) {
			Trace.info("Customer does not exist");
			return false;
		} else {
			Trace.info("Found customer: " + customerInfo);
		}
		return reserveCarNoCustCheck(id, customerId, location);
	}

	private boolean reserveCarNoCustCheck(int id, int customerId,
			String location) throws DeadlockException {
		// Try to reserver a car with the car RM
		int price = 0;
		boolean carStatus = carClient.reserveCar(id, customerId, location);
		if (carStatus) {
			price = carClient.queryCarsPrice(id, location);
		} else {
			return false;
		}
		// Add reservation to customer
		custClient.reserveCustomer(id, customerId, Car.getKey(location),
				location, price);
		return true;
	}

	// Add room reservation to this customer.
	@Override
	public boolean reserveRoom(int id, int customerId, String location)
			throws DeadlockException {
		Trace.info("RM::reserveRoom(" + id + ", " + customerId + "," + location
				+ ") called.");
		Trace.info("Retrieving customer data");
		String customerInfo = queryCustomerInfo(id, customerId);
		// Check if customer exists
		if (customerInfo.isEmpty()) {
			Trace.info("Custome does not exist");
			return false;
		} else {
			Trace.info("Found customer: " + customerInfo);
		}
		return reserveRoomNoCust(id, customerId, location);
	}

	private boolean reserveRoomNoCust(int id, int customerId, String location)
			throws DeadlockException {
		// Try to reserver a car with the car RM
		int price = 0;
		boolean roomStatus = roomClient.reserveRoom(id, customerId, location);
		if (roomStatus) {
			price = roomClient.queryRoomsPrice(id, location);
		} else {
			return false;
		}
		// Add reservation to customer
		custClient.reserveCustomer(id, customerId, Room.getKey(location),
				location, price);
		return true;
	}

	// Reserve an itinerary.
	@Override
	public boolean reserveItinerary(int id, int customerId,
			Vector flightNumbers, String location, boolean car, boolean room)
			throws DeadlockException {
		String s = "RM::reserveItinerary(" + id + ", " + customerId + ",";

		for (Object o : flightNumbers) {
			s += o + ",";
		}
		s += location + "," + car + "," + room + ") called.";
		Trace.info(s);
		Trace.info("Retrieving customer data");
		String customerInfo = queryCustomerInfo(id, customerId);
		// Check if customer exists
		if (customerInfo.isEmpty()) {
			Trace.info("Customer does not exist");
			return false;
		} else {
			Trace.info("Found customer: " + customerInfo);
		}
		
		boolean carResult = true;
		boolean roomResult = true;
		boolean flightResult = true;
		
		if(car){
			carResult = carClient.reserveCar(id, customerId, location);
		}
		if(room){
			roomResult = roomClient.reserveRoom(id, customerId, location);
		}
		for (Object fNumber : flightNumbers) {
			int flightNumber = Integer.parseInt(fNumber.toString());
			flightResult = flightClient.reserveFlight(id, customerId, flightNumber);
			if(!flightResult)
				break;
		}

		if(!(carResult && roomResult && flightResult)){
			
			return abort(id);
		}
		
		int price = 0;

		if (car) {
			price = carClient.queryCarsPrice(id, location);
			custClient.reserveCustomer(id, customerId, Car.getKey(location),
					location, price);
		}
		if (room) {
			price = roomClient.queryRoomsPrice(id, location);
			custClient.reserveCustomer(id, customerId, Room.getKey(location),
					location, price);
		}
		for (Object fNumber : flightNumbers) {
			int flightNumber = Integer.parseInt(fNumber.toString());
			price = flightClient.queryFlightPrice(id, flightNumber);
			custClient.reserveCustomer(id, customerId,
					Flight.getKey(flightNumber), location, price);
		}
		Trace.info("Itinerary booked successfully");
		return true;

	}

	/**
	 * Methods reserveCustomer and reserveItem not implemented for client side.
	 * Only for RM and middleware.
	 */
	@Override
	public boolean reserveCustomer(int id, int customerId, String key,
			String location, int price) {
		return false;
	}

	@Override
	public boolean cancelReserveFlight(int id, int customerId, int flightNumber) {
		return false;
	}

	// Add car reservation to this customer.
	@Override
	public boolean cancelReserveCar(int id, int customerId, String location) {
		return false;
	}

	// Add room reservation to this customer.
	@Override
	public boolean cancelReserveRoom(int id, int customerId, String location) {
		return false;
	}

	@Override
	public boolean cancelReserveCustomer(int id, int customerId, String key,
			String location, int price) {
		return false;
	}

}
