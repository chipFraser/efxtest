package efx.pricecache;

import efx.SpotPrice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapPriceCacheImpl implements PriceCache {
    private final Map<String, SpotPrice> prices = new ConcurrentHashMap();     // used to provide multi thread access to the cache from the client

    @Override
    public SpotPrice get(String symbol) {
        return prices.get(symbol);
    }

    @Override
    public void put(SpotPrice price) {
        prices.put(price.getInstrumentName(), price);

    }
}
