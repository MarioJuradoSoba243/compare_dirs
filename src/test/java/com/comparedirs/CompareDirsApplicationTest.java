package com.comparedirs;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompareDirsApplicationTest {

    @Test
    void shouldParseExcludeFileAndDetailOptions() {
        CompareDirsApplication.CliArguments arguments = CompareDirsApplication.CliArguments.parse(
                new String[]{"left", "right", "--exclude", "target,.git", "--exclude", "build", "-f", "report.txt", "--detail"}
        );

        assertEquals(Path.of("left"), arguments.leftRoot());
        assertEquals(Path.of("right"), arguments.rightRoot());
        assertEquals(3, arguments.exclusions().size());
        assertTrue(arguments.exclusions().contains("target"));
        assertTrue(arguments.exclusions().contains(".git"));
        assertTrue(arguments.exclusions().contains("build"));
        assertTrue(arguments.outputFile().isPresent());
        assertEquals(Path.of("report.txt"), arguments.outputFile().orElseThrow());
        assertTrue(arguments.detail());
    }

    @Test
    void shouldParseWithoutOptionalOutputOptions() {
        CompareDirsApplication.CliArguments arguments = CompareDirsApplication.CliArguments.parse(
                new String[]{"left", "right", "--exclude", "target"}
        );

        assertFalse(arguments.outputFile().isPresent());
        assertFalse(arguments.detail());
    }

    @Test
    void shouldFailOnUnknownOption() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CompareDirsApplication.CliArguments.parse(new String[]{"left", "right", "--bad"}));

        assertTrue(exception.getMessage().contains("Unknown option"));
    }

    @Test
    void shouldFailWhenDetailWithoutFile() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CompareDirsApplication.CliArguments.parse(new String[]{"left", "right", "--detail"}));

        assertTrue(exception.getMessage().contains("requires --file"));
    }
}
