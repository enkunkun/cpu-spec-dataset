package cpu.spec.scraper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import me.tongfei.progressbar.ProgressBar;
import org.openqa.selenium.WebDriver;

import cpu.spec.scraper.exception.DirectoryNotFoundException;
import cpu.spec.scraper.factory.ChromeDriverFactory;
import cpu.spec.scraper.factory.LoggerFactory;
import cpu.spec.scraper.file.CpuSpecificationWriter;
import cpu.spec.scraper.parser.CpuProductParser;
import cpu.spec.scraper.parser.CpuSeriesParser;
import cpu.spec.scraper.parser.CpuSpecificationParser;
import cpu.spec.scraper.utils.FileUtils;
import cpu.spec.scraper.utils.LogUtils;

public class IntelScraperApp {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String HOST_URL = "https://intel.com";

    public static void main(String[] args) throws DirectoryNotFoundException {
        LOGGER.info("Starting Intel Scraper.");
        String outputDir = FileUtils.getOutputDirectoryPath("dataset");
        String outputFile = "intel-cpus.csv";

        WebDriver driver = ChromeDriverFactory.getDriver();
        try {
            List<String> seriesLinks = CpuProductParser.extractSeriesLinks(driver);
            LOGGER.info("Extracted " + seriesLinks.size() + " CPU Series Links.");

            List<String> specificationLinks = extractSpecificationLinks(driver, seriesLinks);
            LOGGER.info("Extracted " + specificationLinks.size() + " CPU Specification Links.");

            List<CpuSpecificationModel> specifications = extractSpecifications(driver, specificationLinks);
            LOGGER.info("Extracted " + specifications.size() + " CPU Specifications.");

            CpuSpecificationWriter.writeCsvFile(specifications, outputDir + outputFile);
            LOGGER.info("Finished Intel Scraper. Output at: " + outputDir + outputFile);
        } catch (Exception e) {
            LOGGER.severe("Intel Scraper failed: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static List<String> extractSpecificationLinks(WebDriver driver, List<String> seriesLinks) {
        List<String> specificationLinks = new ArrayList<>();
        for (String link : seriesLinks) {
            String fullLink = HOST_URL + link;
            try {
                specificationLinks.addAll(CpuSeriesParser.extractSpecificationLinks(driver, fullLink));
            } catch (Exception e) {
                LOGGER.warning(LogUtils.exceptionMessage(e, fullLink));
            }
        }
        return specificationLinks;
    }

    private static List<CpuSpecificationModel> extractSpecifications(WebDriver driver, List<String> specificationLinks) {
        ProgressBar progressBar = new ProgressBar("Extracting specifications:", specificationLinks.size());
        List<CpuSpecificationModel> specifications = new ArrayList<>();
        for (String link : specificationLinks) {
            String fullLink = HOST_URL + link;
            try {
                specifications.add(CpuSpecificationParser.extractSpecification(driver, fullLink));
                if (specifications.size() % 250 == 0) {
                    LOGGER.info(LogUtils.progressMessage(specifications, specificationLinks, "CPU Specifications"));
                }
            } catch (Exception e) {
                LOGGER.warning(LogUtils.exceptionMessage(e, fullLink));
            }
            progressBar.step();
        }
        progressBar.close();
        return specifications;
    }
}
