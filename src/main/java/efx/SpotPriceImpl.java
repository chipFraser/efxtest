package efx;

public class SpotPriceImpl implements SpotPrice {
    private final long uniqueId;
    private final String instrumentName;
    private final double bid;
    private final double ask;
    private final String timestamp;


    public SpotPriceImpl(final long uniqueId, final String instrumentName, final double bid, final double ask, final String timestamp) {
        this.uniqueId = uniqueId;
        this.instrumentName = instrumentName;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    @Override
    public long getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getInstrumentName() {
        return instrumentName;
    }

    @Override
    public double getBid() {
        return bid;
    }

    @Override
    public double getAsk() {
        return ask;
    }

    @Override
    public String getTimeStamp() {
        return timestamp;
    }


}
