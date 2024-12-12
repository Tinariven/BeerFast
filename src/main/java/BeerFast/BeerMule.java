package BeerFast;

import java.util.concurrent.Semaphore;

import BeerFast.Report.Report;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.json.JSONObject;
import com.couchbase.client.java.Collection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


class BeerMule extends Thread {
    private static final Logger logger = LogManager.getLogger(BeerMule.class);
    private final Semaphore startSemaphore;
    private final Semaphore stopSemaphore;
    private final Report report;
    private final int id;
    private volatile boolean isRunning = true;
    private final Collection collection;

    public BeerMule(int id, Collection collection, Semaphore startSemaphore, Semaphore stopSemaphore,Report report) {
        this.id = id;
        this.startSemaphore = startSemaphore;
        this.stopSemaphore = stopSemaphore;
        this.collection = collection;
        this.report = report;
        report.muleID = id;
    }

    @Override
    public void run() {
        try {
            startSemaphore.acquire();
            logger.info("Mule " + id + " started.");
            report.startTime = System.currentTimeMillis();
            executeTest(report.dataFolder);
            report.endTime = System.currentTimeMillis();
            logger.info("Mule " + id + " finished work in " +  (report.endTime - report.startTime)/1000 + "s "  );
            stopSemaphore.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Worker " + id + " was interrupted." + e);
        }
    }

    public void stopRunning() {
        isRunning = false;
    }

    private void executeTest(String folderPath) {

        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                while (isRunning) {
                    report.numberOfRuns++;
                    for (File file : files) {
                        try {
                            long startTime = System.currentTimeMillis();
                            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
                            report.dataVolume = report.dataVolume + content.length();
                            JsonObject jsonObject = convertToCouchbaseJsonObject(new JSONObject(content)) ;

                            logger.debug("Mule:" + id + " File:" + report.filesRead + " " + file.getName() );
                            report.filesRead++;

                            String docID = "M" + id + "ID" +  report.filesRead;
                            MutationResult upsertResult = collection.upsert(
                                    docID, jsonObject
                            );
                            long loadTime = System.currentTimeMillis();

                            for (int i = 0; i < 3; i++) {
                                report.documentCalls++;
                                GetResult getResult = collection.get(docID);
                                JsonObject result = getResult.contentAsObject();
                                logger.debug("Mule:" + id + " Doc:" + docID + " > " + result.getString("name") );
                            }
                            long endTime = System.currentTimeMillis();

                            report.loadDuration = report.loadDuration + (loadTime - startTime);
                            report.getResultDuration = report.getResultDuration + (endTime - loadTime);


                        } catch (IOException e) {
                            logger.log( Level.ERROR, "An error occurred", e);
                            e.printStackTrace();
                        }
                    }
                }

                logger.info("Mule" + id + " Run:" + report.numberOfRuns + " times, " +
                        "Loaded:" + report.filesRead + " files, " + report.dataVolume + " bytes, in " + report.loadDuration  + "ms " +
                        "Made:" + report.documentCalls + " calls in " + report.getResultDuration   + "ms" );

            } else {
                logger.warn("No JSON files found in the directory.");
            }
        } else {
            logger.warn("The provided path is not a directory.");
        }
    }

    public static JsonObject convertToCouchbaseJsonObject(JSONObject jsonObject) {
        JsonObject couchbaseJsonObject = JsonObject.create();
        for (String key : jsonObject.keySet()) {
            couchbaseJsonObject.put(key, jsonObject.get(key));
        }
        return couchbaseJsonObject;
    }

}
