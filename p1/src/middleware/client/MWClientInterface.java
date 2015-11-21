package middleware.client;

import lockmanager.DeadlockException;

public interface MWClientInterface {

	public boolean abort(int id);
	
	public boolean start(int id);
	
	public boolean commit(int id);
	
	public int run(int id);
	
	public boolean addFlight(int id, int flightNumber, int numSeats,
			int flightPrice)  throws DeadlockException;
	
	public boolean deleteFlight(int id, int flightNumber) throws DeadlockException;
	
	public int queryFlight(int id, int flightNumber) throws DeadlockException;
	
	public int queryFlightPrice(int id, int flightNumber) throws DeadlockException;
	
	public boolean addCars(int id, String location, int numCars, int carPrice) throws DeadlockException;
	
	public boolean deleteCars(int id, String location) throws DeadlockException;
	
	public int queryCars(int id, String location) throws DeadlockException;
	
	public int queryCarsPrice(int id, String location) throws DeadlockException;
	
	public boolean addRooms(int id, String location, int numRooms, int roomPrice) throws DeadlockException;
	
	public boolean deleteRooms(int id, String location) throws DeadlockException;
	
	public int queryRooms(int id, String location) throws DeadlockException;
	
	public int queryRoomsPrice(int id, String location) throws DeadlockException;
	
	public int newCustomer(int id);
	
	public boolean newCustomerId(int id, int customerId);
	
	public String queryCustomerInfo(int id, int customerId) throws DeadlockException;
	
	public boolean reserveCustomer(int id, int customerId, String key,
			String location, int price) throws DeadlockException;
	
	public boolean reserveFlight(int id, int customerId, int flightNumber) throws DeadlockException;
	
	public boolean reserveCar(int id, int customerId, String location) throws DeadlockException;
	
	public boolean reserveRoom(int id, int customerId, String location) throws DeadlockException;
	
	public boolean cancelReserveCar(int id, int customerId, String location) throws DeadlockException;
	
	public boolean cancelReserveRoom(int id, int customerId, String location) throws DeadlockException;
	
	public boolean cancelReserveFlight(int id, int customerId, int flightNumber) throws DeadlockException;
	
	public boolean deleteCustomer(int id, int customerId) throws DeadlockException;
	
	public boolean requestVote(int id);
	
}
