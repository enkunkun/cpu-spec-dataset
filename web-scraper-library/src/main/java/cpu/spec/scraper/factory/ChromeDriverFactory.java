package cpu.spec.scraper.factory;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.logging.Logger;

/**
 * Factory to serve chrome drivers with a static evolving configuration.
 */
public abstract class ChromeDriverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * @return chrome driver with custom configuration
     */
    public static ChromeDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--ignore-certificate-errors");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        String proxyServer = System.getProperty("proxy.server");
        if (proxyServer != null && !proxyServer.isBlank()) {
            options.addArguments("--proxy-server=" + proxyServer);
            LOGGER.info("Using proxy: " + proxyServer);
        }

        return new ChromeDriver(options);
    }
}
