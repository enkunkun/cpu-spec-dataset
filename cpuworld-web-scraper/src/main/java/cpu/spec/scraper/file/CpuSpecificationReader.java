package cpu.spec.scraper.file;

import cpu.spec.scraper.CpuSpecificationModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class CpuSpecificationReader {

    /**
     * @param filePath like "../dataset/manufacturer-cpus.csv"
     * @return list of CpuSpecificationModel read from the CSV, or empty list if file does not exist
     * @throws IOException if the file exists but cannot be read
     */
    public static List<CpuSpecificationModel> readCsvFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        List<String> lines = Files.readAllLines(path);
        if (lines.size() < 2) {
            return new ArrayList<>();
        }

        String headerLine = lines.get(0);
        List<String> headers = parseCsvColumns(headerLine);

        List<CpuSpecificationModel> specifications = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            CpuSpecificationModel spec = parseLine(headers, line);
            if (spec != null) {
                specifications.add(spec);
            }
        }
        return specifications;
    }

    private static CpuSpecificationModel parseLine(List<String> headers, String line) {
        List<String> values = parseCsvColumns(line);
        if (values.size() < 3) {
            return null;
        }

        CpuSpecificationModel spec = new CpuSpecificationModel();
        spec.id = values.get(0);
        spec.cpuName = stripQuotes(values.get(1));
        spec.sourceUrl = stripQuotes(values.get(values.size() - 1));

        for (int i = 2; i < values.size() - 1 && i < headers.size() - 1; i++) {
            String value = values.get(i);
            if (value != null && !value.isEmpty()) {
                spec.dataValues.put(headers.get(i), value);
            }
        }
        return spec;
    }

    private static List<String> parseCsvColumns(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                columns.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        columns.add(current.toString().trim());
        return columns;
    }

    private static String stripQuotes(String value) {
        if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
