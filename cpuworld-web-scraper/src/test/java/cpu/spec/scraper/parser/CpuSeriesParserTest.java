package cpu.spec.scraper.parser;

import cpu.spec.scraper.factory.ChromeDriverFactory;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CpuSeriesParserTest {

    @Test
    void testExtractSpecificationLinks() throws Exception {
        WebDriver driver = ChromeDriverFactory.getDriver();
        try {
            List<String> actual = CpuSeriesParser.extractNavigationLinks(driver, "https://www.cpu-world.com/CPUs/Xeon/TYPE-Xeon Platinum.html");
            assertFalse(actual.isEmpty(), "actual is empty");
        } finally {
            driver.quit();
        }
    }
}
