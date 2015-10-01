package server;

import java.io.IOException;

public class Main {

	public static void main(String args[]) throws NumberFormatException, IOException{
		if(args.length != 1){
			System.out.println("Error must enter port number as argument");
			System.exit(-1);
		}
		Server server = new Server(Integer.parseInt(args[0]));
		server.run();
		
	}
}
