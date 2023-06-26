package efx.pricefeed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CSVFXPriceFeedImpl extends AbstractPriceFeedImpl {
    public void publish(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                firePriceToSubscribers(line);
            }
        }
    }

}
