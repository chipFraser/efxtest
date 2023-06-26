package efx.restserver;

import efx.SpotPrice;

import java.util.List;

public class ConsoleOutRestServer implements RestServer {
    @Override
    public void publishPrices(List<SpotPrice> spotPrices) {

        for (SpotPrice price : spotPrices) {
            System.out.println("Price:" + price.getUniqueId()
                    + ", " + price.getInstrumentName()
                    + ", " + price.getBid()
                    + ", " + price.getAsk()
                    + ", " + price.getTimeStamp());
        }
    }
}
