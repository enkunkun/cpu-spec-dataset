package cpu.spec.scraper.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CpuSeriesParserTest {

    @Test
    void testExtractSpecificationLinks() throws Exception {
        List<String> actual = CpuSeriesParser.extractNavigationLinks("https://www.cpu-world.com/CPUs/Xeon/TYPE-Xeon Platinum.html");
        assertFalse(actual.isEmpty(), "actual is empty");
    }
}
