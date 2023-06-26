package efx;

import efx.marginmanger.MarginManagerImpl;
import efx.pricecache.ConcurrentHashMapPriceCacheImpl;
import efx.pricefeed.CSVFXPriceFeedImpl;
import efx.restserver.AsyncRestServerPriceListener;
import efx.restserver.ConsoleOutRestServer;
import efx.restserver.RestServer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Description: MarketPriceHandlerImpl contains the main subscription logic. Its functional dependencies are
 * FXPriceFeed for incoming prices, MarginManager for margins, PriceCache for storing latest pricess and
 * MarketPriceHandler.Listener for the rest server. A 5th dependency is the executor service for providing
 * concurrency with the incoming price feed thread.  Incoming prices can then be queued in the SingleThreadExecutor
 * as shown below. The margined price is published on a simple Listener which is directed toward the rest server.
 * For this the AsyncRestServerPriceListener will batch these for efficient publication to the rest Server on a
 * scheduled interval.
 * <p>
 * Assumptions:
 * 1) Incoming price thread should not be blocked or used for anything but queueing the incoming price;
 * 2) Outgoing publication to the rest server is potientially a slow consumer of prices
 * 3) Client direct access to latest price is coming in on a separate thread.
 * 4) Unit tests should test on a single thread
 * 5) This is a push design, a price pull design would require a refactoring
 */

public class Main {
    public static void main(String[] args) throws IOException {
        String csvFileName = args[0];

        CSVFXPriceFeedImpl priceFeed = new CSVFXPriceFeedImpl();
        RestServer restServer = new ConsoleOutRestServer();

        MarketPriceHandler.Listener priceListener = new AsyncRestServerPriceListener(Executors.newScheduledThreadPool(1), 500, restServer);

        MarketPriceHandler marketPriceHandler = new MarketPriceHandlerImpl(priceFeed, new MarginManagerImpl(),
                new ConcurrentHashMapPriceCacheImpl(), priceListener, Executors.newSingleThreadExecutor());

        marketPriceHandler.start();

        priceFeed.publish(new File(csvFileName));

        SpotPrice price = marketPriceHandler.getLatestSpotPrice("EUR/USD");
        System.out.println("Latest Price:" + price.getUniqueId()
                + ", " + price.getInstrumentName()
                + ", " + price.getBid()
                + ", " + price.getAsk()
                + ", " + price.getTimeStamp());
    }
}
