package server.tm;

import java.util.HashMap;

import server.RMHashtable;
import server.RMItem;

public class TMServer {

	private HashMap<Integer,WriteList> txnWriteList;

	
	public TMServer(){
		txnWriteList = new HashMap<>();
	}

	public boolean writeData(int id, String key, RMItem value) {
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
			return true;
		}
	}
	
	
	

	public boolean removeData(int id, String key, RMItem value) {
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
		
		txnWriteList.remove(id);
		return true;
	}
	
	public synchronized boolean abortTxn(int id, RMHashtable table){
		System.out.println(">>>>>>>>>>>>>>>>>TMServer::abort txn" + id);
		WriteList writeList = txnWriteList.get(id);
		
		if(writeList == null){
			return false;
		}
		
		
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
	
}
