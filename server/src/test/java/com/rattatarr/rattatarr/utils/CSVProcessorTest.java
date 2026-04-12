package com.rattatarr.rattatarr.utils;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CSVProcessorTest {

    @Test
    void testConstructorThrowsException() {
        // When/Then
        assertThrows(Exception.class, () -> {
            var constructor = CSVProcessor.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testParseCSVWithValidCSV() {
        // Given
        String csvContent = """
                Name,Age,City
                John,30,New York
                Jane,25,Los Angeles
                Bob,35,Chicago
                """;

        MultipartFile file = new MockMultipartFile(
                "test.csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("John", result.get(0).name());
        assertEquals(30, result.get(0).age());
        assertEquals("New York", result.get(0).city());
        assertEquals("Jane", result.get(1).name());
        assertEquals("Bob", result.get(2).name());
    }

    @Test
    void testParseCSVWithEmptyCSV() {
        // Given
        String csvContent = """
                Name,Age,City
                """;

        MultipartFile file = new MockMultipartFile(
                "empty.csv",
                "empty.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseCSVWithInvalidFormat() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("Failed to read file"));

        // When/Then
        CommonExceptions.InvalidRequestExceptions exception = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> CSVProcessor.parseCSV(file, record -> Optional.of(new TestRecord("", 0, "")))
        );

        assertTrue(exception.getMessage().contains("Failed to read CSV file"));
    }

    @Test
    void testParseCSVWithCustomMapper() {
        // Given
        String csvContent = """
                FirstName,LastName,Score
                Alice,Smith,95
                Charlie,Brown,87
                """;

        MultipartFile file = new MockMultipartFile(
                "scores.csv",
                "scores.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<String> result = CSVProcessor.parseCSV(file, record -> {
            String firstName = record.get("FirstName");
            String lastName = record.get("LastName");
            int score = Integer.parseInt(record.get("Score"));
            return Optional.of(firstName + " " + lastName + " scored " + score);
        });

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice Smith scored 95", result.get(0));
        assertEquals("Charlie Brown scored 87", result.get(1));
    }

    @Test
    void testParseCSVWithMapperReturningEmpty() {
        // Given
        String csvContent = """
                Name,Age,City
                John,30,New York
                Invalid,not-a-number,Los Angeles
                Bob,35,Chicago
                """;

        MultipartFile file = new MockMultipartFile(
                "test.csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            try {
                String name = record.get("Name");
                int age = Integer.parseInt(record.get("Age"));
                String city = record.get("City");
                return Optional.of(new TestRecord(name, age, city));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).name());
        assertEquals("Bob", result.get(1).name());
    }

    @Test
    void testParseCSVWithMapperThrowingException() {
        // Given
        String csvContent = """
                Name,Age,City
                John,30,New York
                Invalid,not-a-number,Los Angeles
                Bob,35,Chicago
                """;

        MultipartFile file = new MockMultipartFile(
                "test.csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).name());
        assertEquals("Bob", result.get(1).name());
    }

    @Test
    void testParseCSVWithCaseInsensitiveHeaders() {
        // Given
        String csvContent = """
                NAME,age,CITY
                John,30,New York
                """;

        MultipartFile file = new MockMultipartFile(
                "test.csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).name());
    }

    @Test
    void testParseCSVWithWhitespace() {
        // Given
        String csvContent = """
                Name,Age,City
                  John  ,  30  ,  New York
                """;

        MultipartFile file = new MockMultipartFile(
                "test.csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.getFirst().name());
        assertEquals("New York", result.getFirst().city());
    }

    @Test
    void testParseCSVWithLargeCSV() {
        // Given
        StringBuilder csvContent = new StringBuilder("Name,Age,City\n");
        for (int i = 0; i < 1000; i++) {
            csvContent.append(String.format("Person%d,%d,City%d\n", i, 20 + (i % 50), i % 10));
        }

        MultipartFile file = new MockMultipartFile(
                "large.csv",
                "large.csv",
                "text/csv",
                csvContent.toString().getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertEquals(1000, result.size());
        assertEquals("Person0", result.get(0).name());
        assertEquals("Person999", result.get(999).name());
    }

    @Test
    void testParseCSVWithSpecialCharacters() {
        // Given
        String csvContent = """
                Name,Age,City
                "O'Brien, John",30,"New York, NY"
                """;

        MultipartFile file = new MockMultipartFile(
                "test.csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("O'Brien, John", result.getFirst().name());
        assertEquals("New York, NY", result.getFirst().city());
    }

    @Test
    void testParseCSVWithUTF8Characters() {
        // Given
        String csvContent = """
                Name,Age,City
                François,30,Montréal
                José,25,São Paulo
                """;

        MultipartFile file = new MockMultipartFile(
                "test.csv",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<TestRecord> result = CSVProcessor.parseCSV(file, record -> {
            String name = record.get("Name");
            int age = Integer.parseInt(record.get("Age"));
            String city = record.get("City");
            return Optional.of(new TestRecord(name, age, city));
        });

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("François", result.get(0).name());
        assertEquals("Montréal", result.get(0).city());
        assertEquals("José", result.get(1).name());
        assertEquals("São Paulo", result.get(1).city());
    }

    private record TestRecord(String name, int age, String city) {
    }
}
