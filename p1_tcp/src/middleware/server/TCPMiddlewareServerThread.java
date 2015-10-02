package middleware.server;

import server.Car;
import server.Flight;
import server.Room;
import server.Trace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


public class TCPMiddlewareServerThread extends Thread{

    private Socket clientSocket,carSeverSocket, roomSeverSocket,
            flightSeverSocket, customerSeverSocket;

    private PrintWriter carOut, roomOut, flightOut, customerOut;

    private BufferedReader carIn, roomIn, flightIn, customerIn;

    private final String FAILED = "false";
    private final String SUCCESSFUL = "true";

    public TCPMiddlewareServerThread(Socket clientSocket, List<String> rmHostnames, List<Integer> rmPorts) throws IOException {
        super("TCPMiddlewareServerThread");

        this.clientSocket = clientSocket;

        connectRMServers(rmHostnames,rmPorts);
    }

    public void run(){
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()))
        ) {
            String command, result;


            while ((command = in.readLine()) != null) {
                if (command.equals("quit"))
                    break;

                result = command.contains("itinerary")? reserveItinerary(command) : dispatchCommand(command);

                out.println(result);
            }

            carSeverSocket.close();
            roomSeverSocket.close();
            flightSeverSocket.close();
            customerSeverSocket.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectRMServers(List<String> rmHostnames, List<Integer> rmPorts) throws IOException {

        this.carSeverSocket = new Socket(rmHostnames.get(0), rmPorts.get(0));
        this.carOut = new PrintWriter(this.carSeverSocket.getOutputStream(), true);
        this.carIn = new BufferedReader(new InputStreamReader(this.carSeverSocket.getInputStream()));

        this.roomSeverSocket = new Socket(rmHostnames.get(1), rmPorts.get(1));
        this.roomOut = new PrintWriter(this.roomSeverSocket.getOutputStream(), true);
        this.roomIn = new BufferedReader(new InputStreamReader(this.roomSeverSocket.getInputStream()));

        this.flightSeverSocket = new Socket(rmHostnames.get(2), rmPorts.get(2));
        this.flightOut = new PrintWriter(this.flightSeverSocket.getOutputStream(), true);
        this.flightIn = new BufferedReader(new InputStreamReader(this.flightSeverSocket.getInputStream()));

        this.customerSeverSocket = new Socket(rmHostnames.get(3), rmPorts.get(3));
        this.customerOut = new PrintWriter(this.customerSeverSocket.getOutputStream(), true);
        this.customerIn = new BufferedReader(new InputStreamReader(this.customerSeverSocket.getInputStream()));

    }

    private String reserveItinerary(String command) {
        Vector arguments;
        command = command.trim();
        arguments = parse(command);

        //just printing itinerary info
        System.out.println("Reserving an Itinerary using id: " + arguments.elementAt(1));
        System.out.println("Customer id: " + arguments.elementAt(2));
        for (int i = 0; i < arguments.size() - 6; i++)
            System.out.println("Flight number" + arguments.elementAt(3 + i));
        System.out.println("Location for car/room booking: " + arguments.elementAt(arguments.size() - 3));
        System.out.println("car to book?: " + arguments.elementAt(arguments.size() - 2));
        System.out.println("room to book?: " + arguments.elementAt(arguments.size() - 1));

        try {
            int id = getInt(arguments.elementAt(1));
            int customer = getInt(arguments.elementAt(2));
            Vector flightNumbers = new Vector();
            for (int i = 0; i < arguments.size() - 6; i++)
                flightNumbers.addElement(arguments.elementAt(3 + i));
            String location = getString(arguments.elementAt(arguments.size() - 3));
            boolean needCar = getBoolean(arguments.elementAt(arguments.size() - 2));
            boolean needRoom = getBoolean(arguments.elementAt(arguments.size() - 1));

            String queryCustomer = String.format("querycustomer, %d, %d", id, customer);
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

            String reserveCarCommand = String.format("reservecar, %d, %d, %s", id, customer, location);

            if (needCar) {
                carSuccess = dispatchCommand(reserveCarCommand).equals(SUCCESSFUL);
            }

            if (carSuccess) {
                Trace.info("car reserved successfully");
            }

            // Don't bother with room if the car didn't succeed
            if (needRoom && (!needCar || carSuccess)) {
                String reserveRoomCommand = String.format("reserveroom, %d, %d, %s", id, customer, location);

                roomSuccess = dispatchCommand(reserveRoomCommand).equals(SUCCESSFUL);
            }
            if (roomSuccess) {
                Trace.info("room reserved successfully");
            }

            // Don't bother with flight if room didn't succeed
            //Did we need a room? If so it has to succeed.
            //Did we need a car? if so it has to succeed
            if ((!needRoom || (needRoom && roomSuccess)) && (!needCar || (needCar && carSuccess))) {
                for (Object fNumber : flightNumbers) {
                    int flightNumber = Integer.parseInt(fNumber.toString());

                    String reserveFlightCommand = String.format("reserveflight, %d, %d, %d", id, customer, flightNumber);
                    flightSuccess = dispatchCommand(reserveFlightCommand).equals(SUCCESSFUL);

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

            boolean removeCar = (needCar && carSuccess) && (roomFailed || flightFailed);
            boolean removeRoom = (needRoom && roomSuccess) && flightFailed;
            boolean removeFlights = numFlightsReserved > 0 && flightFailed;

            if (removeCar) {
                String cancelReserveCar = String.format("cancelcar, %d, %d, %s", id, customer, location);
                dispatchCommand(cancelReserveCar);
                Trace.info("removed car reservation");
            }
            if (removeRoom) {
                String cancelReserveRoom = String.format("cancelroom, %d, %d, %s", id, customer, location);
                dispatchCommand(cancelReserveRoom);
                Trace.info("removed room reservation");
            }
            if (removeFlights) {
                Trace.info("removing " + numFlightsReserved + "flight reservations");
                for (int i = 0; i < numFlightsReserved; i++) {
                    String fNumber = flightNumbers.get(i).toString();
                    int flightNumber = Integer.parseInt(fNumber);
                    String cancelReserveFlight = String.format("cancelroom, %d, %d, %s", id, customer, flightNumber);
                    dispatchCommand(cancelReserveFlight);
                    Trace.info("removed flight reservation number : " + flightNumber);
                }
            }

            //if none of the 3 failed then update the client
            if (!carFailed && !roomFailed && !flightFailed) {
                int price = 0;

                if (needCar) {
                    String queryCarPrice = String.format("querycarprice, %d, %s", id, location);
                    price = Integer.parseInt(dispatchCommand(queryCarPrice));
                    String reserveCustomer = String.format("reservecustomer %d, %d, %s, %s, %d", id, customer,
                            Car.getKey(location), location, price);
                    dispatchCommand(reserveCustomer);
                }

                if (needRoom) {
                    String queryRoomPrice = String.format("queryroomprice, %d, %s", id, location);
                    price = Integer.parseInt(dispatchCommand(queryRoomPrice));
                    String reserveCustomer = String.format("reservecustomer %d, %d, %s, %s, %d", id, customer,
                            Room.getKey(location), location, price);
                    dispatchCommand(reserveCustomer);

                }

                for (Object fNumber : flightNumbers) {
                    int flightNumber = Integer.parseInt(fNumber.toString());
                    String queryFlightPrice = String.format("queryflightprice, %d, %d", id, flightNumber);
                    price = Integer.parseInt(dispatchCommand(queryFlightPrice));
                    String reserveCustomer = String.format("reservecustomer %d, %d, %s, %s, %d", id, customer,
                            Flight.getKey(flightNumber), location, price);
                    dispatchCommand(reserveCustomer);
                }
                Trace.info("Itinerary booked successfully");
                return SUCCESSFUL;
            }
        }
        catch(Exception e) {
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

    private String dispatchCommand(String command){
        String result = null;
        PrintWriter out = null;
        BufferedReader in = null;


        String commandInLowerCase = command.toLowerCase();
        if (commandInLowerCase.contains("car")){
            out = this.carOut;
            in = this.carIn;
        }
        else if (commandInLowerCase.contains("customer")){
            out = this.customerOut;
            in = this.customerIn;
        }
        else if (commandInLowerCase.contains("room")){
            out = this.roomOut;
            in = this.roomIn;
        }
        else if (commandInLowerCase.contains("flight")){
            out = this.flightOut;
            in = this.flightIn;
        }


        out.println(command);

        try {
            result = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private int getInt(Object temp) throws Exception {
        try {
            return (new Integer((String)temp)).intValue();
        }
        catch(Exception e) {
            throw e;
        }
    }

    private boolean getBoolean(Object temp) throws Exception {
        try {
            return (new Boolean((String)temp)).booleanValue();
        }
        catch(Exception e) {
            throw e;
        }
    }

    private String getString(Object temp) throws Exception {
        try {
            return (String)temp;
        }
        catch (Exception e) {
            throw e;
        }
    }
}
