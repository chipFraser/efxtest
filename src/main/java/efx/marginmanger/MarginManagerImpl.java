package efx.marginmanger;

public class MarginManagerImpl implements MarginManager {

    private double margin = 0.1;

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    @Override
    public double addBidMargin(double bid) {
        return bid - margin;
    }

    @Override
    public double addAskMargin(double ask) {
        return ask + margin;
    }
}
