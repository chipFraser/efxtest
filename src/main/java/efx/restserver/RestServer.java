package efx.restserver;

import efx.SpotPrice;

import java.util.List;

public interface RestServer {
    void publishPrices(List<SpotPrice> spotPrices);
}
