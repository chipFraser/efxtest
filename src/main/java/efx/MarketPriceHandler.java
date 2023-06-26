package efx;

public interface MarketPriceHandler {
    void start();

    void stop();

    SpotPrice getLatestSpotPrice(String symbol);

    interface Listener {
        void onPrice(SpotPrice spotPrice);
    }
}
