package client;



import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleClientPerformanceTest {


    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: SingleClientPerformanceTest <service-name> "
                    + "<service-host> <service-port>");
            System.exit(-1);
        }

        String serviceName = args[0];
        String serviceHost = args[1];
        int servicePort = Integer.parseInt(args[2]);

        ExecutorService pool;
        pool = Executors.newFixedThreadPool(2);

        final int TOTAL_TRANSACTIONS = 100;

        String[] txnTemplateSingleRm = {"newcar,TxnId,location,1,10","newcar,TxnId,location,1,10","newcar,TxnId,location,1,10",
                "deletecar,TxnId,location,1","deletecar,TxnId,location,1","deletecar,TxnId,location,1"};
        Callable<Long> singleRMCall = new PerformanceTestThread(new Client(serviceName,serviceHost,servicePort),
                0L, TOTAL_TRANSACTIONS, txnTemplateSingleRm);
        long singleRMResponseTime = pool.submit(singleRMCall).get();

        String[] txnTemplateAllRm = {"newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
                "deletecar,TxnId,location,1", "deleteroom,TxnId,location,1","deleteflight,TxnId,0"};
        Callable<Long> allRMCall = new PerformanceTestThread(new Client(serviceName,serviceHost,servicePort),
                0L, TOTAL_TRANSACTIONS, txnTemplateAllRm);
        long allRMResponseTime = pool.submit(allRMCall).get();

        System.out.println("Average response time for single RM transaction: " + Long.toString(singleRMResponseTime));
        System.out.println("Average response time for all RM transaction: " + Long.toString(allRMResponseTime));
    }
}
