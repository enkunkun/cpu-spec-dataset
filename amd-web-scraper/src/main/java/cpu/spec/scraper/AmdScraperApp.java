package cpu.spec.scraper;

import cpu.spec.scraper.factory.ChromeDriverFactory;
import cpu.spec.scraper.factory.LoggerFactory;
import cpu.spec.scraper.utils.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AmdScraperApp {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String HOST_URL = "https://www.amd.com/en";
    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(60);

    private static final Map<String, String> PAGES = new LinkedHashMap<>();

    static {
        PAGES.put("specifications/processors.html", "amd-cpus.csv");
        PAGES.put("specifications/server-processor.html", "amd-server-cpus.csv");
        PAGES.put("specifications/embedded.html", "amd-embedded-cpus.csv");
    }

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting AMD Scraper.");
        String outputDir = FileUtils.getOutputDirectoryPath("dataset");
        Path downloadDir = Files.createTempDirectory("amd-scraper-");

        WebDriver driver = ChromeDriverFactory.getDriver(downloadDir.toString());
        try {
            for (Map.Entry<String, String> entry : PAGES.entrySet()) {
                String pageUrl = HOST_URL + "/" + entry.getKey();
                String outputFile = entry.getValue();
                LOGGER.info("Downloading CSV from: " + pageUrl);

                downloadCsv(driver, pageUrl, downloadDir, outputDir + outputFile);
                LOGGER.info("Saved: " + outputDir + outputFile);
            }
        } finally {
            driver.quit();
            deleteDirectory(downloadDir.toFile());
        }
        LOGGER.info("Finished AMD Scraper.");
    }

    private static void downloadCsv(WebDriver driver, String url, Path downloadDir, String outputPath) throws Exception {
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, PAGE_TIMEOUT);
        WebElement csvButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".buttons-csv")));
        csvButton.click();

        File downloaded = waitForDownload(downloadDir);
        Files.move(downloaded.toPath(), Path.of(outputPath), StandardCopyOption.REPLACE_EXISTING);
    }

    private static File waitForDownload(Path downloadDir) throws Exception {
        long deadline = System.currentTimeMillis() + DOWNLOAD_TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            File[] files = downloadDir.toFile().listFiles((dir, name) -> name.endsWith(".csv"));
            if (files != null && files.length > 0) {
                return files[0];
            }
            Thread.sleep(500);
        }
        throw new RuntimeException("CSV download timed out in: " + downloadDir);
    }

    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        dir.delete();
    }
}
