package server;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class RMPersistence {

    private String recordLocation,masterRecordPath,tablePath1,tablePath2, redoInfoPath;
    private boolean testShadowing = false;

    public RMPersistence(String rmType) throws IOException {
        masterRecordPath = "./record/" + rmType + "_master.txt";
        
        File dir = new File("./record/");
        if(!dir.exists()){
        	dir.mkdir();
        }
        
        File f = new File(masterRecordPath);
        if (!f.exists()){
            FileWriter fileWriter = new FileWriter(f, false);
            String initialMaster = "1";
            fileWriter.write(initialMaster);
            fileWriter.close();
        }

        tablePath1 = "./record/" + rmType + "_table1.dat";
        tablePath2 = "./record/" + rmType + "_table2.dat";
        redoInfoPath = "./record/"  + rmType + "_redoInfo.dat";

        recordLocation =  "./record/" + rmType + "_record.dat";

        f = new File(recordLocation);
        if (!f.exists()){
            HashMap<Integer,Set<String>> emptyRecord = new HashMap<>();
            writeObjectToPath(emptyRecord,recordLocation);
        }
        f = new File(tablePath1);
        if(!f.exists()){
        	RMHashtable table = new RMHashtable();
        	writeObjectToPath(table,tablePath1);
        }
        f = new File(tablePath2);
        if(!f.exists()){
        	RMHashtable table = new RMHashtable();
        	writeObjectToPath(table,tablePath2);
        }
        

    }

    // don't need synchronized because it's called in constructor only
    public int redoLastCommit() throws IOException, ClassNotFoundException {
        HashMap<Integer, Set<String>> txnRecords =  (HashMap<Integer, Set<String>>) readObjectFromPath(recordLocation);
        int txnToRedo = -1; // -1 means no txn needs undo
        for (int id : txnRecords.keySet()){
            Set<String> records = txnRecords.get(id);
            if (!records.contains("commit") && records.contains("yes"))
                txnToRedo = id;
            // abort any transactions that haven't voted
            else if(!records.contains("commit") && !records.contains("abort"))
                records.add("abort");
        }

        Map<String, RMItem> changeToRedo = loadRedoCommitInfo();
        boolean redoSucceed = true;
        if (!changeToRedo.isEmpty()){
            System.out.println("Going to redo: " + changeToRedo.toString());
            redoSucceed = applyChangeToShadow(changeToRedo);
            System.out.println("Redo succeed: "+ redoSucceed);
            BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
            String master = br.readLine();
            String shadowCopyPath = master.contains("1")? tablePath2 : tablePath1;
            RMHashtable tab = (RMHashtable) readObjectFromPath(shadowCopyPath);
            System.out.println("Shadow copy: " + tab.toString());
        }

        if (!redoSucceed) {
            txnRecords.get(txnToRedo).add("abort");
            txnToRedo = -1;
        }

        writeObjectToPath(txnRecords, recordLocation);
        return txnToRedo;
    }

    public synchronized boolean saveTxnRecord(int xid, String record) {

        try{

            HashMap<Integer,Set<String>> savedRecords = (HashMap<Integer, Set<String>>) readObjectFromPath(recordLocation);

            if (savedRecords.containsKey(xid))
                savedRecords.get(xid).add(record);
            else{
                Set<String> recordSet = new HashSet<>();
                recordSet.add(record);
                savedRecords.put(xid,recordSet);
            }

            writeObjectToPath(savedRecords,recordLocation);
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public Set<String> loadTxnRecord(int xid){
        HashMap<Integer,Set<String>> savedRecords;
        try {
            savedRecords = (HashMap<Integer, Set<String>>) readObjectFromPath(recordLocation);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashSet<>();
        }

        if (savedRecords.containsKey(xid))
            return savedRecords.get(xid);
        else
            return new HashSet<>();
    }


    public synchronized boolean saveRedoCommitInfo(Map<String,RMItem> redoInfo)  {
        try{
            writeObjectToPath(redoInfo,redoInfoPath);

        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;

    }

    private Map<String, RMItem> loadRedoCommitInfo(){
        File f = new File(redoInfoPath);
        Map<String,RMItem> redoInfo;
        if (f.exists()){
            try {
                redoInfo = (Map<String, RMItem>) readObjectFromPath(redoInfoPath);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                redoInfo = new HashMap<>();
            }
        }
        else
            redoInfo = new HashMap<>();

        return redoInfo;
    }

    public synchronized boolean applyChangeToShadow(Map<String, RMItem> changeToApply){
        if (changeToApply.size()>0)
            try {
                BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
                String master = br.readLine();
                String shadowCopyPath = master.contains("1")? tablePath2 : tablePath1;
                RMHashtable table = (RMHashtable) readObjectFromPath(shadowCopyPath);
                for (String key : changeToApply.keySet()){
                    if (changeToApply.get(key) == null) {
                        table.remove(key);
                    }
                    else {
                        table.put(key,changeToApply.get(key));
                    }
                }
                writeObjectToPath(table,shadowCopyPath);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        return true;
    }

    public synchronized boolean changeShadowPointer(){
       try{
           BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
           String master = br.readLine();
           File masterRecord = new File(masterRecordPath);
           FileWriter fileWriter = new FileWriter(masterRecord, false);
           fileWriter.write(master.contains("1")? "2" : "1");
           fileWriter.close();
       }catch (IOException e){
           return false;
       }
        return true;
    }

    public synchronized boolean prepareFreshShadowCopy() {
        try{
            BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
            String master = br.readLine();
            String committedTablePath, shadowTablePath;
            committedTablePath = master.contains("1")? tablePath1 : tablePath2;
            shadowTablePath = master.contains("1")? tablePath2 : tablePath1;

            RMHashtable committedTable = (RMHashtable) readObjectFromPath(committedTablePath);
            writeObjectToPath(committedTable, shadowTablePath);
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public RMHashtable recoverRMTable() throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
        String master = br.readLine();

        String pathToReadTable = master.contains("1")? tablePath1 : tablePath2;

        File f = new File(pathToReadTable);
        RMHashtable table;
        if (f.exists()){
            table = (RMHashtable) readObjectFromPath(pathToReadTable);
        }
        else
            table = new RMHashtable();


        return table;
    }

    public void setTestShadowing(){
        testShadowing = true;
    }


    private synchronized void writeObjectToPath(Object object, String path) throws IOException {
        FileOutputStream saveFile = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(saveFile);
        out.writeObject(object);
        out.close();
    }

    private Object readObjectFromPath(String path) throws IOException, ClassNotFoundException {
        FileInputStream inputStream = new FileInputStream(path);
        ObjectInputStream in = new ObjectInputStream(inputStream);
        return in.readObject();
    }
    
    public static void deleteAllRecords() {
    	 File dir = new File("./record/");
    	 deleteFolder(dir);
    }
    
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
