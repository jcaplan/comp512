package server;

import server.tm.WriteList;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class RMPersistence {

    private String rmType;
    private boolean testShadowing = false;
    private HashMap<Integer,Set<String>> txnRecords;
    private String recordLocation,masterRecordPath,tablePath1,tablePath2,txnWriteListPath1,txnWriteListPath2;

    public RMPersistence(String rmType) throws IOException {
        this.rmType = rmType;
        txnRecords = new HashMap<>();
        masterRecordPath = "./" + rmType + "_master.txt";

        File f = new File(masterRecordPath);
        if (!f.exists()){
            FileWriter fileWriter = new FileWriter(f, false);
            String initialMaster = "1";
            fileWriter.write(initialMaster);
            fileWriter.close();
        }

        tablePath1 = "./" + rmType + "_table1.dat";
        tablePath2 = "./" + rmType + "_table2.dat";
        txnWriteListPath1 = "./" + rmType + "_writeList1.dat";
        txnWriteListPath2 = "./" + rmType + "_writeList2.dat";

        recordLocation =  "./" + rmType + "_record.dat";

        f = new File(recordLocation);
        if (!f.exists()){
            HashMap<Integer,Set<String>> emptyRecord = new HashMap<>();
            FileOutputStream saveFile = new FileOutputStream(recordLocation);
            ObjectOutputStream out = new ObjectOutputStream(saveFile);
            out.writeObject(emptyRecord);
            out.close();
        }

    }

    public Set<String> loadTxnRecord(int xid) throws IOException, ClassNotFoundException {
        String recordLocation =  "./" + rmType + "_record.dat";
        FileInputStream inputStream = new FileInputStream(recordLocation);
        ObjectInputStream in = new ObjectInputStream(inputStream);
        HashMap<Integer,Set<String>> savedRecords = (HashMap<Integer,Set<String>>)in.readObject();
        in.close();

        if (savedRecords.containsKey(xid))
            return savedRecords.get(xid);
        else
            return new HashSet<>();
    }

    public synchronized void saveTxnRecord(int xid, String record) throws IOException, ClassNotFoundException {

        FileInputStream inputStream = new FileInputStream(recordLocation);
        ObjectInputStream in = new ObjectInputStream(inputStream);
        HashMap<Integer,Set<String>> savedRecords = (HashMap<Integer,Set<String>>)in.readObject();
        in.close();

        if (savedRecords.containsKey(xid))
            savedRecords.get(xid).add(record);
        else{
            Set<String> recordSet = new HashSet<>();
            recordSet.add(record);
            savedRecords.put(xid,recordSet);
        }

        FileOutputStream saveFile = new FileOutputStream(recordLocation);
        ObjectOutputStream out = new ObjectOutputStream(saveFile);
        out.writeObject(savedRecords);
        out.close();

    }

    public synchronized void saveTxnSnapshot(int xid, WriteList writeList, RMHashtable table) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
        String master = br.readLine();
        String pathToSaveTable, pathToSaveWriteList, pathToReadWriteList;
        if (master.contains("1")){
            pathToSaveTable = tablePath2;
            pathToSaveWriteList = txnWriteListPath2;
            pathToReadWriteList = txnWriteListPath1;
        }
        else {
            pathToSaveTable = tablePath1;
            pathToSaveWriteList = txnWriteListPath1;
            pathToReadWriteList = txnWriteListPath2;
        }

        File f = new File(pathToReadWriteList);
        HashMap<Integer, WriteList> txnWriteList;
        if (f.exists()) {
            FileInputStream inputStream = new FileInputStream(pathToReadWriteList);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            txnWriteList = (HashMap<Integer, WriteList>) in.readObject();
            in.close();
        }
        else
            txnWriteList = new HashMap<>();

        txnWriteList.put(xid,writeList);
        FileOutputStream saveFile = new FileOutputStream(pathToSaveWriteList);
        ObjectOutputStream out = new ObjectOutputStream(saveFile);
        out.writeObject(txnWriteList);
        out.close();

        if (testShadowing)
            return;

        saveFile = new FileOutputStream(pathToSaveTable);
        out = new ObjectOutputStream(saveFile);
        out.writeObject(table);
        out.close();

        //reset master record
        File masterRecord = new File(masterRecordPath);
        FileWriter fileWriter = new FileWriter(masterRecord, false);
        fileWriter.write(master.contains("1")? "2" : "1");
        fileWriter.close();

    }

    public HashMap<Integer,WriteList> recoverAllWriteList() throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
        String master = br.readLine();

        String pathToReadWriteList = master.contains("1")? txnWriteListPath1 :txnWriteListPath2;

        File f = new File(pathToReadWriteList);
        HashMap<Integer,WriteList> txnWriteList;
        if (f.exists()){
            FileInputStream inputStream = new FileInputStream(pathToReadWriteList);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            txnWriteList = (HashMap<Integer,WriteList>)in.readObject();
            in.close();
        }
        else
            txnWriteList = new HashMap<>();

        return txnWriteList;
    }

    public RMHashtable recoverRMTable() throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
        String master = br.readLine();

        String pathToReadTable = master.contains("1")? tablePath1 : tablePath2;

        File f = new File(pathToReadTable);
        RMHashtable table;
        if (f.exists()){
            FileInputStream inputStream = new FileInputStream(pathToReadTable);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            table = (RMHashtable)in.readObject();
        }
        else
            table = new RMHashtable();


        return table;
    }

    public void setTestShadowing(){
        this.testShadowing = true;
    }
}
