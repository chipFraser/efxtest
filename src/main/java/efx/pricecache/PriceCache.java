package efx.pricecache;

import efx.SpotPrice;

public interface PriceCache {
    SpotPrice get(String symbol);

    void put(SpotPrice price);
}
