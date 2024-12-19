package BeerFast;

import BeerFast.Config.Config;
import BeerFast.DatabaseConnection.CouchBaseConnection;
import BeerFast.Mule.BeerMuleFactory;
import BeerFast.Mule.MuleIfc;
import BeerFast.Report.ReportIfc;
import BeerFast.Report.ReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BeerFast {

    private static final Logger logger = LogManager.getLogger(BeerFast.class);

    public static void main(String[] args) throws InterruptedException {

        System.out.println("==[Fast Beer]==");
        // Read and prepare configuration
        Config config = Config.getConfig();

        // Validate and parse command line arguments
        // update configuration with command line parameters
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
            MuleIfc[] mules = new MuleIfc[numOfThreads];
            BeerMuleFactory beerMuleFactory = new BeerMuleFactory(startSemaphore, stopSemaphore);

            for (int i = 0; i <  numOfThreads; i++) {
                mules[i] = beerMuleFactory.createMule(startSemaphore, stopSemaphore, i, connection.getConnection(), config );
                mules[i].start();
            }

            // Start all workers at once
            System.out.println("Starting all workers...");
            startSemaphore.release(numOfThreads);

            System.out.println("Running.");
            // Perform tests over specific period
            int testDuration = config.getTestDuration();
            try {
                for (int i = 0; i <= testDuration; i++) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.printf("\r Seconds to complete %d.", testDuration - i );
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted.");
            }
            System.out.print("\n" );
            System.out.print("Stopping all workers..." );

            // stopping tests for each worker
            for (int i = 0; i < numOfThreads; i++) {
                mules[i].stopRunning();
            }
            // sync that all workers completed task
            for (int i = 0; i < numOfThreads; i++) {
                stopSemaphore.acquire();
            }
            System.out.println("all workers have finished.");
            System.out.println("Saving results...");
            List<ReportIfc> results = new ArrayList<>();
            for (int i = 0; i < numOfThreads; i++) {
                 results.add(mules[i].getResult());
             }
            ReportWriter.writeReportCSV(config.getOutputFileName(), results);

            connection.disconnect();

            System.out.println("==[Done]==");

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





