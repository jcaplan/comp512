package server.tm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import server.RMHashtable;
import server.RMItem;
import server.RMPersistence;

public class TMServer {

    //txnWriteList store keys of items that a txn has written
	private HashMap<Integer,Set<String>> txnWriteList;
    private RMPersistence rmPersistence;
    private Set<Integer> activeTnxs;
    private static int TIMEOUT_DELAY = 60000;
    HashMap<Integer, TimerTask> timerList;
    Timer timer;
    private RMHashtable table;
    private Map<Integer, Map<String, RMItem>> redoCommitInfo;
    String type;
	public TMServer(String rmType) throws IOException, ClassNotFoundException {
		txnWriteList = new HashMap<>();
        rmPersistence = new RMPersistence(rmType);
        type = rmType;
        // only transactions with yes vote but no commit/abort are loaded
        activeTnxs = rmPersistence.loadAllActiveTxns();
        timerList = new HashMap<>();
        timer = new Timer();
        this.table = rmPersistence.recoverRMTable();
        redoCommitInfo = rmPersistence.loadRedoCommitInfo();

	}

    public RMHashtable getCommittedTable(){
        return this.table;
    }

    public boolean isTxnActive(int id){
        return activeTnxs.contains(id);
    }

    public boolean prepareCommit(int id){
        if (!isTxnActive(id))
            return false;
        Map<String,RMItem> redoInfo = new HashMap<>();

        for (String k : txnWriteList.get(id))
            redoInfo.put(k, (RMItem) table.get(k));
        boolean saveSuccess = rmPersistence.saveRedoCommitInfo(id,redoInfo) && rmPersistence.saveTxnRecord(id, "yes");

        removeTimerTask(id);
        System.out.println(type + "::" + " vote received for txn " + id + ", wait indefinitely for result");

        return saveSuccess;
    }

	public void modifyData(int id, String key) {
        resetTimer(id);
        if (txnWriteList.containsKey(id))
		    txnWriteList.get(id).add(key);
	}

	public synchronized boolean start(int id){
		System.out.println(type + "::" + "start txn " + id);
		if(isTxnActive(id)){
			System.out.println(type + "::" + "transaction already running");
			System.out.println(activeTnxs);
			return false;
		} else {
			txnWriteList.put(id, new HashSet<String>());
            activeTnxs.add(id);
            rmPersistence.saveTxnRecord(id,"start");
            TimerTask abortTxn = new AbortTxn(id);
            timerList.put(id, abortTxn);
            timer.schedule(abortTxn, TIMEOUT_DELAY);
			return true;
		}
	}
	

	
	public synchronized boolean commitTxn(int id){
		System.out.println(type + "::" + "commit txn" + id);
		if(!isTxnActive(id)){
			return false;
		}
        removeTimerTask(id);

        Map<String, RMItem> changeToApply;
        if (redoCommitInfo.containsKey(id)){
            changeToApply = redoCommitInfo.get(id);
        }
        else{
            changeToApply = new HashMap<>();
            for (String key : txnWriteList.get(id))
                changeToApply.put(key, (RMItem) table.get(key));
        }

        boolean applySuccessful = rmPersistence.applyChangeToStorage(changeToApply);
        if (!applySuccessful) {
        	System.out.println(type + "::" + "applyChangeToStorage failed");
            return false;
        }
        txnWriteList.remove(id);
        activeTnxs.remove(id);
        System.out.println("active: " + activeTnxs);
        return rmPersistence.saveTxnRecord(id,"commit");
	}

	public synchronized boolean abortTxn(int id){
		System.out.println(">>>>>>>>>>>>>>>>>" + type + "::abort txn" + id);
        removeTimerTask(id);
        RMHashtable lastCommittedTable;
        try {
            lastCommittedTable = rmPersistence.recoverRMTable();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        for (String key : txnWriteList.get(id)){
            if (lastCommittedTable.containsKey(key))
                table.put(key,lastCommittedTable.get(key));
            else
                table.remove(key);
        }
        txnWriteList.remove(id);
        activeTnxs.remove(id);
        redoCommitInfo.remove(id);
        return rmPersistence.saveTxnRecord(id,"abort");
	}


	private class AbortTxn extends TimerTask {
		int id;
		
		private AbortTxn(int id){
			this.id = id;
		}

		@Override
		public void run() {
			abortTxn(id);
			System.err.println(type + ":: " + System.currentTimeMillis() + ":Txn #" + id  + ": timeout!!");
		}

	}


	private void resetTimer(int id) {
		timerList.get(id).cancel();
		TimerTask abortTxn = new AbortTxn(id);
		timerList.put(id, abortTxn);
		System.out.println(type + "::" + "new timer");
		timer.schedule(abortTxn, TIMEOUT_DELAY);
	}
	
	private void removeTimerTask(int id) {
		TimerTask t = timerList.get(id);
		if(t != null){
			t.cancel();
			timerList.remove(id);
		}	
	}
	public void setTimeout(int timeout){
		TIMEOUT_DELAY = timeout;
	}
}
