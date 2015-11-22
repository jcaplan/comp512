package server;

import server.tm.WriteList;

import java.util.HashMap;
import java.util.Set;


public class RMPersistence {
    // TODO implement
    private String rmType;
    private boolean testShadowing = false;

    public RMPersistence(String rmType){
        this.rmType = rmType;
    }

    public Set<String> loadTxnRecord(int xid){
        return null;
    }

    public void saveTxnRecord(int xid, String record){

    }

    public void saveTxnSnapshot(int xid, WriteList writeList, RMHashtable table){

    }

    public HashMap<Integer,WriteList> recoverAllWriteList(){
        return null;
    }

    public RMHashtable recoverRMTable(){
        return null;
    }

    public void setTestShadowing(){
        this.testShadowing = true;
    }
}
