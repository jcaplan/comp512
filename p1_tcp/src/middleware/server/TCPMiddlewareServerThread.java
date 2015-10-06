package middleware.server;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import server.Car;
import server.Flight;
import server.Room;
import server.Trace;

public class TCPMiddlewareServerThread extends Thread {

	private Socket clientSocket, carSeverSocket, roomSeverSocket,
			flightSeverSocket, customerSeverSocket;

	private ObjectOutputStream carOut, roomOut, flightOut, customerOut;

	private ObjectInputStream carIn, roomIn, flightIn, customerIn;

	private final String FAILED = "false";
	private final String SUCCESSFUL = "true";

	public TCPMiddlewareServerThread(Socket clientSocket,
			List<String> rmHostnames, List<Integer> rmPorts) throws IOException {
		super("TCPMiddlewareServerThread");

		this.clientSocket = clientSocket;

		connectRMServers(rmHostnames, rmPorts);
	}

	public void run() {
		try (ObjectOutputStream out = new ObjectOutputStream(
				clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(
						clientSocket.getInputStream())) {
			String command = null, result;
			
			while (true) {
				try{
					command = (String) in.readObject();
				} catch (EOFException e){
					//Do nothing
				}
				if (command.equals("quit"))
					break;

				result = command.contains("itinerary") ? reserveItinerary(command)
						: command.contains("deletecustomer") ? deleteCustomer(command) : dispatchCommand(command);

				out.writeObject(result);
			}

			carSeverSocket.close();
			roomSeverSocket.close();
			flightSeverSocket.close();
			customerSeverSocket.close();
			clientSocket.close();
		} catch(EOFException e){
			//Do nothing
		} catch (Exception e) {
            e.printStackTrace();
        } 
    }

	private void connectRMServers(List<String> rmHostnames,
			List<Integer> rmPorts) throws IOException {

		this.carSeverSocket = new Socket(rmHostnames.get(0), rmPorts.get(0));
		this.carOut = new ObjectOutputStream(
				this.carSeverSocket.getOutputStream());
		this.carIn = new ObjectInputStream(this.carSeverSocket.getInputStream());

		this.roomSeverSocket = new Socket(rmHostnames.get(1), rmPorts.get(1));
		this.roomOut = new ObjectOutputStream(
				this.roomSeverSocket.getOutputStream());
		this.roomIn = new ObjectInputStream(
				this.roomSeverSocket.getInputStream());

		this.flightSeverSocket = new Socket(rmHostnames.get(2), rmPorts.get(2));
		this.flightOut = new ObjectOutputStream(
				this.flightSeverSocket.getOutputStream());
		this.flightIn = new ObjectInputStream(
				this.flightSeverSocket.getInputStream());

		this.customerSeverSocket = new Socket(rmHostnames.get(3),
				rmPorts.get(3));
		this.customerOut = new ObjectOutputStream(
				this.customerSeverSocket.getOutputStream());
		this.customerIn = new ObjectInputStream(
				this.customerSeverSocket.getInputStream());

	}

    private String deleteCustomer(String command) throws Exception {
        Trace.info("DEBUG: " + command);
        Vector arguments;
        command = command.trim();
        arguments = parse(command);

        int id = getInt(arguments.elementAt(1));
        int customer = getInt(arguments.elementAt(2));

        String queryCustomer = String.format("querycustomer,%d,%d", id, customer);
        String reservations = dispatchCommand(queryCustomer);
        Trace.info(queryCustomer);
        Trace.info(reservations);

        if (reservations.isEmpty()) {
            Trace.info("Customer does not exist");
            return FAILED;
        } else {
            Trace.info("Found customer: " + reservations);
        }

        StringTokenizer tokenizer = new StringTokenizer(reservations, "\n");
        while(tokenizer.hasMoreTokens()){
            //Now parse the bill using spaces
            String line  = tokenizer.nextToken().trim();
            String[] tokens = line.split(" ");
            //Skip lines that are not relevant
            if(tokens[0].equals("Bill") || tokens.length < 2){
                continue;
            }

            //The key will be "type-arg" so split it around the '-' and
            //then cancel the item
            for(String t : tokens){
                System.out.print(t);
            }
            System.out.println();
            int numItems = Integer.parseInt(tokens[0]);
            String[] keyArray = tokens[1].split("-");
            if (keyArray[0].equals("flight")){
            	for(int i = 0; i < numItems; i++){
	                String cancelReserveFlight = String.format("cancelflight,%d, %d,%s", id, customer, Integer.parseInt(keyArray[1]));
	                dispatchCommand(cancelReserveFlight);
            	}
            }
            else if(keyArray[0].equals("car")) {interrupt();
            	for(int i = 0; i < numItems; i++){
	                String cancelReserveCar = String.format("cancelcar,%d,%d,%s", id, customer, keyArray[1]);
	                dispatchCommand(cancelReserveCar);
            	}
            }
            else if(keyArray[0].equals("room")){
            	for(int i = 0; i < numItems; i++){
	            	String cancelReserveRoom = String.format("cancelroom,%d,%d,%s", id, customer, keyArray[1]);
	                dispatchCommand(cancelReserveRoom);
            	}
            }
        }

        String deleteCustomer = String.format("deletecustomer,%d,%d", id, customer);
        return dispatchCommand(deleteCustomer);
    }

	private String reserveItinerary(String command) {
		Vector arguments;
		command = command.trim();
		arguments = parse(command);

		// just printing itinerary info
		System.out.println("Reserving an Itinerary using id: "
				+ arguments.elementAt(1));
		System.out.println("Customer id: " + arguments.elementAt(2));
		for (int i = 0; i < arguments.size() - 6; i++)
			System.out.println("Flight number" + arguments.elementAt(3 + i));
		System.out.println("Location for car/room booking: "
				+ arguments.elementAt(arguments.size() - 3));
		System.out.println("car to book?: "
				+ arguments.elementAt(arguments.size() - 2));
		System.out.println("room to book?: "
				+ arguments.elementAt(arguments.size() - 1));

		try {
			int id = getInt(arguments.elementAt(1));
			int customer = getInt(arguments.elementAt(2));
			Vector flightNumbers = new Vector();
			for (int i = 0; i < arguments.size() - 6; i++)
				flightNumbers.addElement(arguments.elementAt(3 + i));
			String location = getString(arguments
					.elementAt(arguments.size() - 3));
			boolean needCar = getBoolean(arguments
					.elementAt(arguments.size() - 2));
			boolean needRoom = getBoolean(arguments
					.elementAt(arguments.size() - 1));

			String queryCustomer = String.format("querycustomer, %d, %d", id,
					customer);
			String customerInfo = dispatchCommand(queryCustomer);
			if (customerInfo.isEmpty()) {
				Trace.info("Custome does not exist");
				return FAILED;
			} else {
				Trace.info("Found customer: " + customerInfo);
			}

			boolean carSuccess = false;
			boolean roomSuccess = false;
			boolean flightSuccess = false;
			int numFlightsReserved = 0;

			String reserveCarCommand = String.format("reservecar, %d, %d, %s",
					id, customer, location);

			if (needCar) {
				carSuccess = dispatchCommand(reserveCarCommand).equals(
						SUCCESSFUL);
			}

			if (carSuccess) {
				Trace.info("car reserved successfully");
			}

			// Don't bother with room if the car didn't succeed
			if (needRoom && (!needCar || carSuccess)) {
				String reserveRoomCommand = String.format(
						"reserveroom, %d, %d, %s", id, customer, location);

				roomSuccess = dispatchCommand(reserveRoomCommand).equals(
						SUCCESSFUL);
			}
			if (roomSuccess) {
				Trace.info("room reserved successfully");
			}

			// Don't bother with flight if room didn't succeed
			// Did we need a room? If so it has to succeed.
			// Did we need a car? if so it has to succeed
			if ((!needRoom || (needRoom && roomSuccess))
					&& (!needCar || (needCar && carSuccess))) {
				for (Object fNumber : flightNumbers) {
					int flightNumber = Integer.parseInt(fNumber.toString());

					String reserveFlightCommand = String.format(
							"reserveflight, %d, %d, %d", id, customer,
							flightNumber);
					flightSuccess = dispatchCommand(reserveFlightCommand)
							.equals(SUCCESSFUL);

					if (flightSuccess) {
						numFlightsReserved++;
					} else {
						break;
					}
				}

			}

			boolean carFailed = needCar && !carSuccess;
			boolean flightFailed = !flightSuccess;
			boolean roomFailed = (needRoom && !roomSuccess);

			boolean removeCar = (needCar && carSuccess)
					&& (roomFailed || flightFailed);
			boolean removeRoom = (needRoom && roomSuccess) && flightFailed;
			boolean removeFlights = numFlightsReserved > 0 && flightFailed;

			if (removeCar) {
				String cancelReserveCar = String.format(
						"cancelcar, %d, %d, %s", id, customer, location);
				dispatchCommand(cancelReserveCar);
				Trace.info("removed car reservation");
			}
			if (removeRoom) {
				String cancelReserveRoom = String.format(
						"cancelroom, %d, %d, %s", id, customer, location);
				dispatchCommand(cancelReserveRoom);
				Trace.info("removed room reservation");
			}
			if (removeFlights) {
				Trace.info("removing " + numFlightsReserved
						+ "flight reservations");
				for (int i = 0; i < numFlightsReserved; i++) {
					String fNumber = flightNumbers.get(i).toString();
					int flightNumber = Integer.parseInt(fNumber);
					String cancelReserveFlight = String.format(
							"cancelflight, %d, %d, %s", id, customer,
							flightNumber);
					dispatchCommand(cancelReserveFlight);
					Trace.info("removed flight reservation number : "
							+ flightNumber);
				}
			}

			// if none of the 3 failed then update the client
			if (!carFailed && !roomFailed && !flightFailed) {
				int price = 0;

				if (needCar) {
					String queryCarPrice = String.format(
							"querycarprice, %d, %s", id, location);
					price = Integer.parseInt(dispatchCommand(queryCarPrice));
					String reserveCustomer = String.format(
							"reservecustomer, %d, %d, %s, %s, %d", id,
							customer, Car.getKey(location), location, price);
					dispatchCommand(reserveCustomer);
				}

				if (needRoom) {
					String queryRoomPrice = String.format(
							"queryroomprice, %d, %s", id, location);
					price = Integer.parseInt(dispatchCommand(queryRoomPrice));
					String reserveCustomer = String.format(
							"reservecustomer, %d, %d, %s, %s, %d", id,
							customer, Room.getKey(location), location, price);
					dispatchCommand(reserveCustomer);

				}

				for (Object fNumber : flightNumbers) {
					int flightNumber = Integer.parseInt(fNumber.toString());
					String queryFlightPrice = String.format(
							"queryflightprice, %d, %d", id, flightNumber);
					price = Integer.parseInt(dispatchCommand(queryFlightPrice));
					String reserveCustomer = String.format(
							"reservecustomer, %d, %d, %s, %s, %d", id,
							customer, Flight.getKey(flightNumber), location,
							price);
					dispatchCommand(reserveCustomer);
				}
				Trace.info("Itinerary booked successfully");
				return SUCCESSFUL;
			}
		} catch (Exception e) {
			System.out.println("EXCEPTION: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		return FAILED;
	}

	private Vector parse(String command) {
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

	private String dispatchCommand(String command) throws IOException {
		String result = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		String commandInLowerCase = command.toLowerCase();
		// Check customer first!
		if (commandInLowerCase.contains("customer")) {
			out = this.customerOut;
			in = this.customerIn;
		} else if (commandInLowerCase.contains("car")) {
			out = this.carOut;
			in = this.carIn;
		}

		else if (commandInLowerCase.contains("room")) {
			out = this.roomOut;
			in = this.roomIn;
		} else if (commandInLowerCase.contains("flight")) {
			out = this.flightOut;
			in = this.flightIn;
		}

		out.writeObject(command);

		try {
			result = (String) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

	private int getInt(Object temp) throws Exception {
		try {
			return (new Integer((String) temp)).intValue();
		} catch (Exception e) {
			throw e;
		}
	}

	private boolean getBoolean(Object temp) throws Exception {
		try {
			return (new Boolean((String) temp)).booleanValue();
		} catch (Exception e) {
			throw e;
		}
	}

	private String getString(Object temp) throws Exception {
		try {
			return (String) temp;
		} catch (Exception e) {
			throw e;
		}
	}
}
