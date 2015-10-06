package client;

import java.net.*;
import java.io.*;

import server.ws.*;

public class TCPClient {

	ResourceManager proxy;
	ObjectOutputStream out;
	ObjectInputStream in;
	Socket socket;
	public TCPClient(String serviceHost, int servicePort)
			throws UnknownHostException, IOException {
		socket = new Socket(serviceHost, servicePort);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

	}
	
	public void close(){
		try {
			out.writeObject("quit");
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
