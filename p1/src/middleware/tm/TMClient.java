package middleware.tm;

import server.tm.TMServer;
import middleware.client.MWClient;

public class TMClient {

	private static TMClient theInstance;
	private static Object lock = new Object();


	MWClient carClient;
	MWClient flightClient;
	MWClient roomClient;
	MWClient custClient;
	
	public void setClients(MWClient carClient, MWClient flightClient,
			MWClient roomClient, MWClient custClient){
		this.carClient = carClient;
		this.flightClient = flightClient;
		this.roomClient = roomClient;
		this.custClient = custClient;
	}
	
	public static TMClient getInstance(){
		synchronized(lock){
			if(theInstance == null){
				theInstance = new TMClient();
			}
			return theInstance;
		}
	}
	
	private TMClient(){
		
	}
	
	
	public int start(){
		
		
		return 0;
	}
	
	public boolean commit(int id){
		
		
		return false;
	}
	
	public boolean abort(int id){
		
		return false;
	}
	
	public boolean shutDown(){
		
		
		return false;
	}
	
	
}
