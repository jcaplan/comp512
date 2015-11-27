/** 
 * Simplified version from CSE 593, University of Washington.
 *
 * A Distributed System in Java using Web Services.
 * 
 * Failures should be reported via the return value.  For example, 
 * if an operation fails, you should return either false (boolean), 
 * or some error code like -1 (int).
 *
 * If there is a boolean return value and you're not sure how it 
 * would be used in your implementation, ignore it.  I used boolean
 * return values in the interface generously to allow flexibility in 
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 */

package middleware.server;

import lockmanager.DeadlockException;

import java.util.*;

import javax.jws.WebService;
import javax.jws.WebMethod;

import crash.CrashException;




@WebService
public interface ResourceManager {
    
    // Flight operations //
    
    /* Add seats to a flight.  
     * In general, this will be used to create a new flight, but it should be 
     * possible to add seats to an existing flight.  Adding to an existing 
     * flight should overwrite the current price of the available seats.
     *
     * @return success.
     */
    @WebMethod
    public boolean addFlight(int id, int flightNumber, int numSeats, int flightPrice) throws DeadlockException;

    /**
     * Delete the entire flight.
     * This implies deletion of this flight and all its seats.  If there is a 
     * reservation on the flight, then the flight cannot be deleted.
     *
     * @return success.
     */   
    @WebMethod
    public boolean deleteFlight(int id, int flightNumber) throws DeadlockException;

    /* Return the number of empty seats in this flight. */
    @WebMethod
    public int queryFlight(int id, int flightNumber) throws DeadlockException;

    /* Return the price of a seat on this flight. */
    @WebMethod
    public int queryFlightPrice(int id, int flightNumber) throws DeadlockException;


    // Car operations //

    /* Add cars to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    @WebMethod
    public boolean addCars(int id, String location, int numCars, int carPrice) throws DeadlockException;
    
    /* Delete all cars from a location.
     * It should not succeed if there are reservations for this location.
     */		    
    @WebMethod
    public boolean deleteCars(int id, String location) throws DeadlockException;

    /* Return the number of cars available at this location. */
    @WebMethod
    public int queryCars(int id, String location) throws DeadlockException;

    /* Return the price of a car at this location. */
    @WebMethod
    public int queryCarsPrice(int id, String location) throws DeadlockException;


    // Room operations //
    
    /* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    @WebMethod
    public boolean addRooms(int id, String location, int numRooms, int roomPrice) throws DeadlockException;

    /* Delete all rooms from a location.
     * It should not succeed if there are reservations for this location.
     */
    @WebMethod
    public boolean deleteRooms(int id, String location) throws DeadlockException;

    /* Return the number of rooms available at this location. */
    @WebMethod
    public int queryRooms(int id, String location) throws DeadlockException;

    /* Return the price of a room at this location. */
    @WebMethod
    public int queryRoomsPrice(int id, String location) throws DeadlockException;


    // Customer operations //
        
    /* Create a new customer and return their unique identifier. */
    @WebMethod
    public int newCustomer(int id); 
    
    /* Create a new customer with the provided identifier. */
    @WebMethod
    public boolean newCustomerId(int id, int customerId);

    /* Remove this customer and all their associated reservations. */
    @WebMethod
    public boolean deleteCustomer(int id, int customerId) throws DeadlockException;

    /* Return a bill. */
    @WebMethod
    public String queryCustomerInfo(int id, int customerId) throws DeadlockException;

    /* Reserve a seat on this flight. */
    @WebMethod
    public boolean reserveFlight(int id, int customerId, int flightNumber) throws DeadlockException;

    /* Reserve a car at this location. */
    @WebMethod
    public boolean reserveCar(int id, int customerId, String location) throws DeadlockException;

    /* Reserve a room at this location. */
    @WebMethod
    public boolean reserveRoom(int id, int customerId, String location) throws DeadlockException;


    /* Reserve an itinerary. */
    @WebMethod
    public boolean reserveItinerary(int id, int customerId, Vector flightNumbers, 
                                    String location, boolean car, boolean room) throws DeadlockException;
    
    @WebMethod 
    public boolean reserveCustomer(int id, int customerId, String key,
			String location, int price);
    
    
    @WebMethod 
    public boolean cancelReserveCustomer(int id, int customerId, String key,
			String location, int price);

    @WebMethod
	boolean cancelReserveFlight(int id, int customerId, int flightNumber);

    @WebMethod
	boolean cancelReserveCar(int id, int customerId, String location);

    @WebMethod
	boolean cancelReserveRoom(int id, int customerId, String location);

    
    @WebMethod
    public int start(); //MW to RM
    
    @WebMethod
    public boolean commit(int id) throws CrashException;
    
    @WebMethod 
    public boolean abort(int id);
    
    @WebMethod 
    public boolean shutdown();
    			
}
