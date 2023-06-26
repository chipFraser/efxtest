package efx;

import efx.marginmanger.MarginManagerImpl;
import efx.pricecache.PriceCache;
import efx.pricecache.ConcurrentHashMapPriceCacheImpl;
import efx.pricefeed.FXPriceFeed;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MarketPriceHandlerImplTest {
    @Mock
    FXPriceFeed priceFeed;
    PriceCache priceCache = new ConcurrentHashMapPriceCacheImpl();
    @Mock
    MarketPriceHandler.Listener restServerPriceListener;
    MarginManagerImpl marginManager;
    @Captor
    ArgumentCaptor<FXPriceFeed.Listener> priceFeedListenerCaptor;

    MarketPriceHandler marketPriceHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        marginManager = new MarginManagerImpl();
        marketPriceHandler = new MarketPriceHandlerImpl(priceFeed, marginManager,  priceCache, restServerPriceListener, new SameThreadExecutorService());
        marketPriceHandler.start();
        verify(priceFeed).subscribe(priceFeedListenerCaptor.capture());
    }

    private static String EURUSD = "EUR/USD";
    private static String EURJPY = "EUR/JPY";
    private static String GBPUSD = "GBP/USD";
    private static String CSV106 = "106, " + EURUSD + ", 1.1000, 1.2000, 01-06-2020 12:01:01:001";
    private static String CSV107 = "107, " + EURJPY + ", 119.60,119.90,01-06-2020 12:01:02:002";
    private static String CSV108 = "108, " + GBPUSD + ", 1.2500,1.2560,01-06-2020 12:01:02:002";
    private static String CSV109 = "109, " + GBPUSD + ", 1.2499,1.2561,01-06-2020 12:01:02:100";
    private static String CSV110 = "110, " + EURJPY + ", 119.61,119.91,01-06-2020 12:01:02:110";


    @Test
    public void testPublish() {

        marginManager.setMargin(0.0);
        FXPriceFeed.Listener listener = priceFeedListenerCaptor.getValue();

        ArgumentCaptor<SpotPrice> priceCaptor = new   ArgumentCaptor<SpotPrice>();

        listener.onMessage(CSV106);
        verify(restServerPriceListener).onPrice(priceCaptor.capture());
        Assert.assertEquals(106, priceCaptor.getValue().getUniqueId());


        listener.onMessage(CSV107);
        verify(restServerPriceListener, times(2)).onPrice(priceCaptor.capture());
        Assert.assertEquals(107, priceCaptor.getValue().getUniqueId());

        listener.onMessage(CSV108);
        verify(restServerPriceListener, times(3)).onPrice(priceCaptor.capture());
        Assert.assertEquals(108, priceCaptor.getValue().getUniqueId());

        listener.onMessage(CSV109);
        verify(restServerPriceListener, times(4)).onPrice(priceCaptor.capture());
        Assert.assertEquals(109, priceCaptor.getValue().getUniqueId());

        listener.onMessage(CSV110);
        verify(restServerPriceListener, times(5)).onPrice(priceCaptor.capture());
        Assert.assertEquals(110, priceCaptor.getValue().getUniqueId());
    }

    @Test
    public void testLatest() {

        marginManager.setMargin(0.0);

        FXPriceFeed.Listener listener = priceFeedListenerCaptor.getValue();

        listener.onMessage(CSV106);

        listener.onMessage(CSV107);

        listener.onMessage(CSV108);

        listener.onMessage(CSV109);

        listener.onMessage(CSV110);


        SpotPrice expectedPrice = toPrice(CSV106);
        assertEquals(expectedPrice, marketPriceHandler.getLatestSpotPrice(EURUSD));

        expectedPrice = toPrice(CSV109);
        assertEquals(expectedPrice, marketPriceHandler.getLatestSpotPrice(GBPUSD));

        expectedPrice = toPrice(CSV110);
        assertEquals(expectedPrice, marketPriceHandler.getLatestSpotPrice(EURJPY));
    }


    @Test
    public void testMargin() {

        double margin = 1.0;
        marginManager.setMargin(margin);

        FXPriceFeed.Listener listener = priceFeedListenerCaptor.getValue();

        listener.onMessage(CSV106);
        SpotPrice expectedPrice = toPrice(CSV106, margin);
        assertEquals(expectedPrice, marketPriceHandler.getLatestSpotPrice(EURUSD));

    }

    void assertEquals(SpotPrice expected, SpotPrice actual) {
        Assert.assertEquals(expected.getUniqueId(), actual.getUniqueId());
        Assert.assertEquals(expected.getInstrumentName(), actual.getInstrumentName());
        Assert.assertEquals(expected.getBid(), actual.getBid(), 0.00001);
        Assert.assertEquals(expected.getAsk(), actual.getAsk(), 0.00001);
        Assert.assertEquals(expected.getTimeStamp(), actual.getTimeStamp());
    }
    private SpotPrice toPrice(String csv) {
        return toPrice(csv, 0.0);
    }
    private SpotPrice toPrice(String csv, double margin) {
        String[] fields = csv.split(",");
        long uniqueId = Long.parseLong(fields[0].trim());
        String instrumentName = fields[1].trim();
        double bid = Double.parseDouble(fields[2].trim()) +-margin;
        double ask = Double.parseDouble(fields[3].trim())+ margin;
        String timestamp = fields[4].trim();
        return new SpotPriceImpl(uniqueId, instrumentName, bid, ask, timestamp);
    }

    public class SameThreadExecutorService extends AbstractExecutorService {

        //volatile because can be viewed by other threads
        private volatile boolean terminated;

        @Override
        public void shutdown() {
            terminated = true;
        }

        @Override
        public boolean isShutdown() {
            return terminated;
        }

        @Override
        public boolean isTerminated() {
            return terminated;
        }

        @Override
        public boolean awaitTermination(long theTimeout, TimeUnit theUnit) throws InterruptedException {
            shutdown(); // TODO ok to call shutdown? what if the client never called shutdown???
            return terminated;
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public void execute(Runnable theCommand) {
            theCommand.run();
        }
    }
}
