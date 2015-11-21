package middleware.tm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lockmanager.LockManager;
import middleware.client.MWClient;
import middleware.client.MWClientInterface;

public class TMClient {

	private static final int TIMEOUT_DELAY = 15000;
	private static final int COMMIT_TIMEOUT_SECONDS = 10;
	HashMap<Integer, HashSet<MWClientInterface>> rmList;
	HashMap<Integer, TimerTask> timerList;
	Timer timer;
	
	private static TMClient theInstance;
	private static Object lock = new Object();
	

	MWClientInterface carClient;
	MWClientInterface flightClient;
	MWClientInterface roomClient;
	MWClientInterface custClient;

	int objectCount = 0;
	
	
	int idCounter = 1;

	private LockManager lockManager;
	
	public synchronized void setClients(MWClientInterface carClient, MWClientInterface flightClient,
			MWClientInterface roomClient, MWClientInterface custClient){
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
	
	
	public synchronized int start(){
		int id;
		synchronized(lock){
			id = idCounter++;
			rmList.put(id, new HashSet<>());
			TimerTask abortTxn = new AbortTxn(id);
			timerList.put(id, abortTxn);
			timer.schedule(abortTxn, TIMEOUT_DELAY);
		}
		
		

		System.out.println("TMClient::start = " + id);
		return id;
	}
	

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
	
	public synchronized boolean commit(int id){

		System.out.println("TMClient::commit txn" + id);
		HashSet<MWClientInterface> rms = rmList.get(id);
		
		if(rms == null){
			System.out.println("TMClient::txn id not found");
			return false;
		}
		

		boolean voteResult = prepareCommit(id,rms);
		if(voteResult){
			System.out.println("TMCLIENT:: final vote result: yes");
			for(MWClientInterface rm : rms){
				rm.commit(id);
				rmList.remove(id);
				timerList.get(id).cancel();
				timerList.remove(id);
				lockManager.UnlockAll(id);
				
			}
		} else {
			abort(id);
		}
		
		return voteResult;
	}
	
	private boolean prepareCommit(int id, HashSet<MWClientInterface> rms) {
		List<FutureTask<Boolean>> taskList = new ArrayList<>();

		 ExecutorService executor = Executors.newFixedThreadPool(rms.size());
		 
		//Create a future task for each RM vote
		for(MWClientInterface rm : rms){
			FutureTask<Boolean> vote = new FutureTask<Boolean>(new Callable<Boolean>() {
				@Override
				public Boolean call() {
					return rm.requestVote();
				}
			});
			taskList.add(vote);
			executor.execute(vote);
		}
		
		
		boolean result = true;
		for(FutureTask<Boolean> task : taskList){
			if(!result){
				task.cancel(true);
			} else {
				try {
					result &= task.get(COMMIT_TIMEOUT_SECONDS,TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException 
						| TimeoutException e) {
					result = false;
					e.printStackTrace();
				}
			}
		}

        executor.shutdown();
		return result;
	}

	public synchronized boolean abort(int id){
		
		System.out.println("TMClient::abort");
		HashSet<MWClientInterface> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		for(MWClientInterface rm: rms){
			rm.abort(id);
		}
		rmList.remove(id);
		TimerTask t = timerList.get(id);
		if(t != null){
			t.cancel();
			timerList.remove(id);
		}
		lockManager.UnlockAll(id);
		return false;
	}
	
	public synchronized boolean shutDown(){
		return false;
	}
	
	private void resetTimer(int id) {
		timerList.get(id).cancel();
		TimerTask abortTxn = new AbortTxn(id);
		timerList.put(id, abortTxn);
		System.out.println("TMCLIENT::new timer: " + ((AbortTxn) abortTxn).getCount());
		timer.schedule(abortTxn, TIMEOUT_DELAY);
	}
	
	
	public synchronized boolean enlistCarRM(int id){
		HashSet<MWClientInterface> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		resetTimer(id);
		if(!rms.contains(carClient)){
			carClient.start(id);
		}
		rms.add(carClient);
		return true;
	}
	

	public synchronized boolean enlistFlightRM(int id){
		HashSet<MWClientInterface> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		
		resetTimer(id);
		if(!rms.contains(flightClient)){
			flightClient.start(id);
		}
		rms.add(flightClient);
		return true;
	}
	
	public synchronized boolean enlistRoomRM(int id){
		HashSet<MWClientInterface> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}

		resetTimer(id);
		if(!rms.contains(roomClient)){
			roomClient.start(id);
		}
		rms.add(roomClient);
		return true;
	}
	
	public synchronized boolean enlistCustomerRM(int id){
		HashSet<MWClientInterface> rms = rmList.get(id);
		
		if(rms == null){
			return false;
		}
		

		resetTimer(id);
		if(!rms.contains(custClient)){
			custClient.start(id);
		}
		rms.add(custClient);
		return true;
	}

	public synchronized void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
		
	}
	
	public static void deleteInstance(){
		if(theInstance == null){
			return;
		}
		theInstance.rmList.clear();
		for(int id : theInstance.timerList.keySet()){
			TimerTask task = theInstance.timerList.get(id);
			task.cancel();
		}
		theInstance.timerList.clear();
		theInstance.timer.cancel();
		lock = new Object();
		theInstance.carClient = null;
		theInstance.flightClient = null;
		theInstance.roomClient = null;
		theInstance.custClient = null;
		theInstance.objectCount = 0;
		theInstance.idCounter = 1;
		theInstance.lockManager = null;
		theInstance  = null;
	}
	
}
