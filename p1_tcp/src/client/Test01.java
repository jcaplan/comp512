package client;

//Test 01 : multithreaded client


public class Test01 {
	
	public static final int NUM_THREADS = 5;
    public static void main(String[] args) {
        try {
        
            if (args.length != 3) {
                System.out.println("Usage: MyClient <service-name> " 
                        + "<service-host> <service-port>");
                System.exit(-1);
            }
            
            //parse inputs
            
            String serviceName = args[0];
            String serviceHost = args[1];
            int servicePort = Integer.parseInt(args[2]);
            
            //set up servers

            Client setupClient = new Client(serviceName, serviceHost, servicePort);
            setupClient.handleRequest("newcar,0,0,100,100");
            setupClient.handleRequest("newflight,0,0,200,200");
            setupClient.handleRequest("newroom,0,0,300,300");
            
            
            ClientTestThread[] clientThread = new ClientTestThread[NUM_THREADS];
           try{
        	   for(int i = 0; i < NUM_THREADS; i++){
        		   clientThread[i] = new ClientTestThread(new Client(serviceName, serviceHost, servicePort));	
        		   clientThread[i].start();
        	   }
           } finally {
        	   for(int i = 0; i < NUM_THREADS; i++){
        		   clientThread[i].closeClient();
        	   }
           }

            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
