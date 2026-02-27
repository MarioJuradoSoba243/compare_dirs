package com.comparedirs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportWriterTest {

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("report-writer-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (var walk = Files.walk(tempDir)) {
                walk.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // no-op in test cleanup
                            }
                        });
            }
        }
    }

    @Test
    void consoleReportShouldNotIncludeCommands() {
        ReportWriter writer = new ReportWriter();
        List<ComparisonDifference> differences = List.of(
                new ComparisonDifference(Path.of("a.txt"), DifferenceType.CONTENT_MISMATCH),
                new ComparisonDifference(Path.of("b.txt"), DifferenceType.ONLY_LEFT)
        );

        String report = writer.buildConsoleReport(differences);

        assertTrue(report.contains("CONTENT_MISMATCH"));
        assertTrue(report.contains("ONLY_LEFT"));
        assertFalse(report.contains("Command:"));
        assertFalse(report.contains("diff \""));
        assertFalse(report.contains("cp \""));
    }

    @Test
    void detailedReportShouldIncludeCommandsAndDiffOutputSection() throws IOException {
        Path left = Files.createDirectories(tempDir.resolve("left"));
        Path right = Files.createDirectories(tempDir.resolve("right"));

        Files.writeString(left.resolve("common.txt"), "line-1-left\nline-2\n");
        Files.writeString(right.resolve("common.txt"), "line-1-right\nline-2\n");

        ReportWriter writer = new ReportWriter();
        List<ComparisonDifference> differences = List.of(
                new ComparisonDifference(Path.of("common.txt"), DifferenceType.CONTENT_MISMATCH)
        );

        String report = writer.buildDetailedReport(differences, left, right, true);

        assertTrue(report.contains("Command: diff \""));
        assertTrue(report.contains("Diff output:"));
    }

    @Test
    void shouldWriteReportToFile() throws IOException {
        ReportWriter writer = new ReportWriter();
        Path output = tempDir.resolve("out/report.txt");

        writer.writeReport(output, "hello");

        assertTrue(Files.exists(output));
        assertTrue(Files.readString(output).contains("hello"));
    }
}
