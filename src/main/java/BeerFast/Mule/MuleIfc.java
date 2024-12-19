package BeerFast.Mule;

import BeerFast.Report.ReportIfc;


public interface MuleIfc {
    void stopRunning();

    ReportIfc getResult();

    void start();

}
