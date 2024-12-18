package BeerFast;

import BeerFast.Config.Config;
import BeerFast.Report.BeerReport;
import BeerFast.Report.ReportIfc;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;


class BeerMule extends Thread implements MuleIfc {

    private static final Logger logger = LogManager.getLogger(BeerMule.class);
    private final Semaphore startSemaphore;
    private final Semaphore stopSemaphore;
    private final int id;
    private volatile boolean isRunning = true;
    private final Collection collection;
    private final Config config;

    private final BeerReport report;

    public BeerMule(int id, Collection collection, Config config, Semaphore startSemaphore, Semaphore stopSemaphore) {
        this.id = id;
        this.startSemaphore = startSemaphore;
        this.stopSemaphore = stopSemaphore;
        this.collection = collection;
        this.report = new BeerReport(id, config.getDataFolderName());
        this.config = config;
    }

    @Override
    public void run() {
        try {
            startSemaphore.acquire();
            logger.info("Mule " + id + " started.");
            // report.setStartTime(System.currentTimeMillis());
            executeTest(config.getDataFolderName(), config.getNumerOfTestRuns());
            // report.setEndTime(System.currentTimeMillis());
            logger.info("Mule " + id + " finished work in " + (report.getEndTime() - report.getStartTime()) / 1000 + "s ");
            stopSemaphore.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Worker " + id + " was interrupted." + e);
        }
    }

    public void stopRunning() {
        isRunning = false;
    }

    @Override
    public ReportIfc getResult() {
        return report;
    }


    /**
     * Runs tests reading each .json file from folderPath
     * uploading to CouchBase, then retrieving it getCount times
     * test measures are stored in report at the end of test
     *
     * @param folderPath - directory where test .json are stored
     * @param getCount - indicates how many times stored document will be retrieved from db
     */
    private void executeTest(String folderPath, int getCount) {

        File folder = new File(folderPath);
        long numberOfRuns = 0;
        long filesRead = 0;
        long documentCalls = 0;
        long loadDuration = 0;
        long getResultDuration = 0;
        long dataVolume = 0;

        report.setStartTime(System.currentTimeMillis());

        if (folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                while (isRunning) {
                    for (File file : files) {
                        try {
                            if (!isRunning) {
                                break;
                            }

                            long startT = System.currentTimeMillis();
                            Pair<String, Long> r;
                            r = uploadDocument(collection, file);
                            String docID = r.getValue0();
                            dataVolume = dataVolume + r.getValue1();
                            filesRead++;

                            if (!docID.isEmpty()) {
                                filesRead++;
                                long loadT = System.currentTimeMillis();

                                for (int i = 0; i < getCount; i++) {
                                    if (!isRunning) {
                                        break;
                                    }
                                    documentCalls++;
                                    //
                                    collection.get(docID);
                                }
                                long endT = System.currentTimeMillis();

                                loadDuration += (loadT - startT);
                                getResultDuration += (endT - loadT);

                            }
                            numberOfRuns++;

                        } catch (Exception e) {
                            logger.log(Level.ERROR, "An error occurred", e);
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                logger.warn("No JSON files found in the directory.");
            }
        } else {
            logger.warn("The provided path is not a directory.");
        }

        // finalizing the results
        report.setEndTime(System.currentTimeMillis());
        report.setNumberOfRuns(numberOfRuns);
        report.setFilesRead(filesRead);
        report.setDocumentCalls(documentCalls);
        report.setLoadDuration(loadDuration);
        report.setGetResultDuration(getResultDuration);

        logger.info("Mule" + id + " Run:" + numberOfRuns + " times, " +
                "Loaded:" + filesRead + " files, " + dataVolume + " bytes, in " + loadDuration + "ms " +
                "Made:" + documentCalls + " calls in " + getResultDuration + "ms");
    }

    public static JsonObject convertToCouchbaseJsonObject(JSONObject jsonObject) {
        JsonObject couchbaseJsonObject = JsonObject.create();
        for (String key : jsonObject.keySet()) {
            couchbaseJsonObject.put(key, jsonObject.get(key));
        }
        return couchbaseJsonObject;
    }


    public static Pair<String, Long> uploadDocument(Collection collection, File file) {

        String docID = "";
        long dataVolume = 0;

        try {
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
            dataVolume = content.length();
            JsonObject jsonObject = convertToCouchbaseJsonObject(new JSONObject(content));
            // MutationResult upsertResult =
            collection.upsert(
                    docID, jsonObject
            );
        } catch (Exception e) {
            logger.log(Level.ERROR, "An error occurred", e);
        }

        return Pair.with(docID, dataVolume);
    }

}