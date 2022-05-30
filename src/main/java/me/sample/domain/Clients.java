package me.sample.domain;

import lombok.extern.slf4j.Slf4j;
import me.sample.web.rest.ContentTypes;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public final class Clients {

    private static final Pattern PATTERN_CLIENT_ID = Pattern.compile("\\s*(?<clientId>\\d+)\\s*");

    public static List<String> parseClientIds(String contentType, byte[] data) {
        if (contentType == null) {
            throw new UnsupportedOperationException("No content type provided for client data");
        }

        switch (contentType) {
            case ContentTypes.TXT:
            case ContentTypes.CSV:
                return readClientIdsFromCsv(new ByteArrayInputStream(data));
            case ContentTypes.XLS:
            case ContentTypes.XLSX:
                return readClientIdsFromXls(new ByteArrayInputStream(data));
            default:
                throw new UnsupportedOperationException(String.format(
                        "Unsupported content type provided for client data: %s",
                        contentType));
        }
    }

    public static List<String> readClientIdsFromCsv(InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines()
                    .map(Clients::parseClientId)
                    .filter((String line) -> line != null && !line.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static List<String> readClientIdsFromXls(InputStream in) {
        try (Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);

            List<String> results = new LinkedList<>();
            sheet.forEach((Row row) -> {
                Cell cell = row.getCell(0);
                CellType cellType = cell.getCellType();
                String result;
                switch (cellType) {
                    case BLANK:
                        return;
                    case STRING:
                        result = parseClientId(cell.getStringCellValue());
                        if (result != null && !result.trim().isEmpty()) {
                            results.add(result);
                        }

                        return;
                    case NUMERIC:
                        double value = cell.getNumericCellValue();
                        if (value == (long) value) {
                            result = parseClientId(String.valueOf(Double.valueOf(value).longValue()));
                            if (result != null && !result.trim().isEmpty()) {
                                results.add(result);
                            }
                        }

                        return;
                    default:
                        throw new RuntimeException(String.format("Unsupported cell type: %s", cellType));
                }
            });

            return results;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    static String parseClientId(String string) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }

        String sanitized = string.replaceAll("[\uFEFF-\uFFFF]", "");
        Matcher matcher = PATTERN_CLIENT_ID.matcher(sanitized);
        if (matcher.matches()) {
            return matcher.group("clientId");
        }

        log.warn("Failed to parse client id from string: \"{}\"", sanitized);

        return null;
    }
}
