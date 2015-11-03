package client;


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
		setupClient.handleRequest("newcar,0,0,100,100");
		setupClient.handleRequest("newflight,0,0,100,200");
		setupClient.handleRequest("newroom,0,0,100,300");

		ClientTestThread[] clientThread = new ClientTestThread[NUM_THREADS];

		for (int i = 0; i < NUM_THREADS; i++) {
			clientThread[i] = new ClientTestThread(new Client(serviceName,
					serviceHost, servicePort));
			clientThread[i].start();
		}

		
		for (int i = 0; i < NUM_THREADS; i++) {
			clientThread[i].join();

		}

		int numCars = Integer.parseInt(setupClient.handleRequest("querycar,0,0"));
		int numFlights = Integer.parseInt(setupClient.handleRequest("queryflight,0,0"));
		int numRooms = Integer.parseInt(setupClient.handleRequest("queryroom,0,0"));
		
		boolean error = false;;
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
