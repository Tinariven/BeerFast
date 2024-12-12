package BeerFast;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import BeerFast.Report.Report;
import BeerFast.Report.ReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.couchbase.client.java.*;

public class BeerFast {

    private static final Logger logger = LogManager.getLogger(BeerFast.class);

    //TODO: load configuration from properties

    static String connectionString = "couchbase://[::1]";
    static String username = "Administrator";
    static String password = "Success";
    static String bucketName = "beer-sample";


    public static void main(String[] args) throws InterruptedException {

        int numOfThreads = 1;
        String outputFileName = "output.csv";
        String dataFolderName = ".\\Data";

        // Validate and parse command line arguments
        numOfThreads = validateNumberOfThreads(args, numOfThreads);
        outputFileName = validateFileName(args, outputFileName);
        dataFolderName = validateDataFolderName(args, dataFolderName);

        // Output the values
        System.out.println("Number of Threads: " + numOfThreads);
        System.out.println("File Name: " + outputFileName);

        Semaphore startSemaphore = new Semaphore(0);
        Semaphore stopSemaphore = new Semaphore(0);

        // prepare the Couchbase collection

            Cluster cluster = Cluster.connect(
                    connectionString,
                    ClusterOptions.clusterOptions(username, password).environment(env -> {
                    })
            );


            // get a bucket reference
            Bucket bucket = cluster.bucket(bucketName);
            bucket.waitUntilReady(Duration.ofSeconds(30));

            // get a user-defined collection reference
            Scope scope = bucket.scope("_default");
            Collection collection = scope.collection("_default");

            // Array of workers and their outcome
            BeerMule[] mules = new BeerMule[numOfThreads];
            Report[] reports = new Report[numOfThreads];

            for (int i = 0; i < numOfThreads; i++) {
                reports[i] = new Report();
                reports[i].dataFolder = dataFolderName;
                mules[i] = new BeerMule(i, collection, startSemaphore, stopSemaphore, reports[i]);
                mules[i].start();
            }

            // Start all workers at once
            System.out.println("Starting all workers..." + System.currentTimeMillis());
            startSemaphore.release(numOfThreads);

            try {
                for (int i = 0; i < 4; i++) {

                    TimeUnit.SECONDS.sleep(1);
                    System.out.print(".");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted.");
            }
            System.out.println("Stopping all workers..." + System.currentTimeMillis());

            for (int i = 0; i < numOfThreads; i++) {
                mules[i].stopRunning();
            }
            for (int i = 0; i < numOfThreads; i++) {
                stopSemaphore.acquire();
            }
            System.out.println("All workers have finished.");

            cluster.disconnect();

            ReportWriter.writeReportCSV(outputFileName, List.of(reports));

    }

    private static int validateNumberOfThreads(String[] args, int defaultThreads) {
        if (args.length > 0) {
            try {
                int threads = Integer.parseInt(args[0]);
                if (threads > 0) {
                    return threads;
                } else {
                    logger.warn("Invalid number of threads. Using default value: " + defaultThreads);
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid number format for threads. Using default value: " + defaultThreads);
            }
        }
        return defaultThreads;
    }

    private static String validateFileName(String[] args, String defaultFileName) {
        if (args.length > 1) {
            return args[1];
        }
        return defaultFileName;
    }

    private static String validateDataFolderName(String[] args, String defaultFolderName) {
        if (args.length > 2) {
            return args[2];
        }
        return defaultFolderName;
    }
}





