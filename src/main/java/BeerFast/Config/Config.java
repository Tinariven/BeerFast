package BeerFast.Config;

import BeerFast.BeerFast;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;

public final class Config {

    public static final String PROPERTIES_FILE = "application.properties";
    private static final Logger logger = LogManager.getLogger(Config.class);
    public static final Properties prop = new Properties();

    public Config() {
        try (InputStream input = BeerFast.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + PROPERTIES_FILE);
                logger.error("Sorry, unable to find " + PROPERTIES_FILE);
                throw new MissingResourceException("Resource not found", "config.properties", "");
            }
            prop.load(input);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}



