package cpu.spec.scraper.parser;

import java.net.URI;
import java.net.URISyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import cpu.spec.scraper.CpuSpecificationModel;

public abstract class CpuSpecificationParser {
    /**
     * @param driver WebDriver instance to use for fetching the page
     * @param url <a href="https://ark.intel.com/content/www/us/en/ark/products/226449/intel-pentium-gold-processor-8500-8m-cache-up-to-4-40-ghz.html">Intel Processor Specification Page</a>
     * @return cpu specification model
     * @throws Exception if page cannot be retrieved or elements cannot be found
     */
    public static CpuSpecificationModel extractSpecification(WebDriver driver, String url) throws Exception {
        driver.get(url);
        Thread.sleep(500);
        Document page = Jsoup.parse(driver.getPageSource());
        CpuSpecificationModel specification = new CpuSpecificationModel();

        // Select title element
        // xPath: divs with class='product-details' -> flexible element with itemprop=name
        String xPathQuery = ".//div[@class='product-details']//*[@itemprop='name']";
        Element titleElement = page.selectXpath(xPathQuery).first();

        specification.id = selectId(url);
        specification.cpuName = titleElement != null ? titleElement.text() : "";
        specification.sourceUrl = url;

        // Extract specifications
        xPathQuery = ".//div[contains(@id, 'spec')]//div[contains(@class, 'tech-section')]";
        Elements specElements = page.selectXpath(xPathQuery);
        for (Element specElement : specElements) {
            xPathQuery = ".//div[contains(@class, 'tech-label')]";
            Element dataKeyElement = specElement.selectXpath(xPathQuery).first();
            xPathQuery = ".//div[contains(@class, 'tech-data')]";
            Element dataValue = specElement.selectXpath(xPathQuery).first();

            if (dataKeyElement != null && dataValue != null && !isKeyIgnored(dataKeyElement.text())) {
                specification.dataValues.put(dataKeyElement.text().trim(), dataValue.text().trim());
            }
        }
        return specification;
    }

    private static String selectId(String url) {
        try {
            return new URI(url).getPath().trim().split("/")[7];
        } catch (URISyntaxException e) {
            return e.getClass().getSimpleName();
        }
    }

    private static boolean isKeyIgnored(String key) {
        return key.equalsIgnoreCase("DatasheetUrl");
    }
}
