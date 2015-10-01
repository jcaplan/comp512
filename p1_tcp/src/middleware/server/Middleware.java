// -------------------------------
// Adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package middleware.server;

import java.util.*;

import javax.jws.WebService;

import server.*;
import middleware.client.*;

@WebService(endpointInterface = "server.ws.ResourceManager")
public class Middleware implements server.ws.ResourceManager {

	protected RMHashtable m_itemHT = new RMHashtable();

	/*
	 * Set up the client stuff
	 */

	private static final int NUM_CLIENTS = 4;
	private static final int FLIGHT_CLIENT = 0;
	private static final int ROOM_CLIENT = 1;
	private static final int CAR_CLIENT = 2;
	private static final int CUSTOMER_CLIENT = 3;

	static volatile String[] clientServiceName = { "rmCar", "rmRoom",
			"rmFlight", "rmCustomer" };
	static volatile String[] clientServiceHost = { "localhost", "localhost",
			"localhost", "localhost" };
	static volatile int[] clientPort = { 18081, 18082, 18083, 18084 };

	public static void setParameters(String pclientServiceName,
			String pclientServiceHost, int pclientPort, int pclientType) {
		clientServiceName[pclientType] = pclientServiceName;
		clientServiceHost[pclientType] = pclientServiceHost;
		clientPort[pclientType] = pclientPort;
	}

	public static MWClient getNewClient(int clientType) {
		MWClient client = null;
		try {
			client = new MWClient(clientServiceName[clientType],
					clientServiceHost[clientType], clientPort[clientType]);
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

	private static MWClient getNewFlightClient() {
		return getNewClient(FLIGHT_CLIENT);
	}

	private static MWClient getNewRoomClient() {
		return getNewClient(ROOM_CLIENT);
	}

	private static MWClient getNewCarClient() {
		return getNewClient(CAR_CLIENT);
	}

	public static MWClient getNewCustomerClient() {
		return getNewClient(CUSTOMER_CLIENT);
	}

	// Flight operations //

	// Create a new flight, or add seats to existing flight.
	// Note: if flightPrice <= 0 and the flight already exists, it maintains
	// its current price.
	@Override
	public boolean addFlight(int id, int flightNumber, int numSeats,
			int flightPrice) {
		Trace.info("RM::addFlight(" + id + ", " + flightNumber + ", $"
				+ flightPrice + ", " + numSeats + ") called.");
		MWClient client = getNewFlightClient();
		boolean result = client.addFlight(id, flightNumber, numSeats,
				flightPrice);
		return result;

	}

	@Override
	public boolean deleteFlight(int id, int flightNumber) {
		Trace.info("RM::deleteFlight(" + id + ", " + flightNumber + ") called.");
		MWClient client = getNewFlightClient();
		return client.deleteFlight(id, flightNumber);
	}

	// Returns the number of empty seats on this flight.
	@Override
	public int queryFlight(int id, int flightNumber) {
		Trace.info("RM::queryFlight(" + id + ", " + flightNumber + ") called.");
		MWClient client = getNewFlightClient();
		return client.queryFlight(id, flightNumber);
	}

	// Returns price of this flight.
	public int queryFlightPrice(int id, int flightNumber) {
		Trace.info("RM::queryFlightPrice(" + id + ", " + flightNumber
				+ ") called.");
		MWClient client = getNewFlightClient();
		return client.queryFlightPrice(id, flightNumber);
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
	public boolean addCars(int id, String location, int numCars, int carPrice) {
		Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars
				+ ", $" + carPrice + ") called.");
		MWClient client = getNewCarClient();
		boolean result = client.addCars(id, location, numCars, carPrice);
		Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars
				+ ", $" + carPrice + ") OK.");
		return result;
	}

	// Delete cars from a location.
	@Override
	public boolean deleteCars(int id, String location) {
		Trace.info("RM::deleteCars(" + id + ", " + location + ") called.");
		MWClient client = getNewCarClient();
		boolean result = client.deleteCars(id, location);
		Trace.info("RM::addCars(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns the number of cars available at a location.
	@Override
	public int queryCars(int id, String location) {
		Trace.info("RM::queryCars(" + id + ", " + location + ") called.");
		MWClient client = getNewCarClient();
		int result = client.queryCars(id, location);
		Trace.info("RM::queryCars(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns price of cars at this location.
	@Override
	public int queryCarsPrice(int id, String location) {
		Trace.info("RM::queryCarsPrice(" + id + ", " + location + ") called.");
		MWClient client = getNewCarClient();
		int result = client.queryCarsPrice(id, location);
		Trace.info("RM::queryCarsPrice(" + id + ", " + location + ") OK.");
		return result;
	}

	// Room operations //

	// Create a new room location or add rooms to an existing location.
	// Note: if price <= 0 and the room location already exists, it maintains
	// its current price.
	@Override
	public boolean addRooms(int id, String location, int numRooms, int roomPrice) {
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms
				+ ", $" + roomPrice + ") called.");
		MWClient client = getNewRoomClient();
		boolean result = client.addRooms(id, location, numRooms, roomPrice);
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms
				+ ", $" + roomPrice + ") OK.");
		return result;
	}

	// Delete rooms from a location.
	@Override
	public boolean deleteRooms(int id, String location) {
		Trace.info("RM::deleteRooms(" + id + ", " + location + ") called.");
		MWClient client = getNewRoomClient();
		boolean result = client.deleteRooms(id, location);
		Trace.info("RM::deleteRooms(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns the number of rooms available at a location.
	@Override
	public int queryRooms(int id, String location) {
		Trace.info("RM::queryRooms(" + id + ", " + location + ") called.");
		MWClient client = getNewRoomClient();
		int result = client.queryRooms(id, location);
		Trace.info("RM::queryRooms(" + id + ", " + location + ") OK.");
		return result;
	}

	// Returns room price at this location.
	@Override
	public int queryRoomsPrice(int id, String location) {
		Trace.info("RM::queryRoomsPrice(" + id + ", " + location + ") called.");
		MWClient client = getNewRoomClient();
		int result = client.queryRoomsPrice(id, location);
		Trace.info("RM::queryRoomsPrice(" + id + ", " + location + ") OK.");
		return result;
	}

	// Customer operations //

	@Override
	public int newCustomer(int id) {
		Trace.info("INFO: RM::newCustomer(" + id + ") called.");
		// Generate a globally unique Id for the new customer.
		MWClient client = getNewCustomerClient();
		int customerId = client.newCustomer(id);
		Trace.info("RM::newCustomer(" + id + ") OK: " + customerId);
		return customerId;

	}

	// This method makes testing easier.
	@Override
	public boolean newCustomerId(int id, int customerId) {
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId
				+ ") called.");
		MWClient client = getNewCustomerClient();
		boolean result = client.newCustomerId(id, customerId);
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
	public boolean deleteCustomer(int id, int customerId) {
		// TODO
		// Trace.info("RM::deleteCustomer(" + id + ", " + customerId +
		// ") called.");
		// Customer cust = (Customer) readData(id, Customer.getKey(customerId));
		// if (cust == null) {
		// Trace.warn("RM::deleteCustomer(" + id + ", " + customerId
		// + ") failed: customer doesn't exist.");
		// return false;
		// } else {
		// // Increase the reserved numbers of all reservable items that
		// // the customer reserved.
		// RMHashtable reservationHT = cust.getReservations();
		// for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {
		// String reservedKey = (String) (e.nextElement());
		// ReservedItem reservedItem = cust.getReservedItem(reservedKey);
		// Trace.info("RM::deleteCustomer(" + id + ", " + customerId
		// + "): " + "deleting " + reservedItem.getCount()
		// + " reservations " + "for item "
		// + reservedItem.getKey());
		// ReservableItem item = (ReservableItem) readData(id,
		// reservedItem.getKey());
		// item.setReserved(item.getReserved() - reservedItem.getCount());
		// item.setCount(item.getCount() + reservedItem.getCount());
		// Trace.info("RM::deleteCustomer(" + id + ", " + customerId
		// + "): " + reservedItem.getKey()
		// + " reserved/available = " + item.getReserved() + "/"
		// + item.getCount());
		// }
		// // Remove the customer from the storage.
		// removeData(id, cust.getKey());
		// Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") OK.");
		return true;
		// }
	}

	// Return data structure containing customer reservation info.
	// Returns null if the customer doesn't exist.
	// Returns empty RMHashtable if customer exists but has no reservations.
	public RMHashtable getCustomerReservations(int id, int customerId) {
		// Trace.info("RM::getCustomerReservations(" + id + ", " + customerId
		// + ") called.");
		// Customer cust = (Customer) readData(id, Customer.getKey(customerId));
		// if (cust == null) {
		// Trace.info("RM::getCustomerReservations(" + id + ", " + customerId
		// + ") failed: customer doesn't exist.");
		// return null;
		// } else {
		// return cust.getReservations();
		// }
		return null;
	}

	// Return a bill.
	@Override
	public String queryCustomerInfo(int id, int customerId) {
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId
				+ ") called.");
		MWClient client = getNewCustomerClient();
		String result = client.queryCustomerInfo(id, customerId);
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId + ") OK.");
		return result;
	}

	// Add flight reservation to this customer.
	@Override
	public boolean reserveFlight(int id, int customerId, int flightNumber) {
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
			int flightNumber) {
		// Try to reserver a flight with the flight RM
		MWClient flightClient = getNewFlightClient();
		int price = 0;
		boolean flightStatus = flightClient.reserveItem(id, customerId,
				Flight.getKey(flightNumber), String.valueOf(flightNumber));
		if (flightStatus) {
			price = flightClient.queryFlightPrice(id, flightNumber);
		} else {
			return false;
		}
		// Add reservation to customer
		MWClient client = getNewCustomerClient();
		client.reserveCustomer(id, customerId, Flight.getKey(flightNumber),
				String.valueOf(flightNumber), price);

		return true;
	}

	// Add car reservation to this customer.
	@Override
	public boolean reserveCar(int id, int customerId, String location) {
		Trace.info("RM::reserveCar(" + id + ", " + customerId + "," + location
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
		return reserveCarNoCustCheck(id, customerId, location);
	}

	private boolean reserveCarNoCustCheck(int id, int customerId,
			String location) {
		// Try to reserver a car with the car RM
		MWClient carClient = getNewCarClient();
		int price = 0;
		boolean carStatus = carClient.reserveItem(id, customerId,
				Car.getKey(location), location);
		if (carStatus) {
			price = carClient.queryCarsPrice(id, location);
		} else {
			return false;
		}
		// Add reservation to customer
		MWClient client = getNewCustomerClient();
		client.reserveCustomer(id, customerId, Car.getKey(location), location,
				price);

		return true;
	}

	// Add room reservation to this customer.
	@Override
	public boolean reserveRoom(int id, int customerId, String location) {
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

	private boolean reserveRoomNoCust(int id, int customerId, String location) {
		// Try to reserver a car with the car RM
		MWClient roomClient = getNewRoomClient();
		int price = 0;
		boolean roomStatus = roomClient.reserveItem(id, customerId,
				Room.getKey(location), location);
		if (roomStatus) {
			price = roomClient.queryRoomsPrice(id, location);
		} else {
			return false;
		}
		// Add reservation to customer
		MWClient client = getNewCustomerClient();
		client.reserveCustomer(id, customerId, Room.getKey(location), location,
				price);

		return true;
	}

	// Reserve an itinerary.
	@Override
	public boolean reserveItinerary(int id, int customerId,
			Vector flightNumbers, String location, boolean car, boolean room) {
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
			Trace.info("Custome does not exist");
			return false;
		} else {
			Trace.info("Found customer: " + customerInfo);
		}

		boolean carSuccess = false;
		boolean roomSuccess = false;
		boolean flightSuccess = false;
		int numFlightsReserved = 0;
		
		MWClient carClient, roomClient = null, flightClient = null;
		// First try the car
		carClient = getNewCarClient(); 
		carSuccess = carClient.reserveItem(id, customerId,
				Car.getKey(location), location);
		if (carSuccess) {
			Trace.info("car reserved successfully");
		}

		// Don't bother with room if the car didn't succeed
		if (carSuccess) {
			roomClient = getNewRoomClient();
			roomSuccess = roomClient.reserveItem(id, customerId,
					Room.getKey(location), location);
		}
		if (roomSuccess) {
			Trace.info("room reserved successfully");
		}
		// Don't bother with flight if room didn't succeed
		if (roomSuccess) {
			flightClient = getNewFlightClient();
			for (Object fNumber : flightNumbers) {
				int flightNumber = Integer.parseInt(fNumber.toString());
				flightSuccess = flightClient.reserveItem(id, customerId,
						Flight.getKey(flightNumber), location);

				if(flightSuccess){
					numFlightsReserved++;
				} else {
					break;
				}
			}

		}
		

		boolean removeCar = carSuccess && !(roomSuccess || flightSuccess);
		boolean removeRoom = roomSuccess && !flightSuccess;
		boolean removeFlights = numFlightsReserved > 0 && !flightSuccess;

		if (removeCar) {
			
			carClient.cancelReserveItem(id, customerId, Car.getKey(location),
					location);
			Trace.info("removed car reservation");
		}
		if (removeRoom) {
			roomClient.cancelReserveItem(id, customerId, Room.getKey(location),
					location);
			Trace.info("removed room reservation");
		}
		if (removeFlights) {
			Trace.info("removing " + numFlightsReserved + "flight reservations");
			for (int i = 0; i < numFlightsReserved; i++) {
				String fNumber = flightNumbers.get(i).toString();
				int flightNumber = Integer.parseInt(fNumber);
				flightClient.cancelReserveItem(id, customerId,
						Flight.getKey(flightNumber), location);
			}
		}

		//if all 3 succeed then update the client
		if (carSuccess && roomSuccess && flightSuccess) {
			int price = 0;
			MWClient custClient = getNewCustomerClient();

			price = carClient.queryCarsPrice(id, location);
			custClient.reserveCustomer(id, customerId, Car.getKey(location),
					location, price);

			price = roomClient.queryRoomsPrice(id, location);
			custClient.reserveCustomer(id, customerId, Room.getKey(location),
					location, price);

			for (Object fNumber : flightNumbers) {
				int flightNumber = Integer.parseInt(fNumber.toString());
				price = flightClient.queryFlightPrice(id, flightNumber);
				custClient.reserveCustomer(id, customerId,
						Flight.getKey(flightNumber), location, price);
			}
			Trace.info("Itinerary booked successfully");
			return true;
		} else {
			return false;
		}
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
	public boolean reserveItem(int id, int customerId, String key,
			String location) {
		return false;
	}

	@Override
	public boolean cancelReserveCustomer(int id, int customerId, String key,
			String location, int price) {
		return false;
	}

	@Override
	public boolean cancelReserveItem(int id, int customerId, String key,
			String location) {
		return false;
	}

}
