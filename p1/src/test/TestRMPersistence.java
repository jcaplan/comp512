package test;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import server.Car;
import server.RMHashtable;
import server.RMPersistence;
import server.tm.WriteList;

import java.util.HashMap;
import java.util.Set;

public class TestRMPersistence {
    private RMPersistence persistence;
    private String rmType = "car";

    Car car1,car2;
    WriteList writeList;
    RMHashtable table;

    @Before
    public void setup(){
        persistence = new RMPersistence(rmType);
        car1 = new Car(Car.getKey("MONTREAL"),1,10);
        car2 = new Car(Car.getKey("MONTREAL"),2,10);

        writeList = new WriteList();
        writeList.writeItem(car1.getKey(),car1);

        table = new RMHashtable();
        table.put(car2.getKey(), car2);

    }

    @Test
    public void testTxnRecordPersistence(){
        int xid = 1;
        String record = "abort";
        persistence.saveTxnRecord(xid, record);

        persistence = new RMPersistence(rmType);
        Set<String> recoveredRecord = persistence.loadTxnRecord(xid);

        assertTrue(recoveredRecord.size() == 1);
        assertTrue(recoveredRecord.contains(record));
    }

    @Test
    public void testSaveTxnSnapshot(){


        int xid = 1;
        persistence.saveTxnSnapshot(xid,writeList, table);

        persistence = new RMPersistence(rmType);

        HashMap<Integer,WriteList> recoveredTxnWriteList = persistence.recoverAllWriteList();
        RMHashtable recoveredTable = persistence.recoverRMTable();

        assertTrue(recoveredTxnWriteList.size()==1);
        assertTrue(recoveredTxnWriteList.containsKey(xid));
        assertEquals(car1, recoveredTxnWriteList.get(xid).getItem(car1.getKey()));
        assertTrue(recoveredTable.size()==1);
        assertEquals(car2,recoveredTable.get(car2.getKey()));

    }

    @Test
    public void testShadowing(){
        //simulate doing a partial write and crash
        int xid = 1;
        persistence.saveTxnSnapshot(xid, writeList, table);
        persistence.setTestShadowing();

        Car car3 = new Car(Car.getKey("MONTREAL"),10,20);
        Car car4 = new Car(Car.getKey("MONTREAL"),20,20);
        writeList.writeItem(car3.getKey(),car3);
        table.put(car4.getKey(),car4);

        persistence.saveTxnSnapshot(xid,writeList,table);

        HashMap<Integer,WriteList> recoveredTxnWriteList = persistence.recoverAllWriteList();
        RMHashtable recoveredTable = persistence.recoverRMTable();
        //verify partially written data are not returned
        assertTrue(recoveredTxnWriteList.size()==1);
        assertTrue(recoveredTxnWriteList.containsKey(xid));
        assertEquals(car1, recoveredTxnWriteList.get(xid).getItem(car1.getKey()));
        assertTrue(recoveredTable.size()==1);
        assertEquals(car2,recoveredTable.get(car2.getKey()));
    }

}
