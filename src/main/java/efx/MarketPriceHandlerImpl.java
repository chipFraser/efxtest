package efx;

import efx.marginmanger.MarginManager;
import efx.pricecache.PriceCache;
import efx.pricefeed.FXPriceFeed;

import java.util.concurrent.ExecutorService;

public class MarketPriceHandlerImpl implements MarketPriceHandler {
    private final FXPriceFeed priceFeed;
    private final MarginManager marginManager;
    private final PriceCache priceCache;
    private final Listener restServerPriceListener;
    private final ExecutorService executorService;  // used to  decouple incoming price thread from outgoing publishing
    private FXPriceFeed.Handle handle;
    private boolean started = false;

    public MarketPriceHandlerImpl(final FXPriceFeed priceFeed,
                                  final MarginManager marginManager,
                                  final PriceCache priceCache,
                                  final Listener restServerPriceListener,
                                  final ExecutorService executorService) {
        this.priceFeed = priceFeed;
        this.marginManager = marginManager;
        this.priceCache = priceCache;
        this.restServerPriceListener = restServerPriceListener;
        this.executorService = executorService;
    }

    public void start() {
        if (!started) {
            started = true;
            handle = priceFeed.subscribe(this::onPrice);
        }
    }

    public void stop() {
        if (started) {
            started = false;
            executorService.shutdownNow();
            handle.unsubscribe();
            handle = null;
        }
    }

    @Override
    public SpotPrice getLatestSpotPrice(final String symbol) {
        return priceCache.get(symbol);
    }

    private void onPrice(final String priceCsv) {
        executorService.execute(() ->
        {
            SpotPrice price = processPrice(priceCsv);
            priceCache.put(price);
            restServerPriceListener.onPrice(price);
        });
    }

    private SpotPrice processPrice(String priceCsv) {
        String[] fields = priceCsv.split(",");
        long uniqueId = Long.parseLong(fields[0].trim());
        String instrumentName = fields[1].trim();
        double bid = marginManager.addBidMargin(Double.parseDouble(fields[2].trim()));
        double ask = marginManager.addAskMargin(Double.parseDouble(fields[3].trim()));
        String timestamp = fields[4].trim();
        return new SpotPriceImpl(uniqueId, instrumentName, bid, ask, timestamp);
    }

}
