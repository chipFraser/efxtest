package efx.pricefeed;

public interface FXPriceFeed {

    Handle subscribe(Listener listener);

    interface Listener {
        void onMessage(String message);
    }

    interface Handle {
        void unsubscribe();
    }
}
