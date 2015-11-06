package middleware.tm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import middleware.client.MWClient;

public class TMClient {

	
	HashMap<Integer, HashSet<MWClient>> rmList;
	
	
	
	private static TMClient theInstance;
	private static Object lock = new Object();


	MWClient carClient;
	MWClient flightClient;
	MWClient roomClient;
	MWClient custClient;
	
	int idCounter = 1;
	
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
		int id;
		synchronized(lock){
			id = idCounter++;
			rmList.put(id, new HashSet<>());
		}
		
		
		
		return id;
	}
	
	public boolean commit(int id){
		
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		for(MWClient rm : rms){
			rm.commit(id);
		}
		
		return false;
	}
	
	public boolean abort(int id){
		
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		for(MWClient rm: rms){
			rm.abort(id);
		}
		
		return false;
	}
	
	public boolean shutDown(){
		
		
		return false;
	}
	
	public boolean enlistCarRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		

		rms.add(carClient);
		return true;
	}
	
	public boolean enlistFlightRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		rms.add(flightClient);
		return true;
	}
	
	public boolean enlistRoomRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}

		rms.add(roomClient);
		return true;
	}
	
	public boolean enlistCustomerRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		


		rms.add(custClient);
		return true;
	}
	
	
}
