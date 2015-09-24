package middleware.client;

import java.util.*;
import java.io.*;

public class MWClient extends WSClient {

    public MWClient(String serviceName, String serviceHost, int servicePort) 
    throws Exception {
        super(serviceName, serviceHost, servicePort);
    }

  

    public void run() {
        try {
                    
                    if (proxy.addFlight(1, 1, 1, 1))
                        System.out.println("Flight added");
                    else
                        System.out.println("Flight could not be added");
            } catch (Exception e){
                e.printStackTrace();
            }   
    }
}