package middleware.client;

import java.util.*;
import java.io.*;

public class MWClient extends WSClient {

	public MWClient(String serviceName, String serviceHost, int servicePort)
			throws Exception {
		super(serviceName, serviceHost, servicePort);
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
			int flightPrice) {
		boolean flightStatus = false;
		try {
			flightStatus = proxy.addFlight(id, flightNumber, numSeats,
					flightPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public boolean deleteFlight(int id, int flightNumber) {
		boolean flightStatus = false;
		try {
			flightStatus = proxy.deleteFlight(id, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public int queryFlight(int id, int flightNumber) {
		int flightStatus = 0;
		try {
			flightStatus = proxy.queryFlight(id, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public int queryFlightPrice(int id, int flightNumber) {
		int flightStatus = 0;
		try {
			flightStatus = proxy.queryFlightPrice(id, flightNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flightStatus;
	}

	public boolean addCars(int id, String location, int numCars, int carPrice) {
		boolean carStatus = false;
		try {
			carStatus = proxy.addCars(id, location, numCars, carPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public boolean deleteCars(int id, String location) {
		boolean carStatus = false;
		try {
			carStatus = proxy.deleteCars(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public int queryCars(int id, String location) {
		int carStatus = 0;
		try {
			carStatus = proxy.queryCars(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public int queryCarsPrice(int id, String location) {
		int carStatus = 0;
		try {
			carStatus = proxy.queryCarsPrice(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return carStatus;
	}

	public boolean addRooms(int id, String location, int numRooms, int roomPrice) {
		boolean roomStatus = false;
		try {
			roomStatus = proxy.addRooms(id, location, numRooms, roomPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public boolean deleteRooms(int id, String location) {
		boolean roomStatus = false;
		try {
			roomStatus = proxy.deleteRooms(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public int queryRooms(int id, String location) {
		int roomStatus = 0;
		try {
			roomStatus = proxy.queryRooms(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public int queryRoomsPrice(int id, String location) {
		int roomStatus = 0;
		try {
			roomStatus = proxy.queryRoomsPrice(id, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roomStatus;
	}

	public int newCustomer(int id) {
		int customerID = 0;
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
			customerStatus = proxy.newCustomerId(id, customerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerStatus;
	}

	public String queryCustomerInfo(int id, int customerId) {
		String customerStatus = "";
		try {
			customerStatus = proxy.queryCustomerInfo(id, customerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerStatus;
	}

	public boolean reserveCustomer(int id, int customerId, String key,
			String location, int price) {
		boolean customerStatus = false;
		try {
			customerStatus = proxy.reserveCustomer(id, customerId, key,
					location, price);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return customerStatus;
	}

	public boolean reserveItem(int id, int customerId, String key,
			String location) {
		boolean itemStatus = false;
		try {
			itemStatus = proxy.reserveItem(id, customerId, key, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
	}

	public boolean cancelReserveItem(int id, int customerId, String key,
			String location) {
		boolean itemStatus = false;
		try {
			itemStatus = proxy.cancelReserveItem(id, customerId, key, location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemStatus;
	}
}