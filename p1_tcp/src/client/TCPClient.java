package client;

import java.net.*;
import java.io.*;

import server.ws.*;

public class TCPClient {

	ResourceManager proxy;
	PrintWriter out;
	BufferedReader in;
	Socket socket;
	public TCPClient(String serviceHost, int servicePort)
			throws UnknownHostException, IOException {
		socket = new Socket(serviceHost, servicePort);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

	}
	
	public void close(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
