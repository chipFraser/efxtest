package efx.pricefeed;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPriceFeedImpl implements FXPriceFeed {
    private final CopyOnWriteArrayList<Listener> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public Handle subscribe(final Listener listener) {
        return () -> {
            subscribers.remove(listener);
        };
    }

    protected void firePriceToSubscribers(String priceCsv) {
        Iterator<Listener> iterator = subscribers.iterator();
        while (iterator.hasNext()) {
            iterator.next().onMessage(priceCsv);
        }
    }
}
