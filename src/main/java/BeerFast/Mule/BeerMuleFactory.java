package BeerFast.Mule;

import BeerFast.Config.Config;
import com.couchbase.client.java.Collection;
import java.util.concurrent.Semaphore;

public class BeerMuleFactory extends MuleFactory {

    private final Semaphore start;
    private final Semaphore stop;

    public BeerMuleFactory(Semaphore startSemaphore, Semaphore stopSemaphore){
        this.start = startSemaphore;
        this.stop = stopSemaphore;
    }

    public MuleIfc createMule(Semaphore startSemaphore, Semaphore stopSemaphore, int id, Collection collection,Config config) {
        return new BeerMule(startSemaphore, stopSemaphore, id, collection, config);
    }

    @Override
    public MuleIfc createMule(Semaphore startSemaphore, Semaphore stopSemaphore) throws Exception {
        throw new Exception("Mule configuration is required");
    }
}
