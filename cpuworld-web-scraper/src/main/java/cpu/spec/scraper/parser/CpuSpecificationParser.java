package cpu.spec.scraper.parser;

import cpu.spec.scraper.CpuSpecificationModel;
import cpu.spec.scraper.validator.SeleniumValidator;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public abstract class CpuSpecificationParser {

    /**
     * @param driver shared WebDriver instance
     * @param url <a href="https://www.cpu-world.com/CPUs/Xeon/Intel-Xeon%208272CL.html">Cpu World Specification Page</a>
     * @return cpu specification model
     * @throws Exception if page cannot be retrieved or elements cannot be retrieved
     */
    public static CpuSpecificationModel extractSpecification(WebDriver driver, String url) throws Exception {
        SeleniumValidator validator = new SeleniumValidator(url);
        CpuSpecificationModel specification = new CpuSpecificationModel();

        driver.get(url);
        specification.id = selectId(url);
        specification.sourceUrl = url;

        // Wait for the dynamic content to load
        Thread.sleep(2000);

        // Extract the CPU name from the page
        try {
            WebElement mainDiv = driver.findElement(By.cssSelector("div#AB_B"));
            WebElement titleElement = mainDiv.findElement(By.tagName("h1"));
            specification.cpuName = titleElement.getText();
        } catch (NoSuchElementException e) {
            // Fallback: div#AB_B may not exist in headless mode, try finding h1 directly
            try {
                WebElement titleElement = driver.findElement(By.tagName("h1"));
                specification.cpuName = titleElement.getText();
            } catch (NoSuchElementException ignored) {
                specification.cpuName = "";
            }
        }
        // Extract the specification table
        WebElement infoDiv = validator.findElement(driver, By.cssSelector("div#GET_INFO"));

        WebElement specTable = validator.findElement(infoDiv, By.cssSelector("table.spec_table"));

        // Iterate over each row of the table
        List<WebElement> tableRows = specTable.findElements(By.tagName("tr"));
        for (WebElement tableRow : tableRows) {
            List<WebElement> tdList = tableRow.findElements(By.tagName("td"));
            if (tdList.size() < 2) {
                continue;
            }
            String dataKey = cleanValue(tdList.get(0).getText());
            if (dataKey.isBlank() || isKeyIgnored(dataKey)) {
                continue;
            }
            String dataValue = cleanValue(tdList.get(1).getText());
            if (dataValue.isBlank()) {
                specification.dataValues.put(dataKey, null);
                continue;
            }
            specification.dataValues.put(dataKey, dataValue);
        }
        return specification;
    }

    private static String selectId(String url) {
        String[] split = url.split("/");
        if (split.length > 0) {
            return split[split.length - 1].trim().replaceAll(".html", "");
        } else {
            return null;
        }
    }

    private static String cleanValue(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\n", " ")
                .replaceAll("\\?", "")
                .replaceAll("\\[\\d]", "")
                .trim();
    }

    private static boolean isKeyIgnored(String key) {
        var normedKey = key.toLowerCase();
        return normedKey.contains("part number")
                || normedKey.contains("memory controller")
                || normedKey.contains("other peripherals")
                || normedKey.contains("extensions")
                || normedKey.contains("package")
                || normedKey.contains("none")
                || normedKey.contains("unknown")
                || key.startsWith("BX")
                || key.startsWith("CD")
                || key.startsWith("PK");
    }
}
