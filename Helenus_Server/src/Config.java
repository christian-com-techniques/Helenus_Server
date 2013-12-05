import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;

public class Config {

    private Properties config;

    public Config(String configFile) throws IOException {
        config = new Properties();
        try {
            config.load(new FileInputStream(configFile));
        } catch (NullPointerException e) {
            throw new IOException("File Not Found: " + configFile);
        }
    }

    public String valueFor(String key) {
        return this.config.getProperty(key);
    }
    public int intFor(String key) {
        return Integer.parseInt(valueFor(key));
    }
}