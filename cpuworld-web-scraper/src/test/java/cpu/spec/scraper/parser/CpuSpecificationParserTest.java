package cpu.spec.scraper.parser;

import cpu.spec.scraper.CpuSpecificationModel;
import cpu.spec.scraper.factory.ChromeDriverFactory;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpuSpecificationParserTest {

    @Test
    void testExtractSpecificationSample() throws Exception {
        WebDriver driver = ChromeDriverFactory.getDriver();
        try {
            CpuSpecificationModel actual = CpuSpecificationParser.extractSpecification(driver, "https://www.cpu-world.com/CPUs/Xeon/Intel-Xeon 8272CL.html");
            assertEquals("Intel-Xeon 8272CL", actual.id);
            assertEquals("Intel Xeon Platinum 8272CL", actual.cpuName);
            assertEquals("https://www.cpu-world.com/CPUs/Xeon/Intel-Xeon 8272CL.html", actual.sourceUrl);

            assertEquals("26", actual.dataValues.get("The number of CPU cores"));
            assertEquals("52", actual.dataValues.get("The number of threads"));
        } finally {
            driver.quit();
        }
    }
}
