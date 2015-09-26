package middleware.main;


import java.io.File;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;


public class Main {

    public static void main(String[] args) 
    throws Exception {
    
        if (args.length != 15 ) {
            System.out.println(
                "Usage: java Main <server-service-name> <server-service-port> <server-deploy-dir>" +
                "<client-service-name>  <client-service-host> <client-service-port>");
            System.exit(-1);
        }
    
        String serviceName = args[0];
        int port = Integer.parseInt(args[1]);
        String deployDir = args[2];



        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(deployDir);

        tomcat.getHost().setAppBase(deployDir);
        tomcat.getHost().setDeployOnStartup(true);
        tomcat.getHost().setAutoDeploy(true);

        //tomcat.addWebapp("", new File(deployDir).getAbsolutePath());

        tomcat.addWebapp("/" + serviceName, 
                new File(deployDir + "/" + serviceName).getAbsolutePath());


        String clientServiceName = args[3];
        String clientServiceHost = args[4];
        int clientPort = Integer.parseInt(args[5]);
        // ResourceManagerImpl.setCarClient(clientServiceName,clientServiceHost,clientPort);


        clientServiceName = args[6];
        clientServiceHost = args[7];
        clientPort = Integer.parseInt(args[8]);
        // ResourceManagerImpl.setRoomClient(clientServiceName,clientServiceHost,clientPort);

        clientServiceName = args[9];
        clientServiceHost = args[10];
        clientPort = Integer.parseInt(args[11]);
        // ResourceManagerImpl.setFlightClient(clientServiceName,clientServiceHost,clientPort);

        clientServiceName = args[12];
        clientServiceHost = args[13];
        clientPort = Integer.parseInt(args[14]);
        // ResourceManagerImpl.setCustomerClient(clientServiceName,clientServiceHost,clientPort);
        // Client client = ResourceManagerImpl.getNewCustomerClient();
        
        tomcat.start();
        tomcat.getServer().await();
    }
    
}
