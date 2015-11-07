package client;


import lockmanager.DeadlockException;
import test.ClientTestThread;
import client.*;
//Test 01 : multithreaded client

public class TestDeadlock {
	static Client setupClient;
	public static final int NUM_THREADS = 5;

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.out.println("Usage: MyClient <service-name> "
					+ "<service-host> <service-port>");
			System.exit(-1);
		}

		// parse inputs

		String serviceName = args[0];
		String serviceHost = args[1];
		int servicePort = Integer.parseInt(args[2]);

		// set up servers

		setupClient = new Client(serviceName, serviceHost, servicePort);
		String txnId = setupClient.handleRequest("start");
		setupClient.handleRequest(String.format("newcar,%s,0,100,100",txnId));
		setupClient.handleRequest(String.format("newflight,%s,0,100,200",txnId));
		setupClient.handleRequest(String.format("newroom,%s,0,100,300",txnId));
		setupClient.handleRequest("commit,"+txnId);

		
		Thread1 t1 = new Thread1();
		Thread2 t2 = new Thread2();
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		

	}
	
	
	static class Thread1 extends Thread {
		
		public void run(){
			try {
				String txnId = setupClient.handleRequest("start");
				setupClient.handleRequest(String.format("querycar,%s,0", txnId));
				Thread.sleep(2000);
				System.out.println("hihi");
				setupClient.handleRequest("deleteroom," + txnId + ",0");
				setupClient.handleRequest("commit," + txnId);
			} catch (DeadlockException e) {
				System.out.println("DEADLOCK1!");
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	static class Thread2 extends Thread {
		
		public void run(){
			try {
				String txnId = setupClient.handleRequest("start");
				setupClient.handleRequest(String.format("queryroom,%s,0", txnId));
				Thread.sleep(3000);
				System.out.println("hihi");
				setupClient.handleRequest("deletecar," + txnId + ",0");
				setupClient.handleRequest("commit," + txnId);
			} catch (DeadlockException e) {
				System.out.println("DEADLOCK2!");
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
