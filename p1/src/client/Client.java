package client;

import lockmanager.DeadlockException;

import java.util.*;
import java.io.*;
public class Client extends client.WSClient {

	private Vector results = new Vector();

	public Object getResult(int index) {
		return results.get(index);
	}

	public Client(String serviceName, String serviceHost, int servicePort)
			throws Exception {
		super(serviceName, serviceHost, servicePort);
	}

	public static void main(String[] args) {
		try {

			if (args.length != 3) {
				System.out.println("Usage: MyClient <service-name> "
						+ "<service-host> <service-port>");
				System.exit(-1);
			}

			String serviceName = args[0];
			String serviceHost = args[1];
			int servicePort = Integer.parseInt(args[2]);

			Client client = new Client(serviceName, serviceHost, servicePort);

			 client.run();

			// set up customers
//			client.handleRequest("newcustomer,0");
//			client.handleRequest("newcustomer,1");
//
//			int client0 = (Integer) client.getResult(0);
//			int client1 = (Integer) client.getResult(1);
//			System.out.println("new clients: " + client0 + "," + client1);
//			// set up a car
//			client.handleRequest("newcar,0,0,100,100");
//
//			// try an itinerary that won't work then check car
//			// a few times
//			// for(int i = 0; i < 5; i++){
//			// client.handleRequest("itinerary,0,"+client0 +",0,0,0,0");
//			// client.handleRequest("querycar,0,0");
//			// }
//
//			client.handleRequest("newflight,0,0,200,200");
//			client.handleRequest("newroom,0,0,300,300");
//
//			client.handleRequest("itinerary,0," + client1 + ",0,0,true,true");
//
//			client.handleRequest("querycar,0,0");
//			client.handleRequest("queryflight,0,0");
//			client.handleRequest("queryroom,0,0");
//			client.handleRequest("queryCustomer,0," + client1);
//
//			client.handleRequest("deleteCustomer,0," + client1);
//			client.handleRequest("deleteCustomer,1," + client0);
//
//			client.handleRequest("queryCustomer,0," + client1);
//			client.handleRequest("queryCustomer,0," + client0);
//			client.handleRequest("querycar,0,0");
//			client.handleRequest("queryflight,0,0");
//			client.handleRequest("queryroom,0,0");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws DeadlockException_Exception, DeadlockException {

		System.out.println("Client Interface");
		System.out.println("Type \"help\" for list of supported commands");

		while (true) {
			String command = "";
			Vector arguments = new Vector();

			BufferedReader stdin = new BufferedReader(new InputStreamReader(
					System.in));

			try {
				// read the next command
				command = stdin.readLine();
			} catch (IOException io) {
				System.out.println("Unable to read from standard in");
				System.exit(1);
			}
			// remove heading and trailing white space
			handleRequest(command);
		}
	}

	public String handleRequest(String command) throws DeadlockException_Exception{

		int id;
		int flightNumber;
		int flightPrice;
		int numSeats;
		boolean room;
		boolean car;
		int price;
		int numRooms;
		int numCars;
		String location;

		Vector arguments = new Vector();
		command = command.trim();
		arguments = parse(command);
		String resultString = "";

		// decide which of the commands this was
		switch (findChoice((String) arguments.elementAt(0))) {

		case 1: // help section
			if (arguments.size() == 1) // command was "help"
				listCommands();
			else if (arguments.size() == 2) // command was "help <commandname>"
				listSpecific((String) arguments.elementAt(1));
			else
				// wrong use of help command
				System.out
						.println("Improper use of help command. Type help or help, <commandname>");
			break;

		case 2: // new flight
			if (arguments.size() != 5) {
				wrongNumber();
				break;
			}
			System.out.println("Adding a new Flight using id: "
					+ arguments.elementAt(1));
			System.out.println("Flight number: " + arguments.elementAt(2));
			System.out.println("Add Flight Seats: " + arguments.elementAt(3));
			System.out.println("Set Flight Price: " + arguments.elementAt(4));

			try {
				id = getInt(arguments.elementAt(1));
				flightNumber = getInt(arguments.elementAt(2));
				numSeats = getInt(arguments.elementAt(3));
				flightPrice = getInt(arguments.elementAt(4));
				boolean result = proxy.addFlight(id, flightNumber, numSeats,
						flightPrice);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("Flight added");
				else
					System.out.println("Flight could not be added");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 3: // new car
			if (arguments.size() != 5) {
				wrongNumber();
				break;
			}
			System.out.println("Adding a new car using id: "
					+ arguments.elementAt(1));
			System.out.println("car Location: " + arguments.elementAt(2));
			System.out.println("Add Number of cars: " + arguments.elementAt(3));
			System.out.println("Set Price: " + arguments.elementAt(4));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));
				numCars = getInt(arguments.elementAt(3));
				price = getInt(arguments.elementAt(4));

				Boolean result = proxy.addCars(id, location, numCars, price);
				resultString = result.toString();
				if (result)
					System.out.println("cars added");
				else
					System.out.println("cars could not be added");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 4: // new room
			if (arguments.size() != 5) {
				wrongNumber();
				break;
			}
			System.out.println("Adding a new room using id: "
					+ arguments.elementAt(1));
			System.out.println("room Location: " + arguments.elementAt(2));
			System.out
					.println("Add Number of rooms: " + arguments.elementAt(3));
			System.out.println("Set Price: " + arguments.elementAt(4));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));
				numRooms = getInt(arguments.elementAt(3));
				price = getInt(arguments.elementAt(4));
				boolean result = proxy.addRooms(id, location, numRooms, price);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("rooms added");
				else
					System.out.println("rooms could not be added");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 5: // new Customer
			if (arguments.size() != 2) {
				wrongNumber();
				break;
			}
			System.out.println("Adding a new Customer using id: "
					+ arguments.elementAt(1));
			try {
				id = getInt(arguments.elementAt(1));

				int customer = proxy.newCustomer(id);
				System.out.println("new customer id: " + customer);
				resultString = Integer.toString(customer);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 6: // delete Flight
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Deleting a flight using id: "
					+ arguments.elementAt(1));
			System.out.println("Flight Number: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				flightNumber = getInt(arguments.elementAt(2));
				boolean result = proxy.deleteFlight(id, flightNumber);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("Flight Deleted");
				else
					System.out.println("Flight could not be deleted");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 7: // delete car
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out
					.println("Deleting the cars from a particular location  using id: "
							+ arguments.elementAt(1));
			System.out.println("car Location: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));
				boolean result = proxy.deleteCars(id, location);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("cars Deleted");
				else
					System.out.println("cars could not be deleted");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 8: // delete room
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out
					.println("Deleting all rooms from a particular location  using id: "
							+ arguments.elementAt(1));
			System.out.println("room Location: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));
				boolean result = proxy.deleteRooms(id, location);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("rooms Deleted");
				else
					System.out.println("rooms could not be deleted");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 9: // delete Customer
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out
					.println("Deleting a customer from the database using id: "
							+ arguments.elementAt(1));
			System.out.println("Customer id: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				int customer = getInt(arguments.elementAt(2));
				boolean result = proxy.deleteCustomer(id, customer);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("Customer Deleted");
				else
					System.out.println("Customer could not be deleted");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 10: // querying a flight
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Querying a flight using id: "
					+ arguments.elementAt(1));
			System.out.println("Flight number: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				flightNumber = getInt(arguments.elementAt(2));
				int seats = proxy.queryFlight(id, flightNumber);
				System.out.println("Number of seats available: " + seats);
				resultString = Integer.toString(seats);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 11: // querying a car Location
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Querying a car location using id: "
					+ arguments.elementAt(1));
			System.out.println("car location: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));

				numCars = proxy.queryCars(id, location);
				System.out.println("number of cars at this location: "
						+ numCars);
				resultString = Integer.toString(numCars);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 12: // querying a room location
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Querying a room location using id: "
					+ arguments.elementAt(1));
			System.out.println("room location: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));

				numRooms = proxy.queryRooms(id, location);
				System.out.println("number of rooms at this location: "
						+ numRooms);
				resultString = Integer.toString(numRooms);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 13: // querying Customer Information
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Querying Customer information using id: "
					+ arguments.elementAt(1));
			System.out.println("Customer id: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				int customer = getInt(arguments.elementAt(2));

				String bill = proxy.queryCustomerInfo(id, customer);
				System.out.println("Customer info: " + bill);
				resultString = bill;
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 14: // querying a flight Price
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Querying a flight Price using id: "
					+ arguments.elementAt(1));
			System.out.println("Flight number: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				flightNumber = getInt(arguments.elementAt(2));

				price = proxy.queryFlightPrice(id, flightNumber);
				System.out.println("Price of a seat: " + price);
				resultString = Integer.toString(price);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 15: // querying a car Price
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Querying a car price using id: "
					+ arguments.elementAt(1));
			System.out.println("car location: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));

				price = proxy.queryCarsPrice(id, location);
				System.out.println("Price of a car at this location: " + price);
				resultString = Integer.toString(price);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 16: // querying a room price
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Querying a room price using id: "
					+ arguments.elementAt(1));
			System.out.println("room Location: " + arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				location = getString(arguments.elementAt(2));

				price = proxy.queryRoomsPrice(id, location);
				System.out.println("Price of rooms at this location: " + price);
				resultString = Integer.toString(price);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 17: // reserve a flight
			if (arguments.size() != 4) {
				wrongNumber();
				break;
			}
			System.out.println("Reserving a seat on a flight using id: "
					+ arguments.elementAt(1));
			System.out.println("Customer id: " + arguments.elementAt(2));
			System.out.println("Flight number: " + arguments.elementAt(3));
			try {
				id = getInt(arguments.elementAt(1));
				int customer = getInt(arguments.elementAt(2));
				flightNumber = getInt(arguments.elementAt(3));
				boolean result = proxy
						.reserveFlight(id, customer, flightNumber);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("Flight Reserved");
				else
					System.out.println("Flight could not be reserved.");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 18: // reserve a car
			if (arguments.size() != 4) {
				wrongNumber();
				break;
			}
			System.out.println("Reserving a car at a location using id: "
					+ arguments.elementAt(1));
			System.out.println("Customer id: " + arguments.elementAt(2));
			System.out.println("Location: " + arguments.elementAt(3));
			try {
				id = getInt(arguments.elementAt(1));
				int customer = getInt(arguments.elementAt(2));
				location = getString(arguments.elementAt(3));
				boolean result = proxy.reserveCar(id, customer, location);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("car Reserved");
				else
					System.out.println("car could not be reserved.");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 19: // reserve a room
			if (arguments.size() != 4) {
				wrongNumber();
				break;
			}
			System.out.println("Reserving a room at a location using id: "
					+ arguments.elementAt(1));
			System.out.println("Customer id: " + arguments.elementAt(2));
			System.out.println("Location: " + arguments.elementAt(3));
			try {
				id = getInt(arguments.elementAt(1));
				int customer = getInt(arguments.elementAt(2));
				location = getString(arguments.elementAt(3));
				boolean result = proxy.reserveRoom(id, customer, location);
				resultString = Boolean.toString(result);
				if (result)
					System.out.println("room Reserved");
				else
					System.out.println("room could not be reserved.");
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;

		case 20: // reserve an Itinerary
			if (arguments.size()<7) {
                wrongNumber();
                break;
            }
            System.out.println("Reserving an Itinerary using id: " + arguments.elementAt(1));
            System.out.println("Customer id: " + arguments.elementAt(2));
            for (int i = 0; i<arguments.size()-6; i++)
                System.out.println("Flight number" + arguments.elementAt(3 + i));
            System.out.println("Location for car/room booking: " + arguments.elementAt(arguments.size()-3));
            System.out.println("car to book?: " + arguments.elementAt(arguments.size()-2));
            System.out.println("room to book?: " + arguments.elementAt(arguments.size()-1));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));
                Vector flightNumbers = new Vector();
                for (int i = 0; i < arguments.size()-6; i++)
                    flightNumbers.addElement(arguments.elementAt(3 + i));
                location = getString(arguments.elementAt(arguments.size()-3));
                car = getBoolean(arguments.elementAt(arguments.size()-2));
                room = getBoolean(arguments.elementAt(arguments.size()-1));
                boolean result = proxy.reserveItinerary(id, customer, flightNumbers, 
                        location, car, room);
                resultString = Boolean.toString(result);
                if (result)
                    System.out.println("Itinerary Reserved");
                else
                    System.out.println("Itinerary could not be reserved.");
            }catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
            break;
                        

		case 21: // quit the client
			if (arguments.size() != 1) {
				wrongNumber();
				break;
			}
			System.out.println("Quitting client.");

		case 22: // new Customer given id
			if (arguments.size() != 3) {
				wrongNumber();
				break;
			}
			System.out.println("Adding a new Customer using id: "
					+ arguments.elementAt(1) + " and cid "
					+ arguments.elementAt(2));
			try {
				id = getInt(arguments.elementAt(1));
				int customer = getInt(arguments.elementAt(2));

				boolean c = proxy.newCustomerId(id, customer);
				resultString = Boolean.toString(c);
				System.out.println("new customer id: " + customer);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;
		case 23: // Start
			if(arguments.size() != 1){
				wrongNumber();
				break;
			}
			System.out.println("Starting new transaction");
			try{
				int txnId = proxy.start();
				resultString = Integer.toString(txnId);
				System.out.println("Txn ID: " + txnId);
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;
		case 24: // Commit
			if(arguments.size() != 2){
				wrongNumber();
				break;
			}
			System.out.println("Committing transaction");
			try{
				int txnId = getInt(arguments.elementAt(1));
				proxy.commit(txnId);
			}catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;
		case 25: // Abort
			if(arguments.size() != 2){
				wrongNumber();
				break;
			}
			System.out.println("Aborting transaction");
			try{
				int txnId = getInt(arguments.elementAt(1));
				proxy.abort(txnId);
			} catch(DeadlockException_Exception e) {
				throw e;
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;
		case 26: // Shutdown
			if(arguments.size() != 1){
				wrongNumber();
				break;
			}
			System.out.println("shutting down");
			try{
				proxy.shutdown();
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;
		case 27: // RM crash
			if(arguments.size() != 3){
				wrongNumber();
				break;
			}
			
			try{
				int rm = getInt(arguments.elementAt(1));
				int loc = getInt(arguments.elementAt(2));
				System.out.println("crashing RM");
				proxy.setCrashRM(rm,loc);
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;
		case 28: // TM crash
			if(arguments.size() != 2){
				wrongNumber();
				break;
			}
			
			try{
				int loc = getInt(arguments.elementAt(1));
				System.out.println("crashing TM");
				proxy.setCrashTM(loc);
			} catch (Exception e) {
				System.out.println("EXCEPTION: ");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}	
			break;
			
		default:
			System.out.println("The interface does not support this command.");
			break;
		}
		return resultString;
	}

	public Vector parse(String command) {
		Vector arguments = new Vector();
		StringTokenizer tokenizer = new StringTokenizer(command, ",");
		String argument = "";
		while (tokenizer.hasMoreTokens()) {
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		}
		return arguments;
	}

	public int findChoice(String argument) {
		if (argument.compareToIgnoreCase("help") == 0)
			return 1;
		else if (argument.compareToIgnoreCase("newflight") == 0)
			return 2;
		else if (argument.compareToIgnoreCase("newcar") == 0)
			return 3;
		else if (argument.compareToIgnoreCase("newroom") == 0)
			return 4;
		else if (argument.compareToIgnoreCase("newcustomer") == 0)
			return 5;
		else if (argument.compareToIgnoreCase("deleteflight") == 0)
			return 6;
		else if (argument.compareToIgnoreCase("deletecar") == 0)
			return 7;
		else if (argument.compareToIgnoreCase("deleteroom") == 0)
			return 8;
		else if (argument.compareToIgnoreCase("deletecustomer") == 0)
			return 9;
		else if (argument.compareToIgnoreCase("queryflight") == 0)
			return 10;
		else if (argument.compareToIgnoreCase("querycar") == 0)
			return 11;
		else if (argument.compareToIgnoreCase("queryroom") == 0)
			return 12;
		else if (argument.compareToIgnoreCase("querycustomer") == 0)
			return 13;
		else if (argument.compareToIgnoreCase("queryflightprice") == 0)
			return 14;
		else if (argument.compareToIgnoreCase("querycarprice") == 0)
			return 15;
		else if (argument.compareToIgnoreCase("queryroomprice") == 0)
			return 16;
		else if (argument.compareToIgnoreCase("reserveflight") == 0)
			return 17;
		else if (argument.compareToIgnoreCase("reservecar") == 0)
			return 18;
		else if (argument.compareToIgnoreCase("reserveroom") == 0)
			return 19;
		else if (argument.compareToIgnoreCase("itinerary") == 0)
			return 20;
		else if (argument.compareToIgnoreCase("quit") == 0)
			return 21;
		else if (argument.compareToIgnoreCase("newcustomerid") == 0)
			return 22;
		else if (argument.compareToIgnoreCase("start") == 0)
			return 23;
		else if (argument.compareToIgnoreCase("commit") == 0)
			return 24;
		else if (argument.compareToIgnoreCase("abort") == 0)
			return 25;
		else if (argument.compareToIgnoreCase("shutdown") == 0)
			return 26;
		else if (argument.compareToIgnoreCase("crashRM") == 0){
			return 27;
		} else if (argument.compareToIgnoreCase("crashTM") == 0){
			return 28;
		}
		
		else
			return 666;
	}

	public void listCommands() {
		System.out
				.println("\nWelcome to the client interface provided to test your project.");
		System.out.println("Commands accepted by the interface are: ");
		System.out.println("help");
		System.out
				.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcustomerid\ndeleteflight\ndeletecar\ndeleteroom");
		System.out
				.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
		System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
		System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
		System.out.println("quit");
		System.out
				.println("\ntype help, <commandname> for detailed info (note the use of comma).");
	}

	public void listSpecific(String command) {
		System.out.print("Help on: ");
		switch (findChoice(command)) {
		case 1:
			System.out.println("Help");
			System.out
					.println("\nTyping help on the prompt gives a list of all the commands available.");
			System.out
					.println("Typing help, <commandname> gives details on how to use the particular command.");
			break;

		case 2: // new flight
			System.out.println("Adding a new Flight.");
			System.out.println("Purpose: ");
			System.out.println("\tAdd information about a new flight.");
			System.out.println("\nUsage: ");
			System.out
					.println("\tnewflight, <id>, <flightnumber>, <numSeats>, <flightprice>");
			break;

		case 3: // new car
			System.out.println("Adding a new car.");
			System.out.println("Purpose: ");
			System.out.println("\tAdd information about a new car location.");
			System.out.println("\nUsage: ");
			System.out
					.println("\tnewcar, <id>, <location>, <numberofcars>, <pricepercar>");
			break;

		case 4: // new room
			System.out.println("Adding a new room.");
			System.out.println("Purpose: ");
			System.out.println("\tAdd information about a new room location.");
			System.out.println("\nUsage: ");
			System.out
					.println("\tnewroom, <id>, <location>, <numberofrooms>, <priceperroom>");
			break;

		case 5: // new Customer
			System.out.println("Adding a new Customer.");
			System.out.println("Purpose: ");
			System.out
					.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
			System.out.println("\nUsage: ");
			System.out.println("\tnewcustomer, <id>");
			break;

		case 6: // delete Flight
			System.out.println("Deleting a flight");
			System.out.println("Purpose: ");
			System.out.println("\tDelete a flight's information.");
			System.out.println("\nUsage: ");
			System.out.println("\tdeleteflight, <id>, <flightnumber>");
			break;

		case 7: // delete car
			System.out.println("Deleting a car");
			System.out.println("Purpose: ");
			System.out.println("\tDelete all cars from a location.");
			System.out.println("\nUsage: ");
			System.out.println("\tdeletecar, <id>, <location>, <numCars>");
			break;

		case 8: // delete room
			System.out.println("Deleting a room");
			System.out.println("\nPurpose: ");
			System.out.println("\tDelete all rooms from a location.");
			System.out.println("Usage: ");
			System.out.println("\tdeleteroom, <id>, <location>, <numRooms>");
			break;

		case 9: // delete Customer
			System.out.println("Deleting a Customer");
			System.out.println("Purpose: ");
			System.out.println("\tRemove a customer from the database.");
			System.out.println("\nUsage: ");
			System.out.println("\tdeletecustomer, <id>, <customerid>");
			break;

		case 10: // querying a flight
			System.out.println("Querying flight.");
			System.out.println("Purpose: ");
			System.out
					.println("\tObtain Seat information about a certain flight.");
			System.out.println("\nUsage: ");
			System.out.println("\tqueryflight, <id>, <flightnumber>");
			break;

		case 11: // querying a car Location
			System.out.println("Querying a car location.");
			System.out.println("Purpose: ");
			System.out
					.println("\tObtain number of cars at a certain car location.");
			System.out.println("\nUsage: ");
			System.out.println("\tquerycar, <id>, <location>");
			break;

		case 12: // querying a room location
			System.out.println("Querying a room Location.");
			System.out.println("Purpose: ");
			System.out
					.println("\tObtain number of rooms at a certain room location.");
			System.out.println("\nUsage: ");
			System.out.println("\tqueryroom, <id>, <location>");
			break;

		case 13: // querying Customer Information
			System.out.println("Querying Customer Information.");
			System.out.println("Purpose: ");
			System.out.println("\tObtain information about a customer.");
			System.out.println("\nUsage: ");
			System.out.println("\tquerycustomer, <id>, <customerid>");
			break;

		case 14: // querying a flight for price
			System.out.println("Querying flight.");
			System.out.println("Purpose: ");
			System.out
					.println("\tObtain price information about a certain flight.");
			System.out.println("\nUsage: ");
			System.out.println("\tqueryflightprice, <id>, <flightnumber>");
			break;

		case 15: // querying a car Location for price
			System.out.println("Querying a car location.");
			System.out.println("Purpose: ");
			System.out
					.println("\tObtain price information about a certain car location.");
			System.out.println("\nUsage: ");
			System.out.println("\tquerycarprice, <id>, <location>");
			break;

		case 16: // querying a room location for price
			System.out.println("Querying a room Location.");
			System.out.println("Purpose: ");
			System.out
					.println("\tObtain price information about a certain room location.");
			System.out.println("\nUsage: ");
			System.out.println("\tqueryroomprice, <id>, <location>");
			break;

		case 17: // reserve a flight
			System.out.println("Reserving a flight.");
			System.out.println("Purpose: ");
			System.out.println("\tReserve a flight for a customer.");
			System.out.println("\nUsage: ");
			System.out
					.println("\treserveflight, <id>, <customerid>, <flightnumber>");
			break;

		case 18: // reserve a car
			System.out.println("Reserving a car.");
			System.out.println("Purpose: ");
			System.out
					.println("\tReserve a given number of cars for a customer at a particular location.");
			System.out.println("\nUsage: ");
			System.out
					.println("\treservecar, <id>, <customerid>, <location>, <nummberofcars>");
			break;

		case 19: // reserve a room
			System.out.println("Reserving a room.");
			System.out.println("Purpose: ");
			System.out
					.println("\tReserve a given number of rooms for a customer at a particular location.");
			System.out.println("\nUsage: ");
			System.out
					.println("\treserveroom, <id>, <customerid>, <location>, <nummberofrooms>");
			break;

		case 20: // reserve an Itinerary
			System.out.println("Reserving an Itinerary.");
			System.out.println("Purpose: ");
			System.out
					.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
			System.out.println("\nUsage: ");
			System.out.println("\titinerary, <id>, <customerid>, "
					+ "<flightnumber1>....<flightnumberN>, "
					+ "<LocationToBookcarsOrrooms>, <car>, <room>");
			break;

		case 21: // quit the client
			System.out.println("Quitting client.");
			System.out.println("Purpose: ");
			System.out.println("\tExit the client application.");
			System.out.println("\nUsage: ");
			System.out.println("\tquit");
			break;

		case 22: // new customer with id
			System.out.println("Create new customer providing an id");
			System.out.println("Purpose: ");
			System.out.println("\tCreates a new customer with the id provided");
			System.out.println("\nUsage: ");
			System.out.println("\tnewcustomerid, <id>, <customerid>");
			break;

		default:
			System.out.println(command);
			System.out.println("The interface does not support this command.");
			break;
		}
	}

	public void wrongNumber() {
		System.out
				.println("The number of arguments provided in this command are wrong.");
		System.out
				.println("Type help, <commandname> to check usage of this command.");
	}

	public int getInt(Object temp) throws Exception {
		try {
			return (new Integer((String) temp)).intValue();
		} catch (Exception e) {
			throw e;
		}
	}

	public boolean getBoolean(Object temp) throws Exception {
		try {
			return (new Boolean((String) temp)).booleanValue();
		} catch (Exception e) {
			throw e;
		}
	}

	public String getString(Object temp) throws Exception {
		try {
			return (String) temp;
		} catch (Exception e) {
			throw e;
		}
	}

}
