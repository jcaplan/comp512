package middleware.tm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import middleware.client.MWClient;

public class TMClient {

	private static final int TIMEOUT_DELAY = 2000;
	
	HashMap<Integer, HashSet<MWClient>> rmList;
	HashMap<Integer, TimerTask> timerList;
	Timer timer;
	
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
		rmList = new HashMap<>();
		timerList = new HashMap<>();
		timer = new Timer();
	}
	
	
	public int start(){
		int id;
		synchronized(lock){
			id = idCounter++;
			rmList.put(id, new HashSet<>());
			TimerTask abortTxn = new AbortTxn(id);
			timerList.put(id, abortTxn);
			timer.schedule(abortTxn, TIMEOUT_DELAY);
		}
		
		
		
		return id;
	}
	
	int objectCount = 0;
	private class AbortTxn extends TimerTask {
		int id;
		int count;
		
		private AbortTxn(int id){
			this.id = id;
			count = objectCount++;
		}

		@Override
		public void run() {
			abort(id);
			System.err.println(System.currentTimeMillis() + ":Txn #" + id + " : Task # " + count + ": timeout!!");
		}
		
		public int getCount(){
			return count;
		}
	}
	
	public boolean commit(int id){
		
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		for(MWClient rm : rms){
			rm.commit(id);
		}
		
		rmList.remove(id);
		timerList.get(id).cancel();
		timerList.remove(id);
		
		return false;
	}
	
	public boolean abort(int id){
		
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		for(MWClient rm: rms){
//			rm.abort(id);
		}
		rmList.remove(id);
		timerList.get(id).cancel();
		timerList.remove(id);
		return false;
	}
	
	public boolean shutDown(){
		
		
		return false;
	}
	
	private void resetTimer(int id) {
		TimerTask abortTxn = new AbortTxn(id);
		timerList.put(id, abortTxn);
		System.out.println("TMCLIENT::new timer: " + ((AbortTxn) abortTxn).getCount());
		timer.schedule(abortTxn, TIMEOUT_DELAY);
	}
	
	
	public boolean enlistCarRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		resetTimer(id);
		rms.add(carClient);
		return true;
	}
	

	public boolean enlistFlightRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		resetTimer(id);
		rms.add(flightClient);
		return true;
	}
	
	public boolean enlistRoomRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}

		resetTimer(id);
		rms.add(roomClient);
		return true;
	}
	
	public boolean enlistCustomerRM(int id){
		HashSet<MWClient> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		

		resetTimer(id);
		rms.add(custClient);
		return true;
	}
	
	
}
