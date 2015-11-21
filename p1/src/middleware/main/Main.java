package middleware.main;


import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;


public class Main {

    public static void main(String[] args) 
    throws Exception {
    
        if (args.length != 4 ) {
            System.out.println(
                "Usage: java Main <server-service-name> <server-service-port> <server-deploy-dir> <shutdown-port>");
            System.exit(-1);
        }
    
        String serviceName = args[0];
        int port = Integer.parseInt(args[1]);
        String deployDir = args[2];
        int shutdownPort = Integer.parseInt(args[3]);


        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(deployDir);

        tomcat.getHost().setAppBase(deployDir);
        tomcat.getHost().setDeployOnStartup(true);
        tomcat.getHost().setAutoDeploy(true);

        //tomcat.addWebapp("", new File(deployDir).getAbsolutePath());

        tomcat.addWebapp("/" + serviceName, 
                new File(deployDir + "/" + serviceName).getAbsolutePath());
        tomcat.getServer().setPort(shutdownPort);
        Runtime.getRuntime().addShutdownHook(new Thread(){
        	public void run(){
        		System.out.println("Shutting down RM");
        	}
        });


        tomcat.enableNaming();
        tomcat.start();
        tomcat.getServer().await();
        
    }
    
}
