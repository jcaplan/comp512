package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class Server {

	ResourceManagerImpl proxy;
	ServerSocket serverSocket;

	int port;

	public Server(int port) throws IOException {
		this.port = port;
		proxy = new ResourceManagerImpl();
		serverSocket = new ServerSocket(port);
	}

	public void run() {
		while(true){
			try {
				new ServerThread(serverSocket.accept(),proxy).run();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}