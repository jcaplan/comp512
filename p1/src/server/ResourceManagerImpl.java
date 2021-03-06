// -------------------------------
// Adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package server;

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.jws.WebService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import crash.Crash;
import crash.CrashException;
import crash.TestRMCrash;
import crash.WSCrash;
import server.tm.TMServer;

@WebService(endpointInterface = "server.ws.ResourceManager")
public class ResourceManagerImpl implements server.ws.ResourceManager {

	protected RMHashtable m_itemHT = new RMHashtable();
    private TMServer tmServer;
	private int crashLocation = -1;
	private Crash crash;
	Object syncLock = new Object();
	String name;
    public ResourceManagerImpl() throws NamingException, IOException, ClassNotFoundException {
        this((String)(((Context) new InitialContext().lookup("java:comp/env")).lookup("service-name")));
     }

    public ResourceManagerImpl(String serviceName) throws IOException, ClassNotFoundException {
        tmServer = new TMServer(serviceName);
        m_itemHT = tmServer.getCommittedTable();
        name = serviceName;
    }

	@Override
	public boolean abort(int id) throws CrashException {
        boolean aborted;
        //TODO  crash here
        
        if(crashLocation == 1){
        	crash.crash("RM crashes before aborting");
        }
        
		synchronized(m_itemHT){
			aborted =  tmServer.abortTxn(id);
		}
        return aborted;
	}
	
	@Override
	public boolean start(int id){
		synchronized(m_itemHT){
			return tmServer.start(id);
		}
	}
	
	@Override
	public boolean commit(int id) throws CrashException{
        boolean committed;
        
        //TODO  crash here
        
        if(crashLocation == 2){
        	crash.crash("RM crashes before committing");
        }
		synchronized(m_itemHT){
			committed =  tmServer.commitTxn(id);
		}
        return committed;
	}
	
	@Override
	public boolean shutdown(){
		return false;
	}




	private void setPrice(int id, ReservableItem item, int price){
		//Checks that the transaction ID is valid...
		if (tmServer.isTxnActive(id)){
            tmServer.writeData(id, item.getKey(),item);
            item.setPrice(price);
        }

	}
	
	private void setCount(int id, ReservableItem item, int count){
        if (tmServer.isTxnActive(id)){
            tmServer.writeData(id, item.getKey(),item);
            item.setCount(count);
        }
	}
	
	private void setReserved(int id, ReservableItem item, int reserved){
        if (tmServer.isTxnActive(id)){
            tmServer.writeData(id, item.getKey(),item);
            item.setReserved(reserved);
        }
	}
	
	private void custReserve(int id, Customer cust, String key, String location, int  price){
		ReservedItem item = cust.getReservedItem(key);

        if (tmServer.isTxnActive(id)) {
            tmServer.writeData(id, cust.getKey(),item);
            cust.reserve(key, location, price);
        }
	}
	
	// Read a data item.
	private RMItem readData(int id, String key) {
		System.out.println("txn active "+ id + ": " + tmServer.isTxnActive(id));
		tmServer.resetTimer(id);
        RMItem result = null;
		synchronized (m_itemHT) {
            if (tmServer.isTxnActive(id))
			    result =  (RMItem) m_itemHT.get(key);
		}
        return result;
	}

	// Write a data item.
	private void writeData(int id, String key, RMItem value) {
		synchronized (m_itemHT) {
			TMServer tm = tmServer;
            if (tm.isTxnActive(id)){
                tm.writeData(id, key,(RMItem)m_itemHT.get(key));
                m_itemHT.put(key, value);
            }
		}
	}

	// Remove the item out of storage.
	protected RMItem removeData(int id, String key) {
        RMItem result = null;
		synchronized (m_itemHT) {
			TMServer tm = tmServer;
			if (tm.isTxnActive(id)){
                tm.removeData(id,key, (RMItem)m_itemHT.get(key));
                result =  (RMItem) m_itemHT.remove(key);
            }
		}
        return result;
	}

	// Basic operations on ReservableItem //

	// Delete the entire item.
	protected boolean deleteItem(int id, String key) {
		Trace.info("RM::deleteItem(" + id + ", " + key + ") called.");
		synchronized(syncLock){
			ReservableItem curObj = (ReservableItem) readData(id, key);
			// Check if there is such an item in the storage.
			if (curObj == null) {
				Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed: "
						+ " item doesn't exist.");
				return false;
			} else {
				if (curObj.getReserved() == 0) {
					removeData(id, curObj.getKey());
					Trace.info("RM::deleteItem(" + id + ", " + key + ") OK.");
					return true;
				} else {
					Trace.info("RM::deleteItem(" + id + ", " + key + ") failed: "
							+ "some customers have reserved it.");
					return false;
				}
			}
		}
	}

	// Query the number of available seats/rooms/cars.
	protected int queryNum(int id, String key) {
		Trace.info("RM::queryNum(" + id + ", " + key + ") called.");
		synchronized(syncLock){
			ReservableItem curObj = (ReservableItem) readData(id, key);
			System.out.println("queried object:" + curObj);
			int value = 0;
			if (curObj != null) {
				value = curObj.getCount();
			} else {
				Trace.info("RM::queryNum: did not find data...");
			}
			Trace.info("RM::queryNum(" + id + ", " + key + ") OK: " + value);
			return value;
		}
	}

	// Query the price of an item.
	protected int queryPrice(int id, String key) {
		Trace.info("RM::queryPrice(" + id + ", " + key + ") called.");
		synchronized(syncLock){
			ReservableItem curObj = (ReservableItem) readData(id, key);
			int value = 0;
			if (curObj != null) {
				value = curObj.getPrice();
			}
			Trace.info("RM::queryPrice(" + id + ", " + key + ") OK: $" + value);
			return value;
		}
	}

	// Reserve an item.

	private boolean reserveItem(int id, int customerId, String key,
			String location) {

		synchronized(syncLock){
			// Check if the item is available.
			ReservableItem item = (ReservableItem) readData(id, key);
			if (item == null) {
				Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " + key
						+ ", " + location + ") failed: item doesn't exist.");
				return false;
			} else if (item.getCount() == 0) {
				Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " + key
						+ ", " + location + ") failed: no more items.");
				return false;
			} else {
	
				// Decrease the number of available items in the storage.
				setCount(id,item,item.getCount() - 1);
				setReserved(id,item,item.getReserved() + 1);
	
				Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " + key
						+ ", " + location + ") OK.");
				return true;
			}
		}
	}

	// Cancel an item.

	private boolean cancelReserveItem(int id, int customerId, String key,
			String location) {

		synchronized(syncLock){
			// Check if the item is available.
			ReservableItem item = (ReservableItem) readData(id, key);
			if (item == null) {
				Trace.warn("RM::cancelReserveItem(" + id + ", " + customerId + ", "
						+ key + ", " + location + ") failed: item doesn't exist.");
				return false;
			} else if (item.getReserved() == 0) {
				Trace.warn("RM::cancelReserveItem(" + id + ", " + customerId + ", "
						+ key + ", " + location + ") failed: no reservations.");
				return false;
			} else {
	
				// Increase the number of available items in the storage.
				setCount(id,item,item.getCount() + 1);
				setReserved(id,item,item.getReserved() - 1);
	
				Trace.warn("RM::cancelReserveItem(" + id + ", " + customerId + ", "
						+ key + ", " + location + ") OK.");
				return true;
			}
		}
	}

	// Reserve an item.
	@Override
	public boolean reserveCustomer(int id, int customerId, String key,
			String location, int price) {
		synchronized(syncLock){
			Customer cust = (Customer) readData(id, Customer.getKey(customerId));
			if (cust == null) {
				Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " + key
						+ ", " + location + ") failed: customer doesn't exist.");
				return false;
			}
	
			else {
				// Do reservation.
				custReserve(id,cust, key,location, price);
				writeData(id, cust.getKey(), cust);
	
				Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " + key
						+ ", " + location + ") OK.");
				return true;
			}
		}
	}

	@Override
	public boolean cancelReserveCustomer(int id, int customerId, String key,
			String location, int price) {
		return false;
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
		synchronized(syncLock){
			Flight curObj = (Flight) readData(id, Flight.getKey(flightNumber));
			if (curObj == null) {
				// Doesn't exist; add it.
				Flight newObj = new Flight(flightNumber, numSeats, flightPrice);
				writeData(id, newObj.getKey(), newObj);
				Trace.info("RM::addFlight(" + id + ", " + flightNumber + ", $"
						+ flightPrice + ", " + numSeats + ") OK.");
			} else {
				// Add seats to existing flight and update the price.
				setCount(id,curObj,curObj.getCount() + numSeats);
				if (flightPrice > 0) {
					setPrice(id,curObj,flightPrice);
				}
				writeData(id, curObj.getKey(), curObj);
				Trace.info("RM::addFlight(" + id + ", " + flightNumber + ", $"
						+ flightPrice + ", " + numSeats + ") OK: " + "seats = "
						+ curObj.getCount() + ", price = $" + flightPrice);
			}
			return (true);
		}
	}

	@Override
	public boolean deleteFlight(int id, int flightNumber) {
		return deleteItem(id, Flight.getKey(flightNumber));
	}

	// Returns the number of empty seats on this flight.
	@Override
	public int queryFlight(int id, int flightNumber) {
		return queryNum(id, Flight.getKey(flightNumber));
	}

	// Returns price of this flight.
	public int queryFlightPrice(int id, int flightNumber) {
		return queryPrice(id, Flight.getKey(flightNumber));
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
		synchronized(syncLock){
		Car curObj = (Car) readData(id, Car.getKey(location));
			if (curObj == null) {
				// Doesn't exist; add it.
				Car newObj = new Car(location, numCars, carPrice);
				writeData(id, newObj.getKey(), newObj);
				Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars
						+ ", $" + carPrice + ") OK.");
			} else {
				// Add count to existing object and update price.
				setCount(id,curObj,curObj.getCount() + numCars);
				if (carPrice > 0) {
					setPrice(id,curObj,carPrice);
				}
				writeData(id, curObj.getKey(), curObj);
				Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars
						+ ", $" + carPrice + ") OK: " + "cars = "
						+ curObj.getCount() + ", price = $" + carPrice);
			}
			return (true);
		}
	}

	// Delete cars from a location.
	@Override
	public boolean deleteCars(int id, String location) {
		return deleteItem(id, Car.getKey(location));
	}

	// Returns the number of cars available at a location.
	@Override
	public int queryCars(int id, String location) {
		return queryNum(id, Car.getKey(location));
	}

	// Returns price of cars at this location.
	@Override
	public int queryCarsPrice(int id, String location) {
		return queryPrice(id, Car.getKey(location));
	}

	// Room operations //

	// Create a new room location or add rooms to an existing location.
	// Note: if price <= 0 and the room location already exists, it maintains
	// its current price.
	@Override
	public boolean addRooms(int id, String location, int numRooms, int roomPrice) {
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms
				+ ", $" + roomPrice + ") called.");
		synchronized(syncLock){
			Room curObj = (Room) readData(id, Room.getKey(location));
			if (curObj == null) {
				// Doesn't exist; add it.
				Room newObj = new Room(location, numRooms, roomPrice);
				writeData(id, newObj.getKey(), newObj);
				Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms
						+ ", $" + roomPrice + ") OK.");
			} else {
				// Add count to existing object and update price.
				setCount(id,curObj,curObj.getCount() + numRooms);
				if (roomPrice > 0) {
					setPrice(id,curObj,roomPrice);
				}
				writeData(id, curObj.getKey(), curObj);
				Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms
						+ ", $" + roomPrice + ") OK: " + "rooms = "
						+ curObj.getCount() + ", price = $" + roomPrice);
			}
			return (true);
		}
	}

	// Delete rooms from a location.
	@Override
	public boolean deleteRooms(int id, String location) {
		return deleteItem(id, Room.getKey(location));
	}

	// Returns the number of rooms available at a location.
	@Override
	public int queryRooms(int id, String location) {
		return queryNum(id, Room.getKey(location));
	}

	// Returns room price at this location.
	@Override
	public int queryRoomsPrice(int id, String location) {
		return queryPrice(id, Room.getKey(location));
	}

	// Customer operations //

	@Override
	public int newCustomer(int id) {
		Trace.info("INFO: RM::newCustomer(" + id + ") called.");
		// Generate a globally unique Id for the new customer.
		int customerId = Integer.parseInt(String.valueOf(id)
				+ String.valueOf(Calendar.getInstance().get(
						Calendar.MILLISECOND))
				+ String.valueOf(Math.round(Math.random() * 100 + 1)));
		Customer cust = new Customer(customerId);
		writeData(id, cust.getKey(), cust);
		Trace.info("RM::newCustomer(" + id + ") OK: " + customerId);
		return customerId;
	}

	// This method makes testing easier.
	@Override
	public boolean newCustomerId(int id, int customerId) {
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId
				+ ") called.");
		Customer cust = (Customer) readData(id, Customer.getKey(customerId));
		if (cust == null) {
			cust = new Customer(customerId);
			writeData(id, cust.getKey(), cust);
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId
					+ ") OK.");
			return true;
		} else {
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerId
					+ ") failed: customer already exists.");
			return false;
		}
	}

	// Delete customer from the database.
	@Override
	public boolean deleteCustomer(int id, int customerId) {
		Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") called.");
		synchronized(syncLock){
			Customer cust = (Customer) readData(id, Customer.getKey(customerId));
				removeData(id, cust.getKey());
				Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") OK.");
				return true;
		}
//		}
	}

	// Return data structure containing customer reservation info.
	// Returns null if the customer doesn't exist.
	// Returns empty RMHashtable if customer exists but has no reservations.
	public String getCustomerReservations(int id, int customerId) {
		Trace.info("RM::getCustomerReservations(" + id + ", " + customerId
				+ ") called.");
		synchronized(syncLock){
			Customer cust = (Customer) readData(id, Customer.getKey(customerId));
			if (cust == null) {
				Trace.info("RM::getCustomerReservations(" + id + ", " + customerId
						+ ") failed: customer doesn't exist.");
				return null;
			} else {
				return cust.printBill();
			}
		}
	}

	// Return a bill.
	@Override
	public String queryCustomerInfo(int id, int customerId) {
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId
				+ ") called.");
		synchronized(syncLock){
			Customer cust = (Customer) readData(id, Customer.getKey(customerId));
			if (cust == null) {
				Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerId
						+ ") failed: customer doesn't exist.");
				// Returning an empty bill means that the customer doesn't exist.
				return "";
			} else {
				String s = cust.printBill();
				Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId
						+ "): \n");
				System.out.println(s);
				return s;
			}
		}
	}

	// Add flight reservation to this customer.
	@Override
	public boolean reserveFlight(int id, int customerId, int flightNumber) {
		return reserveItem(id, customerId, Flight.getKey(flightNumber),
				String.valueOf(flightNumber));
	}

	// Add car reservation to this customer.
	@Override
	public boolean reserveCar(int id, int customerId, String location) {
		return reserveItem(id, customerId, Car.getKey(location), location);
	}

	// Add room reservation to this customer.
	@Override
	public boolean reserveRoom(int id, int customerId, String location) {
		return reserveItem(id, customerId, Room.getKey(location), location);
	}
	
	@Override
	public boolean cancelReserveFlight(int id, int customerId, int flightNumber) {
		return cancelReserveItem(id, customerId, Flight.getKey(flightNumber),
				String.valueOf(flightNumber));
	}

	// Add car reservation to this customer.
	@Override
	public boolean cancelReserveCar(int id, int customerId, String location) {
		return cancelReserveItem(id, customerId, Car.getKey(location), location);
	}

	// Add room reservation to this customer.
	@Override
	public boolean cancelReserveRoom(int id, int customerId, String location) {
		return cancelReserveItem(id, customerId, Room.getKey(location), location);
	}

	// Reserve an itinerary.
	@Override
	public boolean reserveItinerary(int id, int customerId,
			Vector flightNumbers, String location, boolean car, boolean room) {
		return false;
	}

	@Override
	public boolean requestVote(int id) throws CrashException {

		//TODO  before record
		if(crashLocation == 3){
			System.out.println(name + " crashes before returning vote");
			crash.crash("RM crashes before returning vote");
		}
		//If(!tm.hasTransaction(id) return false;
		

        	// wait for commit
        boolean canCommit = tmServer.prepareCommit(id);
        if (!canCommit)
            tmServer.abortTxn(id);
        
        // TODO  after sending answer...
        // start a new thread, wait half a second, then thread exits entire program
        if(crashLocation == 4){
	       (new Thread(){
	    	   public void run(){
	    		   try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		   try {
					crash.crash("RM crashes after sending vote response");
				} catch (CrashException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	   };
	       }).start();
        }
        
        System.out.println(name + ": votes: " + canCommit);
        return canCommit;
	}
	
	@Override
	public void setCrashLocation(int location){
		crashLocation = location;
	}
	
	@Override
	public void setCrashType(boolean isTest){
		if(isTest){
			crash = new TestRMCrash();
	
		} else {
			crash = new WSCrash();
		}
	}
	
	public void setTimeout(int timeout){
		tmServer.setTimeout(timeout);
	}
	


}
