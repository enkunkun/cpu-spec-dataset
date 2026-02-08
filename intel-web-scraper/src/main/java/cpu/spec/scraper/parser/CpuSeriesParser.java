package cpu.spec.scraper.parser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

public abstract class CpuSeriesParser {
    /**
     * @param driver WebDriver instance to use for fetching the page
     * @param url <a href="https://ark.intel.com/content/www/us/en/ark/products/series/230485/13th-generation-intel-core-i9-processors.html">Intel Processor Series Page</a>
     * @return cpu links for sub routing
     * @throws Exception if page cannot be retrieved or elements cannot be found
     */
    public static List<String> extractSpecificationLinks(WebDriver driver, String url) throws Exception {
        driver.get(url);
        Thread.sleep(500);
        Document page = Jsoup.parse(driver.getPageSource());

        // xPath: table with id "product-table" -> table body -> table rows
        String xPathQuery = ".//table[@id='product-table']//tbody/tr";
        Elements tableRows = page.selectXpath(xPathQuery);

        List<String> specificationLinks = new ArrayList<>();
        for (Element row : tableRows) {

            // xpath: table data with data-component "arkproductlink" -> "a" elements
            xPathQuery = ".//td[@data-component='arkproductlink']//a";

            Element linkElement = row.selectXpath(xPathQuery).first();
            if (linkElement != null) {
                specificationLinks.add(linkElement.attr("href"));
            }
        }
        return specificationLinks;
    }
}
