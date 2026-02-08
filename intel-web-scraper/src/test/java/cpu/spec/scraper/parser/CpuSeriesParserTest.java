package cpu.spec.scraper.parser;

import cpu.spec.scraper.factory.ChromeDriverFactory;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CpuSeriesParserTest {

    @Test
    void testExtractSpecificationLinks() throws Exception {
        WebDriver driver = ChromeDriverFactory.getDriver();
        try {
            List<String> actual = CpuSeriesParser.extractSpecificationLinks(driver, "https://ark.intel.com/content/www/us/en/ark/products/series/123588/intel-core-x-series-processors.html");
            assertTrue(actual.size() >= 14, "size >= 14");
            assertTrue(actual.get(0).contains("/content/www/us/en/ark/products"), "sample contains '/content/www/us/en/ark/products'");
        } finally {
            driver.quit();
        }
    }
}
