package client;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultipleClientsPerformanceTest {

    private final int TOTAL_TRANSACTIONS = 20;
    private final int MIN_CLIENT_NUMBER = 10;
    private final int MAX_CLIENT_NUMBER = 12;
    private final int MIN_TPS = 300;
    private final int MAX_TPS = 305;
    private String serviceName, serviceHost;
    private int servicePort;
    private String[] txnTemplateAllRm = {"newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true","newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
            "itinerary,TxnId,customerId,0,location,true,true"};


    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: MultipleClientsPerformanceTest <service-name> "
                    + "<service-host> <service-port>");
            System.exit(-1);
        }

        String serviceName = args[0];
        String serviceHost = args[1];
        int servicePort = Integer.parseInt(args[2]);

        MultipleClientsPerformanceTest test = new MultipleClientsPerformanceTest(serviceName, serviceHost, servicePort);
        test.runTest();


    }
    public MultipleClientsPerformanceTest(String serviceName, String serviceHost, int servicePort){
        this.serviceName = serviceName;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
    }

    public void runTest() throws Exception {

        ExecutorService pool;
        long waitTime;

        File file =new File("performance_" + System.currentTimeMillis() + ".csv");
        if(!file.exists()){
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file.getName(),true);
        BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

        bufferWriter.write(" ,");
        for (int i=MIN_TPS;i<=MAX_TPS;i++)
            bufferWriter.write(i + ",");
        bufferWriter.newLine();
        bufferWriter.close();

        for (int clientNumber = MIN_CLIENT_NUMBER;clientNumber <= MAX_CLIENT_NUMBER; clientNumber++){
            List<Long> responseList = new LinkedList<>();

            for (int tps = MIN_TPS;tps <= MAX_TPS;tps++){
                waitTime = calculateWaitTime(clientNumber,tps);
                pool = Executors.newFixedThreadPool(clientNumber);
                List<Future<Long>> futureList = new LinkedList<>();

                for (int i=1;i<=clientNumber;i++){
                    Callable<Long> clientThread = new PerformanceTestThread(new Client(serviceName,serviceHost,servicePort),
                            waitTime, TOTAL_TRANSACTIONS/clientNumber + 1, txnTemplateAllRm);
                    Future<Long> future = pool.submit(clientThread);
                    futureList.add(future);
                }

                long sum = 0;
                for (Future<Long> future : futureList)
                    sum += future.get();
                responseList.add(sum/clientNumber);
                pool.shutdown();
            }
            saveDataToFile(responseList, file, clientNumber);
        }
    }

    private void saveDataToFile(List<Long> resultList, File file, int numberOfClients) throws IOException {
        if(!file.exists()){
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file.getName(),true);
        BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

        bufferWriter.write(numberOfClients + ",");
        for (Long loadResponse : resultList)
            bufferWriter.write(loadResponse.toString() + ",");
        bufferWriter.newLine();
        bufferWriter.close();
    }

    private long calculateWaitTime(int clientNumber, int tps){
        return (long) ((1000.0/tps)*clientNumber);
    }


}

