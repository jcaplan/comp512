package client;

public class Test02 {

	
	String[] testCommands = {
		"newcustomer,0",
		"newcar,0,0,100,100",
		"newflight,0,0,100,100",
		"newroom,0,0,100,100",
		"reservecar,0,"
	};
	
	
	
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
            client.handleRequest("newcar,0,0,100,100");
            client.handleRequest("newflight,0,0,100,100");
            client.handleRequest("newroom,0,0,100,100");
            client.handleRequest("reservecar,0," + client0 + ",0");
            System.out.println(client.handleRequest("querycar,0,0"));
            System.out.println(client.handleRequest("querycustomer,0," + client0));
            
            client.handleRequest("reserveroom,0," + client0 + ",0");
            System.out.println(client.handleRequest("queryroom,0,0"));
            System.out.println(client.handleRequest("querycustomer,0," + client0));
            
            client.handleRequest("reserveflight,0," + client0 + ",0");
            System.out.println(client.handleRequest("queryflight,0,0"));
            System.out.println(client.handleRequest("querycustomer,0," + client0));
            
            client.handleRequest("deletecustomer,0,"+client0);
            client.handleRequest("deleteflight,0,0");
            

            client.close();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}