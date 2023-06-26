package efx.restserver;

import efx.MarketPriceHandler;
import efx.SpotPrice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncRestServerPriceListener implements MarketPriceHandler.Listener {
    private final ScheduledExecutorService scheduledExecutorService;
    private final RestServer restServer;
    private final long asyncDelayInMillis;
    ConcurrentLinkedQueue<SpotPrice> spotPricesQueue = new ConcurrentLinkedQueue<>();
    private boolean started;

    public AsyncRestServerPriceListener(final ScheduledExecutorService scheduledExecutorService,
                                        final long asyncDelayInMillis,
                                        final RestServer restServer
    ) {
        this.scheduledExecutorService = scheduledExecutorService;

        this.restServer = restServer;
        this.asyncDelayInMillis = asyncDelayInMillis;
    }

    public void start() {
        if (!started) {
            started = true;
            scheduledExecutorService.schedule(this::publishPricesToRestServer, asyncDelayInMillis, TimeUnit.MILLISECONDS);
        }

    }

    public void stop() {
        scheduledExecutorService.shutdownNow();
    }

    private void publishPricesToRestServer() {
        List<SpotPrice> spotPrices = new ArrayList(spotPricesQueue.size());
        spotPricesQueue.removeAll(spotPrices);
        restServer.publishPrices(spotPrices);
    }

    @Override
    public void onPrice(SpotPrice spotPrice) {
        spotPricesQueue.offer(spotPrice);
    }

}
