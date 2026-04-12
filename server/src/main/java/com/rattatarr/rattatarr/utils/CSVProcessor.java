package com.rattatarr.rattatarr.utils;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@NullMarked
public class CSVProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CSVProcessor.class);

    private CSVProcessor() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parses a CSV file using a custom record mapper function.
     *
     * @param file         The CSV file to parse
     * @param recordMapper Function to map each CSVRecord to a domain object
     * @param <T>          The type of objects to return
     * @return List of parsed objects
     * @throws CommonExceptions.InvalidRequestExceptions if file cannot be read
     * @example <pre>{@code
     * // Define a mapper function
     * private IMDbWatchlistRow mapImdbRecord(CSVRecord record) {
     *     String IMDbId = record.get("Const");
     *     Float rating = record.get("Your Rating").isBlank() ? null
     *                  : Float.parseFloat(record.get("Your Rating"));
     *     return new IMDbWatchlistRow(IMDbId, rating, record.get("Title Type"));
     * }
     *
     * // Use the parser
     * List<IMDbWatchlistRow> rows = CSVHelper.parseCSV(file, this::mapImdbRecord);
     * }</pre>
     */
    public static <T> List<T> parseCSV(
            MultipartFile file,
            Function<CSVRecord, Optional<T>> recordMapper
    ) {
        List<T> records = new ArrayList<>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));

            CSVParser parser = new CSVParser(
                    reader,
                    CSVFormat.DEFAULT.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .setIgnoreHeaderCase(true)
                            .setTrim(true)
                            .build()
            );

            for (CSVRecord record : parser) {
                try {
                    recordMapper.apply(record).ifPresent(records::add);
                } catch (Exception e) {
                    logger.error("Error reading CSV file: {}", e.getMessage());
                }
            }

        } catch (IOException e) {

            throw new CommonExceptions.InvalidRequestExceptions("Failed to read CSV file: " + e.getMessage());
        }

        logger.info("Parsed {} records from CSV file {}", records.size(), file.getOriginalFilename());
        return records;
    }
}
