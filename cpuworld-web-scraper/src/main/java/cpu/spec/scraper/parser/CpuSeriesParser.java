package cpu.spec.scraper.parser;

import cpu.spec.scraper.factory.ChromeDriverFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public abstract class CpuSeriesParser {
    /**
     * @param url <a href="">CPU World Series Page</a>
     * @return cpu links for sub routing
     * @throws Exception if page cannot be retrieved or elements cannot be found
     */
    public static List<String> extractNavigationLinks(String url) throws Exception {
        WebDriver driver = ChromeDriverFactory.getDriver();
        try {
            driver.get(url);
            Thread.sleep(1000);
            Document page = Jsoup.parse(driver.getPageSource());

            Elements cpuNames = page.select("div.cpu_name");

            List<String> specificationLinks = new ArrayList<>();
            for (Element row : cpuNames) {
                Element aSpec = row.selectFirst("a");
                if (aSpec == null) {
                    continue;
                }
                String link = aSpec.attr("href");
                if (link.isBlank()) {
                    continue;
                }
                specificationLinks.add(link);
            }
            return specificationLinks;
        } finally {
            driver.quit();
        }
    }
}
