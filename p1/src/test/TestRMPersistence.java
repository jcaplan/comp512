package test;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.Car;
import server.RMHashtable;
import server.RMItem;
import server.RMPersistence;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestRMPersistence {
    private RMPersistence persistence;
    private String rmType = "car";

    Car car1,car2,car3;
    RMHashtable table;

    @Before
    public void setup() throws IOException {
        persistence = new RMPersistence(rmType);
        car1 = new Car(Car.getKey("MONTREAL"),1,1);
        car2 = new Car(Car.getKey("KINGSTON"),2,2);
        car3 = new Car(Car.getKey("TORONTO"),3,3);

        table = new RMHashtable();
        table.put(car1.getKey(), car1);

    }

    @After
    public void cleanup(){
        File dir = new File("./record/");
        deleteFolder(dir);
    }

    @Test
    public void testLoadAllActiveTxns() throws IOException, ClassNotFoundException {
        persistence.saveTxnRecord(1,"start");
        persistence.saveTxnRecord(2,"start");
        persistence.saveTxnRecord(2,"yes");
        persistence.saveTxnRecord(3,"start");
        persistence.saveTxnRecord(3,"yes");
        persistence.saveTxnRecord(3,"commit");
        persistence.saveTxnRecord(4,"start");
        persistence.saveTxnRecord(4,"abort");
        persistence.saveTxnRecord(5,"start");
        persistence.saveTxnRecord(5,"yes");

        Set<Integer> activeTxns = persistence.redoLastCommit();
        assertEquals(2,activeTxns.size());
        assertTrue(activeTxns.contains(2));
        assertTrue(activeTxns.contains(5));

        persistence = new RMPersistence(rmType);
        assertEquals(2,activeTxns.size());
        assertTrue(activeTxns.contains(2));
        assertTrue(activeTxns.contains(5));
    }

    @Test
    public void testRedoCommitInfo() throws IOException {
        Map<String, RMItem> redoInfo1 = new HashMap<>();
        Map<String, RMItem> redoInfo2 = new HashMap<>();

        redoInfo1.put(car1.getKey(),car1);
        redoInfo1.put(car2.getKey(),car2);
        redoInfo2.put(car3.getKey(),car3);

        int id1 = 1;
        int id2 = 2;
        persistence.saveRedoCommitInfo(1,redoInfo1);
        persistence.saveRedoCommitInfo(2,redoInfo2);

        persistence = new RMPersistence(rmType);
        Map<Integer, Map<String, RMItem>> redoCommitInfo = persistence.loadRedoCommitInfo();
        assertEquals(2,redoCommitInfo.size());
        assertEquals(2, redoCommitInfo.get(id1).size());
        assertEquals(1, redoCommitInfo.get(id2).size());
        assertEquals(car1.toString(),redoCommitInfo.get(id1).get(car1.getKey()).toString());
        assertEquals(car2.toString(),redoCommitInfo.get(id1).get(car2.getKey()).toString());
        assertEquals(car3.toString(),redoCommitInfo.get(id2).get(car3.getKey()).toString());

    }

    @Test
    public void testShadowing() throws IOException, ClassNotFoundException {

        Map<String,RMItem> changeToApply1 = new HashMap<>();
        changeToApply1.put(car1.getKey(),car1);
        Map<String,RMItem> changeToApply2 = new HashMap<>();
        persistence.applyChangeToShadow(changeToApply1);

        changeToApply2.put(car2.getKey(), car2);
        changeToApply2.put(car3.getKey(),car3);
        persistence.setTestShadowing();
        assertTrue(!persistence.applyChangeToShadow(changeToApply2));

        persistence = new RMPersistence(rmType);
        RMHashtable recoveredTable = persistence.recoverRMTable();
        assertEquals(1,recoveredTable.size());
        assertEquals(car1.toString(), recoveredTable.get(car1.getKey()).toString());

    }

    private  void deleteFolder(File folder) {
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
