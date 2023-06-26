package efx;

public interface SpotPrice {
    long getUniqueId();

    String getInstrumentName();

    double getBid();

    double getAsk();

    String getTimeStamp();


}
