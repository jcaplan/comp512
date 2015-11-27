package server.tm;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import server.RMHashtable;
import server.RMItem;

public class TMServer {

	private static final int TIMEOUT_DELAY = 1000;
	private HashMap<Integer,WriteList> txnWriteList;
	RMHashtable table;
	HashMap<Integer, TimerTask> timerList;
	Timer timer;

	public TMServer(){
		txnWriteList = new HashMap<>();
		timerList = new HashMap<>();
		timer = new Timer();
	}

	public boolean writeData(int id, String key, RMItem value) {
		resetTimer(id);
		
		WriteList writeList = txnWriteList.get(id);
		if(writeList == null){
			return false;
		}
		writeList.writeItem(key, value);
		return true;
	}

	public boolean start(int id){
		System.out.println("TMServer::start txn " + id);
		if(txnWriteList.containsKey(id)){
			return false;
		} else {
			txnWriteList.put(id, new WriteList());
			TimerTask abortTxn = new AbortTxn(id);
			timerList.put(id, abortTxn);
			timer.schedule(abortTxn, TIMEOUT_DELAY);
			return true;
		}
	}
	
	
	

	public boolean removeData(int id, String key, RMItem value) {
		resetTimer(id);
		
		WriteList writeList = txnWriteList.get(id);
		if(writeList == null){
			return false;
		}
		
		if(value != null){
			return writeList.writeItem(key, value);
		}
	
		return false;
	}
	
	public boolean commitTxn(int id){
		System.out.println("TMServer::commit txn" + id);
		if(!txnWriteList.containsKey(id)){
			return false;
		}
		removeTimerTask(id);
		txnWriteList.remove(id);
		return true;
	}
	

	public synchronized boolean abortTxn(int id){
		System.out.println(">>>>>>>>>>>>>>>>>TMServer::abort txn" + id);
		WriteList writeList = txnWriteList.get(id);
		
		if(writeList == null){
			return false;
		}
		

		removeTimerTask(id);
		HashMap<String,RMItem> map = writeList.writeList;
		for(String key : map.keySet()){
			if(map.get(key)!= null){
				table.put(key, map.get(key));
			} else {
				System.out.println("!!!!!!>>>>>>>!!!!!!TXN" + id + " removes " + key);
				table.remove(key);
			}
			
		}	
		txnWriteList.remove(id);
		return true;
	}

    public WriteList getLastCommittedVersionOfModifiedData(int id){
        return txnWriteList.get(id);
    }
	
	public void setTxnWriteList(HashMap<Integer,WriteList> recoveredTxnWriteList){
        this.txnWriteList = recoveredTxnWriteList;
    }
	
	
	private class AbortTxn extends TimerTask {
		int id;
		
		private AbortTxn(int id){
			this.id = id;
		}

		@Override
		public void run() {
			abortTxn(id);
			System.err.println(System.currentTimeMillis() + ":Txn #" + id  + ": timeout!!");
		}

	}


	public void setTable(RMHashtable m_itemHT) {
		table = m_itemHT;
	}
	
	private void resetTimer(int id) {
		timerList.get(id).cancel();
		TimerTask abortTxn = new AbortTxn(id);
		timerList.put(id, abortTxn);
		System.out.println("TMServer::new timer");
		timer.schedule(abortTxn, TIMEOUT_DELAY);
	}
	
	private void removeTimerTask(int id) {
		TimerTask t = timerList.get(id);
		if(t != null){
			t.cancel();
			timerList.remove(id);
		}	
	}

	public void requestVote(int id) {
		removeTimerTask(id);
		System.out.println("TMServer:: vote received for txn " + id + ", wait indefinitely for result");
	}
}
