package middleware.client;

import java.util.*;
import java.io.*;


import lockmanager.DeadlockException;
import lockmanager.LockManager;
import lockmanager.TrxnObj;
import middleware.tm.TMClient;
import server.*;

public class MWClient extends WSClient {
	private LockManager lockManager;

	public MWClient(String serviceName, String serviceHost, int servicePort, LockManager lockManager)
			throws Exception {
		super(serviceName, serviceHost, servicePort);
		this.lockManager = lockManager;
	}

	
	public boolean abort(int id){
		boolean result = false;
		try {
			result = proxy.abort(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean start(int id){
		boolean result = false;
		try {
			result = proxy.start(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean commit(int id){
		boolean result = false;
		try {
			result = proxy.commit(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean shutdown(){
		boolean result = false;
		try {
			result = proxy.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	public int run(int id) {
		int customerID = 0;
		try {

			customerID = proxy.newCustomer(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerID;
	}

	public boolean addFlight(int id, int flightNumber, int numSeats,
			int flightPrice) throws DeadlockException {
		boolean flightStatus = false;
		TMClient.getInstance().enlistFlightRM(id);
		try {
			lockManager.Lock(id, Flight.getKey(flightNumber), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			flightStatus = proxy.addFlight(id, flightNumber, numSeats,
					flightPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public boolean deleteFlight(int id, int flightNumber) throws DeadlockException {
		boolean flightStatus = false;
		TMClient.getInstance().enlistFlightRM(id);
		try {
			lockManager.Lock(id, Flight.getKey(flightNumber), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			flightStatus = proxy.deleteFlight(id, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public int queryFlight(int id, int flightNumber) throws DeadlockException {
		int flightStatus = 0;
		TMClient.getInstance().enlistFlightRM(id);
		try {
			lockManager.Lock(id, Flight.getKey(flightNumber), TrxnObj.READ);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			flightStatus = proxy.queryFlight(id, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public int queryFlightPrice(int id, int flightNumber) throws DeadlockException {
		int flightStatus = 0;
		TMClient.getInstance().enlistFlightRM(id);
		try {
			lockManager.Lock(id, Flight.getKey(flightNumber), TrxnObj.READ);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			flightStatus = proxy.queryFlightPrice(id, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public boolean addCars(int id, String location, int numCars, int carPrice) throws DeadlockException {
		boolean carStatus = false;
		TMClient.getInstance().enlistCarRM(id);
		try {
			lockManager.Lock(id, Car.getKey(location), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			carStatus = proxy.addCars(id, location, numCars, carPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public boolean deleteCars(int id, String location) throws DeadlockException {
		boolean carStatus = false;
		TMClient.getInstance().enlistCarRM(id);
		try {
			lockManager.Lock(id, Car.getKey(location), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			carStatus = proxy.deleteCars(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public int queryCars(int id, String location) throws DeadlockException {
		int carStatus = 0;
		TMClient.getInstance().enlistCarRM(id);
		try {
			lockManager.Lock(id, Car.getKey(location), TrxnObj.READ);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			carStatus = proxy.queryCars(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public int queryCarsPrice(int id, String location) throws DeadlockException {
		int carStatus = 0;
		TMClient.getInstance().enlistCarRM(id);
		try {
			lockManager.Lock(id, Car.getKey(location), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			carStatus = proxy.queryCarsPrice(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public boolean addRooms(int id, String location, int numRooms, int roomPrice) throws DeadlockException {
		boolean roomStatus = false;
		TMClient.getInstance().enlistRoomRM(id);
		try {
			lockManager.Lock(id, Room.getKey(location), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			roomStatus = proxy.addRooms(id, location, numRooms, roomPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public boolean deleteRooms(int id, String location) throws DeadlockException {
		boolean roomStatus = false;
		TMClient.getInstance().enlistRoomRM(id);
		try {
			lockManager.Lock(id, Room.getKey(location), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			roomStatus = proxy.deleteRooms(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public int queryRooms(int id, String location) throws DeadlockException {
		int roomStatus = 0;
		TMClient.getInstance().enlistRoomRM(id);
		try {
			lockManager.Lock(id, Room.getKey(location), TrxnObj.READ);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			roomStatus = proxy.queryRooms(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public int queryRoomsPrice(int id, String location) throws DeadlockException {
		int roomStatus = 0;
		TMClient.getInstance().enlistRoomRM(id);
		try {
			lockManager.Lock(id, Room.getKey(location), TrxnObj.READ);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			roomStatus = proxy.queryRoomsPrice(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public int newCustomer(int id) {
		int customerID = 0;
		TMClient.getInstance().enlistCustomerRM(id);
		try {
			customerID = proxy.newCustomer(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerID;
	}

	public boolean newCustomerId(int id, int customerId) {
		boolean customerStatus = false;
		try {
			TMClient.getInstance().enlistCustomerRM(id);
			customerStatus = proxy.newCustomerId(id, customerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerStatus;
	}

	public String queryCustomerInfo(int id, int customerId) throws DeadlockException {
		String customerStatus = "";
		TMClient.getInstance().enlistCustomerRM(id);
		try {
			lockManager.Lock(id, Customer.getKey(customerId), TrxnObj.READ);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			customerStatus = proxy.queryCustomerInfo(id, customerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerStatus;
	}

	public boolean reserveCustomer(int id, int customerId, String key,
			String location, int price) throws DeadlockException {
		boolean customerStatus = false;
		TMClient.getInstance().enlistCustomerRM(id);
		try {
			lockManager.Lock(id, Customer.getKey(customerId), TrxnObj.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
			TMClient.getInstance().abort(id);
			throw e;
		}
		try {
			customerStatus = proxy.reserveCustomer(id, customerId, key,
					location, price);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerStatus;
	}




	
	// Add flight reservation to this customer.
	public boolean reserveFlight(int id, int customerId, int flightNumber) throws DeadlockException {
		boolean itemStatus = false;
		TMClient.getInstance().enlistFlightRM(id);
		lockManager.Lock(id, Flight.getKey(flightNumber), TrxnObj.WRITE);
		try {
			itemStatus = proxy.reserveFlight(id, customerId, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
	}

	// Add car reservation to this customer.
	public boolean reserveCar(int id, int customerId, String location) throws DeadlockException {
		boolean itemStatus = false;
		TMClient.getInstance().enlistCarRM(id);
		lockManager.Lock(id, Car.getKey(location), TrxnObj.WRITE);
		try {
			itemStatus = proxy.reserveCar(id, customerId, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
	}

	// Add room reservation to this customer.
	public boolean reserveRoom(int id, int customerId, String location) throws DeadlockException {
		boolean itemStatus = false;
		TMClient.getInstance().enlistRoomRM(id);
		lockManager.Lock(id, Room.getKey(location), TrxnObj.WRITE);
		try {
			itemStatus = proxy.reserveRoom(id, customerId, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
	}

	public boolean cancelReserveCar(int id, int customerId, String location) throws DeadlockException {
		boolean itemStatus = false;
		TMClient.getInstance().enlistCarRM(id);
		lockManager.Lock(id, Car.getKey(location), TrxnObj.WRITE);
		try {
			itemStatus = proxy.cancelReserveCar(id, customerId, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
		
	}

	public boolean cancelReserveRoom(int id, int customerId, String location) throws DeadlockException {
		boolean itemStatus = false;
		TMClient.getInstance().enlistRoomRM(id);
		lockManager.Lock(id, Room.getKey(location), TrxnObj.WRITE);
		try {
			itemStatus = proxy.cancelReserveRoom(id, customerId, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
		
	}

	public boolean cancelReserveFlight(int id, int customerId, int flightNumber) throws DeadlockException {
		boolean itemStatus = false;
		TMClient.getInstance().enlistFlightRM(id);
		lockManager.Lock(id, Flight.getKey(flightNumber), TrxnObj.WRITE);
		try {
			itemStatus = proxy.cancelReserveFlight(id, customerId, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
		
	}

	public boolean deleteCustomer(int id, int customerId) throws DeadlockException {
		boolean itemStatus = false;
		TMClient.getInstance().enlistCustomerRM(id);
		lockManager.Lock(id, Customer.getKey(customerId), TrxnObj.WRITE);
		try {
			itemStatus = proxy.deleteCustomer(id, customerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
		
	}


}