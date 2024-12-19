package BeerFast.Mule;

import java.util.concurrent.Semaphore;

public abstract class MuleFactory {
    private Semaphore startSemaphore;
    private Semaphore stopSemaphore;
    public abstract MuleIfc createMule(Semaphore startSemaphore, Semaphore stopSemaphore) throws Exception;
}

