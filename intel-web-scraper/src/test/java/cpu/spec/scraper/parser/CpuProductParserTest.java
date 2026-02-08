package cpu.spec.scraper.parser;

import cpu.spec.scraper.factory.ChromeDriverFactory;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CpuProductParserTest {

    @Test
    void testExtractSeriesLinks() throws Exception {
        WebDriver driver = ChromeDriverFactory.getDriver();
        try {
            List<String> actual = CpuProductParser.extractSeriesLinks(driver);
            assertTrue(actual.size() >= 114, "size >= 114");
            assertTrue(actual.get(0).contains("/content/www/us/en/ark/products/series"), "sample contains '/content/www/us/en/ark/products/series'");
        } finally {
            driver.quit();
        }
    }
}
