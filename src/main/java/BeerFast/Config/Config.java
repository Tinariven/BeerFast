package BeerFast.Config;

import BeerFast.BeerFast;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;

public final class Config {

    private static volatile Config instance;
    private static final String PROPERTIES_FILE = "application.properties";
    private static final Logger logger = LogManager.getLogger(Config.class);
    private static final Properties prop = new Properties();

    private int numOfThreads;
    private String dataFolderName;
    private String outputFileName;
    private String connectionString;
    private String username;
    private String password;
    private  String bucketName;
    private  String scopeName;
    private  String collectionName;
    private int testDuration;
    private int numerOfTestRuns;

    private Config() {
        try (InputStream input = BeerFast.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + PROPERTIES_FILE);
                logger.error("Sorry, unable to find " + PROPERTIES_FILE);
                throw new MissingResourceException("Resource not found", "config.properties", "");
            }
            prop.load(input);
            connectionString = Config.prop.getProperty("storage.host");
            username = Config.prop.getProperty("storage.username");
            password = Config.prop.getProperty("storage.password");
            bucketName = Config.prop.getProperty("storage.bucket");
            scopeName = Config.prop.getProperty("storage.scope");
            collectionName = Config.prop.getProperty("storage.collection");
            numOfThreads = Integer.parseInt(Config.prop.getProperty("default.numberOfThreads","1"));
            outputFileName = Config.prop.getProperty("default.output","output.csv");
            dataFolderName = Config.prop.getProperty("default.dataFolder",".\\Data");
            testDuration = Integer.parseInt(Config.prop.getProperty("test.duration"));
            numerOfTestRuns = Integer.parseInt(Config.prop.getProperty("test.getCount"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Config getConfig() {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    public String getConnectionString() {
        return connectionString;
    }
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getScopeName() {
        return scopeName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public int getNumOfThreads() {
        return numOfThreads;
    }

    public String getDataFolderName() {
        return dataFolderName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public int getTestDuration() {
        return testDuration;
    }

    public int getNumerOfTestRuns() {
        return numerOfTestRuns;
    }

    public void setNumOfThreads(int numOfThreads) {
        this.numOfThreads = numOfThreads;
    }

    public void setDataFolderName(String dataFolderName) {
        this.dataFolderName = dataFolderName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

}



