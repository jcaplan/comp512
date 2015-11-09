package client;


import test.PerformanceTestThread;
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

    private final int TOTAL_TRANSACTIONS = 8*9*5*7; // divisible by any number from 2 to 10
    private final int MAX_CLIENT_NUMBER = 10;
    private final int MAX_TPS = 10;
    private String serviceName, serviceHost;
    private int servicePort;
    private String[] txnTemplateAllRm = {"newcar,TxnId,location,1,10", "newroom,TxnId,location,1,10", "newflight,TxnId,0,1,10",
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
        test.runTest(test.MAX_CLIENT_NUMBER,test.MAX_TPS);


    }
    public MultipleClientsPerformanceTest(String serviceName, String serviceHost, int servicePort){
        this.serviceName = serviceName;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
    }

    public void runTest(int maxClientNumber, int maxTps) throws Exception {

        ExecutorService pool;
        long waitTime;
        List<List<Long>> resultList = new LinkedList<>();
        for (int clientNumber = 1;clientNumber <= maxClientNumber; clientNumber++){
            List<Long> responseList = new LinkedList<>();

            for (int tps = 1;tps <= maxTps;tps++){
                waitTime = calculateWaitTime(clientNumber,tps);
                pool = Executors.newFixedThreadPool(clientNumber);
                List<Future<Long>> futureList = new LinkedList<>();

                for (int i=1;i<=clientNumber;i++){
                    Callable<Long> clientThread = new PerformanceTestThread(new Client(serviceName,serviceHost,servicePort),
                            waitTime, TOTAL_TRANSACTIONS/clientNumber, txnTemplateAllRm);
                    Future<Long> future = pool.submit(clientThread);
                    futureList.add(future);
                }

                long sum = 0;
                for (Future<Long> future : futureList)
                    sum += future.get();
                responseList.add(sum/clientNumber);
            }
            resultList.add(responseList);
        }

        saveDataToFile(resultList);
    }

    private void saveDataToFile(List<List<Long>> resultList) throws IOException {
        File file =new File("performance_" + System.currentTimeMillis() + ".csv");
        if(!file.exists()){
            file.createNewFile();
        }

        FileWriter fileWritter = new FileWriter(file.getName(),true);
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

        bufferWritter.write(" ,");
        int maxTps = resultList.get(0).size();
        for (int i=1;i<=maxTps;i++)
            bufferWritter.write(i + ",");
        bufferWritter.newLine();

        int numberOfClients = 1;
        for (List<Long> clientResponse : resultList){
            bufferWritter.write(numberOfClients + ",");
            for (Long loadResponse : clientResponse)
                bufferWritter.write(loadResponse.toString() + ",");
            bufferWritter.newLine();
            numberOfClients++;
        }

        bufferWritter.close();
    }

    private long calculateWaitTime(int clientNumber, int tps){
        return (long) ((1000.0/tps)*clientNumber);
    }


}

