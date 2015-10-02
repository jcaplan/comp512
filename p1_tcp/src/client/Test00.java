package client;

public class Test00 {
	
	
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
            
//            client.run();
            
            //set up customers
            String client0 = client.handleRequest("newcustomer,0");
            String client1 = client.handleRequest("newcustomer,1");
            //set up a car
            client.handleRequest("newcar,0,0,100,100");

            //try an itinerary that won't work then check car
            //a few times
            for(int i = 0; i < 5; i++){
	            client.handleRequest("itinerary,0,"+client0 +",0,0,0,0");
	            client.handleRequest("querycar,0,0");
            }
            
            
            client.handleRequest("newflight,0,0,200,200");
            client.handleRequest("newroom,0,0,300,300");
            
            client.handleRequest("itinerary,0,"+client1 +",0,0,true,true");
            

            client.handleRequest("querycar,0,0");
            client.handleRequest("queryflight,0,0");
            client.handleRequest("queryroom,0,0");
            
            
//            client.handleRequest("deleteCustomer,0," + client1);
//            client.handleRequest("deleteCustomer,1," + client0);
//
//            
//            client.handleRequest("queryCustomer,0," + client1);
//            client.handleRequest("queryCustomer,0," + client0);
//            client.handleRequest("querycar,0,0");
//            client.handleRequest("queryflight,0,0");
//            client.handleRequest("queryroom,0,0");
//            
  

            client.close();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
