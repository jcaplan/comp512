package client;


import test.ClientTestThread;
import client.*;
//Test 01 : multithreaded client

public class Test01 {

	public static final int NUM_THREADS = 5;

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.out.println("Usage: MyClient <service-name> "
					+ "<service-host> <service-port>");
			System.exit(-1);
		}

		// parse inputs

		String serviceName = args[0];
		String serviceHost = args[1];
		int servicePort = Integer.parseInt(args[2]);

		// set up servers

		Client setupClient = new Client(serviceName, serviceHost, servicePort);
		String txnId = setupClient.handleRequest("start");
		setupClient.handleRequest(String.format("newcar,%s,0,100,100",txnId));
		setupClient.handleRequest(String.format("newflight,%s,0,100,200",txnId));
		setupClient.handleRequest(String.format("newroom,%s,0,100,300",txnId));
		setupClient.handleRequest("commit,"+txnId);

		ClientTestThread[] clientThread = new ClientTestThread[NUM_THREADS];

		for (int i = 0; i < NUM_THREADS; i++) {
			clientThread[i] = new ClientTestThread(new Client(serviceName,
					serviceHost, servicePort));
			clientThread[i].start();
		}

		
		for (int i = 0; i < NUM_THREADS; i++) {
			clientThread[i].join();

		}

		for(int i = 0; i < NUM_THREADS; i++){
			txnId = setupClient.handleRequest("start");
			setupClient.handleRequest("deletecustomer," + txnId + ","+clientThread[i].clientId);
			setupClient.handleRequest(String.format("commit,%s,",txnId));
		
		}

		txnId = setupClient.handleRequest("start");
		int numCars = Integer.parseInt(setupClient.handleRequest(String.format("querycar,%s,0",txnId)));
		int numFlights = Integer.parseInt(setupClient.handleRequest(String.format("queryflight,%s,0",txnId)));
		int numRooms = Integer.parseInt(setupClient.handleRequest(String.format("queryroom,%s,0",txnId)));
		setupClient.handleRequest("commit,"+txnId);
		
		boolean error = false;
		if(numCars != 100){
			System.err.println("number of cars is incorrect");
			error = true;
		}
		if(numFlights != 100){
			System.err.println("number of flights is incorrect");
			error = true;
		}
		if(numRooms != 100){
			System.err.println("number of rooms is incorrect");
			error = true;
		}
		if(!error){
			System.out.println("everything worked!");
		}

		

	}
}
