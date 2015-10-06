package middleware.main;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import middleware.server.TCPMiddlewareServerThread;


public class Main {

    public static void main(String[] args) 
    throws Exception {
    
        if (args.length != 9 ) {
            System.out.println(
                "Usage: java Main <middleware-server-port-number> <car-rm-hostname> <car-rm-port> " +
                        "<room-rm-hostname> <room-rm-port> <flight-rm-hostname> <flight-rm-port> " +
                        "<customer-rm-hostname> <customer-rm-port> ");
            System.exit(-1);
        } 

        ArrayList<String> hostnames = new ArrayList<>();

        ArrayList<Integer> portnumbers = new ArrayList<>();

        for (int i = 1; i<9; i += 2){
            hostnames.add(args[i]);
            portnumbers.add(Integer.parseInt(args[i+1]));
        }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new TCPMiddlewareServerThread(serverSocket.accept(), hostnames, portnumbers).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
    
}
