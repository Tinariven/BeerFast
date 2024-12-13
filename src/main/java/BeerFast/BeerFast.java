package BeerFast;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import BeerFast.Config.Config;
import BeerFast.Report.Report;
import BeerFast.Report.ReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.couchbase.client.java.*;

public class BeerFast {

    private static final Logger logger = LogManager.getLogger(BeerFast.class);

    //TODO: load configuration from properties

    static String connectionString;
    static String username;
    static String password;
    static String bucketName;
    static String scopeName;
    static String collectionName;

    static Config config = new Config();


    public static void main(String[] args) throws InterruptedException {

        int numOfThreads = Integer.parseInt(Config.prop.getProperty("default.numberOfThreads","1"));
        String outputFileName = Config.prop.getProperty("default.output","output.csv");
        String dataFolderName = Config.prop.getProperty("default.dataFolder",".\\Data");


        System.out.println("==[Fast Beer]==");

        // Validate and parse command line arguments
        numOfThreads = validateNumberOfThreads(args, numOfThreads);
        outputFileName = validateFileName(args, outputFileName);
        dataFolderName = validateDataFolderName(args, dataFolderName);

        connectionString = Config.prop.getProperty("storage.host");
        username = Config.prop.getProperty("storage.username");
        password = Config.prop.getProperty("storage.password");
        bucketName = Config.prop.getProperty("storage.bucket");
        scopeName = Config.prop.getProperty("storage.scope");
        collectionName = Config.prop.getProperty("storage.collection");

        System.out.println("Number of Threads: " + numOfThreads +
                " Data folder: " + dataFolderName +
                " Result file: " + outputFileName);

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
            Scope scope = bucket.scope(scopeName);
            Collection collection = scope.collection(collectionName);

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
            System.out.println("Starting all workers...");
            startSemaphore.release(numOfThreads);

            System.out.println("Running.");
            // Perform tests over specific period
            int testDuration = Integer.parseInt(Config.prop.getProperty("test.duration"));
            try {
                for (int i = 0; i < testDuration; i++) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.printf("\r Seconds to complete %d", testDuration - i );
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted.");
            }
            System.out.print("\n" );
            System.out.println("Stopping all workers..." );

            for (int i = 0; i < numOfThreads; i++) {
                mules[i].stopRunning();
            }
            for (int i = 0; i < numOfThreads; i++) {
                stopSemaphore.acquire();
            }
            System.out.println("All workers have finished.");

            cluster.disconnect();

            System.out.println("Saving results.");
            ReportWriter.writeReportCSV(outputFileName, List.of(reports));

            System.out.println("Done.");

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





