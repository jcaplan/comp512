package test;


import client.*;

public class TestSingleClient {
    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Usage: MyClient <service-name> "
                    + "<service-host> <service-port>");
            System.exit(-1);
        }

        final int INITIAL_CAR_NUMBER = 100;
        final int INITIAL_SEAT_NUMBER = 200;
        final int INITIAL_ROOM_NUMBER = 300;

        String serviceName = args[0];
        String serviceHost = args[1];
        int servicePort = Integer.parseInt(args[2]);

        try {
            Client client = new Client(serviceName, serviceHost, servicePort);

            System.out.println("Querying non-existent customer...");
            System.out.println(client.handleRequest("querycustomer,0,0"));

            String client0 = client.handleRequest("newcustomer,0");
            System.out.println("Querying newly created customer info...");
            System.out.println("Result:" + client.handleRequest("querycustomer,0," + client0));

            System.out.println("Try to reserve car when there's no car...");
            System.out.println(client.handleRequest("reservecar,0," + client0 + ",0"));

            System.out.println("Add " + INITIAL_CAR_NUMBER + " cars at $100/car");
            System.out.println(client.handleRequest("newcar,0,0," + INITIAL_CAR_NUMBER + ",100"));
            System.out.println("Result:" + client.handleRequest("querycar,0,0") + " added");

            System.out.println("Add " + INITIAL_SEAT_NUMBER +" seats to flight 0, at $200/seat");
            System.out.println(client.handleRequest("newflight,0,0," + INITIAL_SEAT_NUMBER + ",200"));
            System.out.println(client.handleRequest("queryflight,0,0") + " added");


            System.out.println("Try to reserve itinerary when there's no room at the location");
            System.out.println(client.handleRequest("itinerary,0,"+client0 +",0,0,true,true"));
            System.out.println("Expected number of remaining cars: " + INITIAL_CAR_NUMBER);
            System.out.println("Result:" + client.handleRequest("querycar,0,0"));
            System.out.println("Expected number of remaining seats: " + INITIAL_SEAT_NUMBER);
            System.out.println("Result:" + client.handleRequest("queryflight,0,0"));

            System.out.println("Add 300 rooms to location 0, at $300/room");
            System.out.println(client.handleRequest("newroom,0,0," + INITIAL_ROOM_NUMBER + ",300"));
            System.out.println("Result:" + client.handleRequest("queryroom,0,0") + " added");

            System.out.println("Query again1");
            System.out.println("Result:" + client.handleRequest("querycustomer,0," + client0));

            System.out.println("Try to reserve same itinerary again");
            System.out.println(client.handleRequest("itinerary,0,"+client0 +",0,0,true,true"));
            System.out.println("Query again2");
            System.out.println("Result:" + client.handleRequest("querycustomer,0," + client0));
            System.out.println("Expected number of remaining cars: " + (INITIAL_CAR_NUMBER-1));
            System.out.println("Result:" + client.handleRequest("querycar,0,0"));
            System.out.println("Expected number of remaining seats: " + (INITIAL_SEAT_NUMBER-1));
            System.out.println("Result:" + client.handleRequest("queryflight,0,0"));
            System.out.println("Expected number of remaining rooms: " + (INITIAL_ROOM_NUMBER-1));
            System.out.println("Result:" + client.handleRequest("queryroom,0,0"));

            System.out.println("Query again");
            System.out.println("Result:" + client.handleRequest("querycustomer,0," + client0));

            System.out.println("Try to delete the customer");
            System.out.println(client.handleRequest("deletecustomer,0," + client0));
            System.out.println("Check that all reservation made by the client are canceled");
            System.out.println("Expected number of remaining cars: " + INITIAL_CAR_NUMBER);
            System.out.println("Result:" + client.handleRequest("querycar,0,0"));
            System.out.println("Expected number of remaining seats: " + INITIAL_SEAT_NUMBER);
            System.out.println("Result:" + client.handleRequest("queryflight,0,0"));
            System.out.println("Expected number of remaining rooms: " + INITIAL_ROOM_NUMBER);
            System.out.println("Result:" + client.handleRequest("queryroom,0,0"));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
