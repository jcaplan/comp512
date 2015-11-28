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

    }

    // don't need synchronized because it's called in constructor only
    public Set<Integer> loadAllActiveTxns() throws IOException, ClassNotFoundException {
        HashMap<Integer, Set<String>> txnRecords =  (HashMap<Integer, Set<String>>) readObjectFromPath(recordLocation);
        Set<Integer> activeTxns = new HashSet<>();
        for (int id : txnRecords.keySet()){
            Set<String> records = txnRecords.get(id);
            if (!records.contains("commit") && records.contains("yes"))
                activeTxns.add(id);
            // abort any transactions that haven't voted
            else if(!records.contains("commit") && !records.contains("abort"))
                records.add("abort");
        }
        writeObjectToPath(txnRecords, recordLocation);
        return activeTxns;
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

    public synchronized boolean removeTxnRecord(int xid){
        try{

            HashMap<Integer,Set<String>> savedRecords = (HashMap<Integer, Set<String>>) readObjectFromPath(recordLocation);

            savedRecords.remove(xid);
            writeObjectToPath(savedRecords,recordLocation);
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized boolean saveRedoCommitInfo(int xid, Map<String,RMItem> redoInfo)  {
        try{

            File f = new File(redoInfoPath);
            HashMap<Integer, Map<String,RMItem>> allRedoInfo;
            if (f.exists()) {
                allRedoInfo = (HashMap<Integer, Map<String, RMItem>>) readObjectFromPath(redoInfoPath);
            }
            else
                allRedoInfo = new HashMap<>();

            allRedoInfo.put(xid, redoInfo);
            writeObjectToPath(allRedoInfo,redoInfoPath);

        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public Map<Integer, Map<String, RMItem>> loadRedoCommitInfo(){
        File f = new File(redoInfoPath);
        HashMap<Integer, Map<String,RMItem>> allRedoInfo;
        if (f.exists())
            try {
                allRedoInfo = (HashMap<Integer, Map<String, RMItem>>) readObjectFromPath(redoInfoPath);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                allRedoInfo = new HashMap<>();
            }
        else
            allRedoInfo = new HashMap<>();

        return allRedoInfo;
    }

    public synchronized boolean applyChangeToStorage(Map<String,RMItem> changeToApply){
        try {
            RMHashtable table = recoverRMTable();
            for (String key : changeToApply.keySet()){
                if (changeToApply.get(key) == null)
                    table.remove(key);
                else
                    table.put(key,changeToApply.get(key));
            }

            if (testShadowing)
                return false;

            BufferedReader br = new BufferedReader(new FileReader(masterRecordPath));
            String master = br.readLine();
            writeObjectToPath(table,master.contains("1")? tablePath2 : tablePath1);

            File masterRecord = new File(masterRecordPath);
            FileWriter fileWriter = new FileWriter(masterRecord, false);
            fileWriter.write(master.contains("1")? "2" : "1");
            fileWriter.close();

        } catch (IOException | ClassNotFoundException e) {
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
