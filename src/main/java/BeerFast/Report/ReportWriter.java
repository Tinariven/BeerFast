package BeerFast.Report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;



public class ReportWriter {

    private static final Logger logger = LogManager.getLogger(ReportWriter.class);
    private static final String CSV_HEADER = "muleID,startTime,endTime,numberOfRuns,filesRead,dataVolume,documentCalls,loadDuration,getResultDuration";

    /**
     * Saves test results (reports) as csv file
     * @param fileName - output file name
     * @param reports - test results collection
     */
    public static void writeReportCSV(String fileName, List<ReportIfc> reports) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write CSV header
            writer.println(CSV_HEADER);
            // Write data
            for (ReportIfc report : reports) {
                writer.println(report.print());
              }
            System.out.println("Report "+fileName+" created successfully.");
        } catch (IOException e) {
            logger.error("Error writing: " + e.getMessage());
        }
    }
}
