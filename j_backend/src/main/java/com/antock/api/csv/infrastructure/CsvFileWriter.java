package com.antock.api.csv.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@Component
public class CsvFileWriter {
    @Value("${csv.file-template}")
    private String fileTemplate;

    @Value("${file.upload-dir:}")
    private String uploadDir;

    public String getFilePath(String city, String district) {
        String fileName = String.format(fileTemplate, city, district);
        return uploadDir.isEmpty() ? fileName : Paths.get(uploadDir, fileName).toString();
    }

    public boolean fileExists(String city, String district) {
        return Files.exists(Paths.get(getFilePath(city, district)));
    }

    public void writeCsv(String city, String district, List<String> headers, List<Map<String, Object>> data) throws IOException {
        String path = getFilePath(city, district);
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(path), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write(String.join(",", headers));
            writer.write("\n");
            for (Map<String, Object> row : data) {
                List<String> values = headers.stream()
                        .map(h -> String.valueOf(row.getOrDefault(h, "")))
                        .toList();
                writer.write(String.join(",", values));
                writer.write("\n");
            }
        }
    }
}