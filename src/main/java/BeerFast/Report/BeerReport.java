package BeerFast.Report;

public class BeerReport implements ReportIfc {

    private long muleID;
    private String dataFolder;
    private long startTime;
    private long endTime;
    private long numberOfRuns;
    private long filesRead;
    private long dataVolume;
    private long documentCalls;
    private long loadDuration;
    private long getResultDuration;


    public BeerReport(long Id, String dataFolder){
        this.muleID = Id;
        this.dataFolder = dataFolder;
    }


    public void update(BeerReport report) {
        this.startTime = report.startTime;
        this.endTime = report.endTime;
        this.numberOfRuns = report.numberOfRuns;
        this.filesRead = report.filesRead;
        this.dataVolume = report.dataVolume;
        this.documentCalls = report.documentCalls;
        this.loadDuration = report.loadDuration;
        this.getResultDuration = report.getResultDuration;
    }

    public long getMuleID() {
        return muleID;
    }

    public void setMuleID(long muleID) {
        this.muleID = muleID;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getNumberOfRuns() {
        return numberOfRuns;
    }

    public void setNumberOfRuns(long numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public long getFilesRead() {
        return filesRead;
    }

    public void setFilesRead(long filesRead) {
        this.filesRead = filesRead;
    }

    public long getDataVolume() {
        return dataVolume;
    }

    public void setDataVolume(long dataVolume) {
        this.dataVolume = dataVolume;
    }

    public long getDocumentCalls() {
        return documentCalls;
    }

    public void setDocumentCalls(long documentCalls) {
        this.documentCalls = documentCalls;
    }

    public long getLoadDuration() {
        return loadDuration;
    }

    public void setLoadDuration(long loadDuration) {
        this.loadDuration = loadDuration;
    }

    public long getGetResultDuration() {
        return getResultDuration;
    }

    public void setGetResultDuration(long getResultDuration) {
        this.getResultDuration = getResultDuration;
    }

    @Override
    public String print() {
        return String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
                this.getMuleID(), this.getStartTime(), this.getEndTime(), this.getNumberOfRuns(), this.getFilesRead(), this.getDataVolume(), this.getDocumentCalls(), this.getLoadDuration(), this.getGetResultDuration());

    }
}
