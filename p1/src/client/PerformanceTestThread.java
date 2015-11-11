package client;

import server.Trace;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;


public class PerformanceTestThread implements Callable<Long>{
    private final double WAIT_TIME_VARIATION = 0.3;
    private Client client;
    private long timeInterval;
    private int numTransactions;
    private String[] transactionTemplate;

    public PerformanceTestThread(Client client, long timeInterval, int numTransactions, String[] txnTemplate) throws Exception {
        this.client = client;
        this.timeInterval = timeInterval;
        this.numTransactions = numTransactions;
        transactionTemplate = txnTemplate;
    }


    @Override
    public Long call() throws Exception {
        List<List<String>> transactionList = new LinkedList<>();
        for (int i = 0; i < numTransactions; i++){
            List<String> transaction = new LinkedList<>();
            for (int j = 0; j < transactionTemplate.length; j++){
                transaction.add(transactionTemplate[j]);
            }
            transactionList.add(transaction);
        }
        long begin,end;
        int executed = 0;
        long totalResponse = 0;
        for (List<String> transaction : transactionList){

            String TxnId,customerId;
                 /*assume all operations in one Txn is done for only one client*/

            TxnId = "1";
            begin = System.currentTimeMillis();
            boolean transactionCompleted = false;
            while (!transactionCompleted){
                TxnId = client.handleRequest("start");
                customerId = client.handleRequest("newcustomer," + TxnId);
                for (String operation : transaction){
                    try {
                        client.handleRequest(operation.replace("TxnId", TxnId).replace("customerId", customerId));
                        transactionCompleted = true;
                    } catch (Exception e) {
                        Trace.error("Transaction aborted due to deadlock, Txn id: " + TxnId + " operation: " + operation);
                        transactionCompleted = false;
                        break;
                    }
                }
            }

            client.handleRequest("commit," + TxnId);
            end = System.currentTimeMillis();

            totalResponse += (end - begin);
            executed++;

            long waitTime;
            if (timeInterval > 0L){
                waitTime = (long) (((1-WAIT_TIME_VARIATION)+ 2*WAIT_TIME_VARIATION*Math.random())*timeInterval);
                System.out.println("WILL SLEEP FOR :" + waitTime);
                Thread.sleep(waitTime);
            }
        }

        long averageResponse = totalResponse/executed;
        return averageResponse;
    }
}
