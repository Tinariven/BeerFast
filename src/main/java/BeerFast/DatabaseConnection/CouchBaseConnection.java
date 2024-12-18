package BeerFast.DatabaseConnection;
import BeerFast.Config.Config;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;


public class CouchBaseConnection implements DatabaseConnectionIfc{
    private static volatile CouchBaseConnection instance;


    private Cluster cluster;
    private Bucket bucket;
    private Scope scope;
    private Collection collection;

    private static final Logger logger = LogManager.getLogger(CouchBaseConnection.class);

    /**
     * Connects to Couchbase Cluster using connection string, username and password
     * @param connectionString
     * @param username
     * @param password
     * @throws CouchbaseException
     */

    private CouchBaseConnection(String connectionString, String username, String password,String bucketName, String scopeName, String collectionName) throws CouchbaseException  {
        try {
            // connect to cluster
           this.cluster = Cluster.connect(
                    connectionString,
                    ClusterOptions.clusterOptions(username, password).environment(env -> {} )
            );
            // get a bucket reference
            this.bucket = cluster.bucket(bucketName);
            this.bucket.waitUntilReady(Duration.ofSeconds(30));
            // get a user-defined collection reference
            this.scope = bucket.scope(scopeName);
            this.collection = scope.collection(collectionName);

        } catch ( CouchbaseException e) {
            logger.error("Error connecting to CouchBase instance" + e.getMessage());
        }
    }



    public static CouchBaseConnection getInstance() throws CouchbaseException {
        if (instance == null) {
            synchronized (CouchBaseConnection.class) {
                if (instance == null) {
                    Config config = Config.getConfig();
                    instance = new CouchBaseConnection(config.getConnectionString(),config.getUsername(),config.getPassword(),config.getBucketName(),config.getScopeName(),config.getCollectionName());
                }
            }
        }
        return instance;
    }

    public Collection getConnection() {
        return this.collection;
    }

    @Override
    public void disconnect() {
        cluster.disconnect();
    }
}

