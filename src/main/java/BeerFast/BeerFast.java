package BeerFast;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import BeerFast.Config.Config;
import BeerFast.DatabaseConnection.CouchBaseConnection;
import BeerFast.DatabaseConnection.DatabaseConnectionIfc;
import BeerFast.Report.Report;
import BeerFast.Report.ReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




public class BeerFast {

    private static final Logger logger = LogManager.getLogger(BeerFast.class);

    public static void main(String[] args) throws InterruptedException {

       //  int numOfThreads = Integer.parseInt(Config.prop.getProperty("default.numberOfThreads","1"));
       // String outputFileName = Config.prop.getProperty("default.output","output.csv");
       // String dataFolderName = Config.prop.getProperty("default.dataFolder",".\\Data");

        System.out.println("==[Fast Beer]==");
        Config config = Config.getConfig();

        // Validate and parse command line arguments
        config.setNumOfThreads(validateNumberOfThreads(args, config.getNumOfThreads()));
        config.setOutputFileName(validateFileName(args, config.getOutputFileName()));
        config.setDataFolderName(validateDataFolderName(args,config.getDataFolderName()));

        System.out.println("Number of Threads: " + config.getNumOfThreads() +
                " Data folder: " + config.getDataFolderName() +
                " Result file: " + config.getOutputFileName());

        // Prepare connection and prepare the Couchbase collection
        CouchBaseConnection connection = CouchBaseConnection.getInstance();

        // Prepare threads
        Semaphore startSemaphore = new Semaphore(0);
        Semaphore stopSemaphore = new Semaphore(0);

        int numOfThreads = config.getNumOfThreads();

            // Array of workers and their outcome
            BeerMule[] mules = new BeerMule[numOfThreads];
            Report[] reports = new Report[numOfThreads];

            for (int i = 0; i <  numOfThreads; i++) {
                reports[i] = new Report();
                reports[i].dataFolder = config.getDataFolderName();
                mules[i] = new BeerMule(i, connection.getConnection(), startSemaphore, stopSemaphore, reports[i]);
                mules[i].start();
            }

            // Start all workers at once
            System.out.println("Starting all workers...");
            startSemaphore.release(numOfThreads);

            System.out.println("Running.");
            // Perform tests over specific period
            int testDuration = config.getTestDuration();
            try {
                for (int i = 0; i < testDuration; i++) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.printf("\r Seconds to complete %d.", testDuration - i );
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

            connection.disconnect();

            System.out.println("Saving results.");
            ReportWriter.writeReportCSV(config.getOutputFileName(), List.of(reports));

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





