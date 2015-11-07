package test;

import lockmanager.DeadlockException;
import client.*;

public class ClientTestThread extends Thread{
	
	
	Client client;
	public String clientId;
	public ClientTestThread(Client client) {
		this.client = client;
	}

	public void run() {
		int txnId;
		try{
			txnId = Integer.parseInt(client.handleRequest("start"));
			this.clientId = client.handleRequest("newcustomer," + txnId);
			client.handleRequest(String.format("commit,%d,",txnId));
		} catch (Exception e){
			e.printStackTrace();
			return;
		}
		
		while (true){
			
			long start = System.currentTimeMillis();
			try{
				txnId = Integer.parseInt(client.handleRequest("start"));
				boolean result = client.handleRequest("itinerary," + txnId + ","+clientId +",0,0,true,true").equals("true");
				client.handleRequest(String.format("commit,%d,",txnId));
				if(!result){
					break;
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		
			long end = System.currentTimeMillis();

			System.out.println("THREAD_" + Thread.currentThread().getId() + "::response time: " + (end - start));

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		


		
	}


}
