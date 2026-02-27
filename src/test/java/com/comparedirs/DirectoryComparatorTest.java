package com.comparedirs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryComparatorTest {

    private Path tempRoot;
    private Path left;
    private Path right;

    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("compare-dirs-test");
        left = Files.createDirectories(tempRoot.resolve("left"));
        right = Files.createDirectories(tempRoot.resolve("right"));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempRoot != null && Files.exists(tempRoot)) {
            try (var walk = Files.walk(tempRoot)) {
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
    void shouldReportMismatchAndCopyCommands() throws IOException {
        Files.writeString(left.resolve("common.txt"), "left-content");
        Files.writeString(right.resolve("common.txt"), "right-content");

        Files.writeString(left.resolve("only-left.txt"), "x");
        Files.writeString(right.resolve("only-right.txt"), "y");

        DirectoryComparator comparator = new DirectoryComparator();
        List<ComparisonDifference> differences = comparator.compare(left, right, Set.of());

        assertEquals(3, differences.size());
        assertTrue(differences.stream().anyMatch(d -> d.type() == DifferenceType.CONTENT_MISMATCH
                && d.suggestedCommand(left, right).startsWith("diff ")));
        assertTrue(differences.stream().anyMatch(d -> d.type() == DifferenceType.ONLY_LEFT
                && d.suggestedCommand(left, right).startsWith("cp ")));
        assertTrue(differences.stream().anyMatch(d -> d.type() == DifferenceType.ONLY_RIGHT
                && d.suggestedCommand(left, right).startsWith("cp ")));
    }

    @Test
    void shouldExcludeDirectoriesByNameOrRelativePath() throws IOException {
        Files.createDirectories(left.resolve("build/tmp"));
        Files.createDirectories(right.resolve("build/tmp"));
        Files.writeString(left.resolve("build/tmp/data.txt"), "left");
        Files.writeString(right.resolve("build/tmp/data.txt"), "right");

        Files.createDirectories(left.resolve("logs"));
        Files.writeString(left.resolve("logs/app.log"), "left-log");

        DirectoryComparator comparator = new DirectoryComparator();
        List<ComparisonDifference> differences = comparator.compare(left, right, Set.of("build", "logs"));

        assertTrue(differences.isEmpty());
    }
}
