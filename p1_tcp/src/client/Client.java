package client;

import java.util.*;
import java.io.*;


public class Client extends client.TCPClient {




    public Client(String serviceName, String serviceHost, int servicePort) 
    throws Exception {
        super(serviceHost, servicePort);
    }




    public void run(){
        
        System.out.println("Client Interface");
        System.out.println("Type \"help\" for list of supported commands");
        
        while (true) {
            String command = "";

             BufferedReader stdin = 
                new BufferedReader(new InputStreamReader(System.in));
        
            try {
                //read the next command
                command = stdin.readLine();
            }
            catch (IOException io) {
                System.out.println("Unable to read from standard in");
                System.exit(1);
            }
            //remove heading and trailing white space
           handleRequest(command);
        }
    }

    public String handleRequest(String command) {
    	String result = "";
        if(command.isEmpty()){
        	System.exit(0);
        } else {
        	out.println(command);
        	try {
        		result = in.readLine();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        System.out.println("sending command: " + command);
		System.out.println("result: " + result);
        return result;
        
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
        else
            return 666;
    }

    public void listCommands() {
        System.out.println("\nWelcome to the client interface provided to test your project.");
        System.out.println("Commands accepted by the interface are: ");
        System.out.println("help");
        System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcustomerid\ndeleteflight\ndeletecar\ndeleteroom");
        System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
        System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
        System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
        System.out.println("quit");
        System.out.println("\ntype help, <commandname> for detailed info (note the use of comma).");
    }


    public void listSpecific(String command) {
        System.out.print("Help on: ");
        switch(findChoice(command)) {
            case 1:
            System.out.println("Help");
            System.out.println("\nTyping help on the prompt gives a list of all the commands available.");
            System.out.println("Typing help, <commandname> gives details on how to use the particular command.");
            break;

            case 2:  //new flight
            System.out.println("Adding a new Flight.");
            System.out.println("Purpose: ");
            System.out.println("\tAdd information about a new flight.");
            System.out.println("\nUsage: ");
            System.out.println("\tnewflight, <id>, <flightnumber>, <numSeats>, <flightprice>");
            break;
            
            case 3:  //new car
            System.out.println("Adding a new car.");
            System.out.println("Purpose: ");
            System.out.println("\tAdd information about a new car location.");
            System.out.println("\nUsage: ");
            System.out.println("\tnewcar, <id>, <location>, <numberofcars>, <pricepercar>");
            break;
            
            case 4:  //new room
            System.out.println("Adding a new room.");
            System.out.println("Purpose: ");
            System.out.println("\tAdd information about a new room location.");
            System.out.println("\nUsage: ");
            System.out.println("\tnewroom, <id>, <location>, <numberofrooms>, <priceperroom>");
            break;
            
            case 5:  //new Customer
            System.out.println("Adding a new Customer.");
            System.out.println("Purpose: ");
            System.out.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
            System.out.println("\nUsage: ");
            System.out.println("\tnewcustomer, <id>");
            break;
            
            
            case 6: //delete Flight
            System.out.println("Deleting a flight");
            System.out.println("Purpose: ");
            System.out.println("\tDelete a flight's information.");
            System.out.println("\nUsage: ");
            System.out.println("\tdeleteflight, <id>, <flightnumber>");
            break;
            
            case 7: //delete car
            System.out.println("Deleting a car");
            System.out.println("Purpose: ");
            System.out.println("\tDelete all cars from a location.");
            System.out.println("\nUsage: ");
            System.out.println("\tdeletecar, <id>, <location>, <numCars>");
            break;
            
            case 8: //delete room
            System.out.println("Deleting a room");
            System.out.println("\nPurpose: ");
            System.out.println("\tDelete all rooms from a location.");
            System.out.println("Usage: ");
            System.out.println("\tdeleteroom, <id>, <location>, <numRooms>");
            break;
            
            case 9: //delete Customer
            System.out.println("Deleting a Customer");
            System.out.println("Purpose: ");
            System.out.println("\tRemove a customer from the database.");
            System.out.println("\nUsage: ");
            System.out.println("\tdeletecustomer, <id>, <customerid>");
            break;
            
            case 10: //querying a flight
            System.out.println("Querying flight.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain Seat information about a certain flight.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryflight, <id>, <flightnumber>");
            break;
            
            case 11: //querying a car Location
            System.out.println("Querying a car location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain number of cars at a certain car location.");
            System.out.println("\nUsage: ");
            System.out.println("\tquerycar, <id>, <location>");        
            break;
            
            case 12: //querying a room location
            System.out.println("Querying a room Location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain number of rooms at a certain room location.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryroom, <id>, <location>");        
            break;
            
            case 13: //querying Customer Information
            System.out.println("Querying Customer Information.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain information about a customer.");
            System.out.println("\nUsage: ");
            System.out.println("\tquerycustomer, <id>, <customerid>");
            break;               
            
            case 14: //querying a flight for price 
            System.out.println("Querying flight.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain price information about a certain flight.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryflightprice, <id>, <flightnumber>");
            break;
            
            case 15: //querying a car Location for price
            System.out.println("Querying a car location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain price information about a certain car location.");
            System.out.println("\nUsage: ");
            System.out.println("\tquerycarprice, <id>, <location>");        
            break;
            
            case 16: //querying a room location for price
            System.out.println("Querying a room Location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain price information about a certain room location.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryroomprice, <id>, <location>");        
            break;

            case 17:  //reserve a flight
            System.out.println("Reserving a flight.");
            System.out.println("Purpose: ");
            System.out.println("\tReserve a flight for a customer.");
            System.out.println("\nUsage: ");
            System.out.println("\treserveflight, <id>, <customerid>, <flightnumber>");
            break;
            
            case 18:  //reserve a car
            System.out.println("Reserving a car.");
            System.out.println("Purpose: ");
            System.out.println("\tReserve a given number of cars for a customer at a particular location.");
            System.out.println("\nUsage: ");
            System.out.println("\treservecar, <id>, <customerid>, <location>, <nummberofcars>");
            break;
            
            case 19:  //reserve a room
            System.out.println("Reserving a room.");
            System.out.println("Purpose: ");
            System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
            System.out.println("\nUsage: ");
            System.out.println("\treserveroom, <id>, <customerid>, <location>, <nummberofrooms>");
            break;
            
            case 20:  //reserve an Itinerary
            System.out.println("Reserving an Itinerary.");
            System.out.println("Purpose: ");
            System.out.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
            System.out.println("\nUsage: ");
            System.out.println("\titinerary, <id>, <customerid>, "
                    + "<flightnumber1>....<flightnumberN>, "
                    + "<LocationToBookcarsOrrooms>, <car>, <room>");
            break;
            

            case 21:  //quit the client
            System.out.println("Quitting client.");
            System.out.println("Purpose: ");
            System.out.println("\tExit the client application.");
            System.out.println("\nUsage: ");
            System.out.println("\tquit");
            break;
            
            case 22:  //new customer with id
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
        System.out.println("The number of arguments provided in this command are wrong.");
        System.out.println("Type help, <commandname> to check usage of this command.");
    }

    public int getInt(Object temp) throws Exception {
        try {
            return (new Integer((String)temp)).intValue();
        }
        catch(Exception e) {
            throw e;
        }
    }
    
    public boolean getBoolean(Object temp) throws Exception {
        try {
            return (new Boolean((String)temp)).booleanValue();
        }
        catch(Exception e) {
            throw e;
        }
    }

    public String getString(Object temp) throws Exception {
        try {    
            return (String)temp;
        }
        catch (Exception e) {
            throw e;
        }
    }
    
}
