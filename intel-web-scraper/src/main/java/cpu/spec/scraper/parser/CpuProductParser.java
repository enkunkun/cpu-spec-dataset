package cpu.spec.scraper.parser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

public abstract class CpuProductParser {
    private static final String ENTRY_URL = "https://www.intel.com/content/www/us/en/ark.html";

    /**
     * @param driver WebDriver instance to use for fetching the page
     * @return series links for sub routing
     * @throws Exception if page cannot be retrieved or elements cannot be found
     */
    public static List<String> extractSeriesLinks(WebDriver driver) throws Exception {
        driver.get(ENTRY_URL);
        Thread.sleep(2000);
        Document page = Jsoup.parse(driver.getPageSource());

        // xPath: divs with data-parent-panel-key='Processors' -> divs with data-panel-key
        String xPathQuery = ".//div[@data-parent-panel-key='Processors']//div[@data-panel-key]";
        Elements generationButtons = page.selectXpath(xPathQuery);

        List<String> seriesLinks = new ArrayList<>();
        for (Element generationBtn : generationButtons) {

            // Extract page specific IDs from selector Parents
            String generationLabel = generationBtn.attr("data-panel-key");
            if (generationLabel.isBlank()) {
                continue;
            }

            // Extract links to Series
            // xPath: "a" children of divs with generation label
            xPathQuery = String.format(".//div[@data-parent-panel-key='%s']//a", generationLabel);
            Elements linkElements = page.selectXpath(xPathQuery);
            for (Element aSeries : linkElements) {
                String seriesLink = aSeries.attr("href");
                if (seriesLink.isBlank()) {
                    continue;
                }
                seriesLinks.add(seriesLink);
            }
        }
        return seriesLinks;
    }
}
