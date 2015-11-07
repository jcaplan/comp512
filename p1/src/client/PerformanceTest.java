package client;


import server.Trace;

import java.util.LinkedList;
import java.util.List;

public class PerformanceTest{

    public static final int NUM_THREADS = 1;

    public static void main(String[] args) throws Exception {
        System.out.println("haha");
        if (args.length != 3) {
            System.out.println("Usage: MyClient <service-name> "
                    + "<service-host> <service-port>");
            System.exit(-1);
        }

        String serviceName = args[0];
        String serviceHost = args[1];
        int servicePort = Integer.parseInt(args[2]);

        PerformanceTestThread[] testThreads = new PerformanceTestThread[NUM_THREADS];

        PerformanceTest test = new PerformanceTest();
        for (int i = 0; i < NUM_THREADS; i++) {
            testThreads[i] = test.new PerformanceTestThread(new Client(serviceName, serviceHost, servicePort),false, 0);
            testThreads[i].start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            testThreads[i].join();

        }
    }

    class PerformanceTestThread extends Thread {
        Client client;
        boolean needSleep;
        int timesOfTXnPerSecond;

        public PerformanceTestThread(Client client, boolean needSleep, int timesOfTXnPerSecond) throws Exception {
            this.client = client;
            this.needSleep = needSleep;
            this.timesOfTXnPerSecond = timesOfTXnPerSecond;
        }

        public void run(){
            try {
                testTransactionSingleRM();
            } catch (DeadlockException_Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void testTransactionSingleRM() throws DeadlockException_Exception, InterruptedException {
            List<String> carTxn, roomTxn, flightTxn;
            String addCar = "newcar,TxnId,0,1,10";
            String queryCarPrice = "querycarprice,TxnId,0";
            carTxn = new LinkedList<>();
            for(int i = 0;i<100;i++)
                carTxn.add(addCar);
            for (int i = 0;i<100;i++)
                carTxn.add(queryCarPrice);

            roomTxn = new LinkedList<>();
            String addRoom = "newroom,TxnId,0,1,10";
            String queryRoomPrice = "queryroomprice,TxnId,0";
            for(int i = 0;i<100;i++)
                roomTxn.add(addRoom);
            for (int i = 0;i<100;i++)
                roomTxn.add(queryRoomPrice);

            List<List<String>> transactionList = new LinkedList<>();
            transactionList.add(carTxn);
            transactionList.add(roomTxn);

            long threadId = Thread.currentThread().getId();
            System.out.println("[Thread:" + threadId + "] Response time with Txns involving single RM:"  + (executeTxns(transactionList)));

        }

        public void testTransactionAllRM()throws DeadlockException_Exception, InterruptedException {

        }

        private List<Long> executeTxns(List<List<String>> transactionList) throws InterruptedException, DeadlockException_Exception {
            long begin,end;
            LinkedList<Long> response = new LinkedList<>();
            for (List<String> transaction : transactionList){
                begin = System.currentTimeMillis();
                String TxnId = null;

                TxnId = client.handleRequest("start");

            /*assume all operations in one Txn is done for only one client*/
                String customerId = null;
                customerId = client.handleRequest("newcustomer," + TxnId);
                for (String operation : transaction){
                    try {
                        client.handleRequest(operation.replace("TxnId", TxnId).replace("customerId", customerId));
                    } catch (DeadlockException_Exception e) {
                        Trace.error("Transaction aborted due to deadlock, Txn id: " + TxnId + " operation: " + operation);
                        continue;
                    }
                }
                client.handleRequest("commit," + TxnId);
                end = System.currentTimeMillis();
                response.add(end - begin);
                if (needSleep){
                    double minSleepTime = (1000.0/timesOfTXnPerSecond)*(2.0/3);
                    long sleepTime = (long)(minSleepTime + Math.random()*minSleepTime);
                    Thread.sleep(sleepTime);
                }
            }
            return response;
        }
    }
}

