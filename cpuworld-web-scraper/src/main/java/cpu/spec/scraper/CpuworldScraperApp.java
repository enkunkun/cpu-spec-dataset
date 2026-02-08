package cpu.spec.scraper;

import cpu.spec.scraper.exception.DirectoryNotFoundException;
import cpu.spec.scraper.factory.ChromeDriverFactory;
import cpu.spec.scraper.factory.LoggerFactory;
import cpu.spec.scraper.file.CpuSpecificationReader;
import cpu.spec.scraper.file.CpuSpecificationWriter;
import cpu.spec.scraper.parser.CpuSeriesParser;
import cpu.spec.scraper.parser.CpuSpecificationParser;
import cpu.spec.scraper.utils.FileUtils;
import cpu.spec.scraper.utils.LogUtils;
import cpu.spec.scraper.utils.TimeUtils;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

public class CpuworldScraperApp {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String HOST_URL = "https://www.cpu-world.com";

    public static void main(String[] args) throws DirectoryNotFoundException, IOException {
        LOGGER.info("Starting Cpu World Scraper.");
        String outputDir = FileUtils.getOutputDirectoryPath("dataset");
        String outputPath = outputDir + "cpuworld-cpus.csv";
        String progressFile = outputDir + "cpuworld-progress.txt";

        WebDriver driver = ChromeDriverFactory.getDriver();
        try {
            List<CpuSpecificationModel> specifications = extractXeonPlatinumAndXeonGoldSpecifications(driver, outputPath, progressFile);
            CpuSpecificationWriter.writeCsvFile(specifications, outputPath);
            LOGGER.info("Finished Cpu World Scraper. Output at: " + outputPath);
        } finally {
            driver.quit();
        }
    }

    private static List<CpuSpecificationModel> extractXeonPlatinumAndXeonGoldSpecifications(WebDriver driver, String outputPath, String progressFile) {
        List<String> familyLinks = List.of(
                "https://www.cpu-world.com/CPUs/Xeon/TYPE-Xeon Platinum.html",
                "https://www.cpu-world.com/CPUs/Xeon/TYPE-Xeon Gold.html");
        LOGGER.info("Given " + familyLinks.size() + " Family Links.");

        List<String> specificationHrefs = extractNavigationLinks(driver, familyLinks);
        List<String> specificationLinks = specificationHrefs.stream().map(href -> HOST_URL + href).toList();
        LOGGER.info("Extracted " + specificationLinks.size() + " Specification Links.");

        List<CpuSpecificationModel> specifications = extractSpecifications(driver, specificationLinks, outputPath, progressFile);
        LOGGER.info("Extracted " + specifications.size() + " of " + specificationLinks.size() + " CPU Specifications.");
        return specifications;
    }

    private static List<CpuSpecificationModel> extractSelectedCpuSpecifications(WebDriver driver, String outputPath, String progressFile) {
        List<String> specificationLinks = List.of(
                "https://www.cpu-world.com/CPUs/Xeon/Intel-Xeon 8272CL.html",
                "https://www.cpu-world.com/CPUs/Xeon/Intel-Xeon 8370C.html",
                "https://www.cpu-world.com/CPUs/Xeon/Intel-Xeon 8373C.html",
                "https://www.cpu-world.com/CPUs/Xeon/Intel-Xeon 6268CL.html");
        LOGGER.info("Given " + specificationLinks.size() + " Specification Links.");

        List<CpuSpecificationModel> specifications = extractSpecifications(driver, specificationLinks, outputPath, progressFile);
        LOGGER.info("Extracted " + specifications.size() + " CPU Specifications.");
        return specifications;
    }

    private static List<String> extractNavigationLinks(WebDriver driver, List<String> inputLinks) {
        List<String> outputLinks = new ArrayList<>();
        for (String link : inputLinks) {
            try {
                outputLinks.addAll(CpuSeriesParser.extractNavigationLinks(driver, link));
            } catch (Exception e) {
                LOGGER.warning(LogUtils.exceptionMessage(e, link));
            }
        }
        return outputLinks;
    }

    private static List<CpuSpecificationModel> extractSpecifications(WebDriver driver, List<String> specificationLinks, String outputPath, String progressFile) {
        List<CpuSpecificationModel> specifications = new ArrayList<>();
        Set<String> completedUrls = loadCompletedUrls(progressFile);

        if (!completedUrls.isEmpty()) {
            try {
                List<CpuSpecificationModel> existing = CpuSpecificationReader.readCsvFile(outputPath);
                specifications.addAll(existing);
                LOGGER.info("Loaded " + existing.size() + " existing specs from CSV.");
            } catch (IOException e) {
                LOGGER.warning("Failed to load existing CSV: " + e.getMessage());
            }
        }

        int skipped = 0;
        for (String link : specificationLinks) {
            if (completedUrls.contains(link)) {
                skipped++;
                continue;
            }
            try {
                TimeUtils.sleepBetween(10000, 3000);
                specifications.add(CpuSpecificationParser.extractSpecification(driver, link));
                saveCompletedUrl(progressFile, link);
                saveProgress(specifications, outputPath);
                LOGGER.info(LogUtils.progressMessage(specifications, specificationLinks,
                        "CPU Specifications (skipped " + skipped + " already done)"));

            } catch (Exception e) {
                LOGGER.warning(LogUtils.exceptionMessage(e, link));
                LOGGER.info("Retrying extraction of: " + link);
                try {
                    specifications.add(CpuSpecificationParser.extractSpecification(driver, link));
                    saveCompletedUrl(progressFile, link);
                    saveProgress(specifications, outputPath);
                } catch (Exception ex) {
                    LOGGER.severe(LogUtils.exceptionMessage(e, link));
                }
            }
        }

        if (skipped > 0) {
            LOGGER.info("Skipped " + skipped + " already completed URLs (resume).");
        }
        return specifications;
    }

    private static Set<String> loadCompletedUrls(String progressFile) {
        try {
            Path path = Path.of(progressFile);
            if (Files.exists(path)) {
                Set<String> urls = new HashSet<>(Files.readAllLines(path));
                LOGGER.info("Loaded " + urls.size() + " completed URLs from progress file.");
                return urls;
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to load progress file: " + e.getMessage());
        }
        return new HashSet<>();
    }

    private static void saveCompletedUrl(String progressFile, String url) {
        try {
            Files.writeString(Path.of(progressFile), url + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.warning("Failed to save progress: " + e.getMessage());
        }
    }

    private static void saveProgress(List<CpuSpecificationModel> specifications, String outputPath) {
        try {
            CpuSpecificationWriter.writeCsvFile(specifications, outputPath);
        } catch (IOException e) {
            LOGGER.warning("Failed to save CSV: " + e.getMessage());
        }
    }
}
