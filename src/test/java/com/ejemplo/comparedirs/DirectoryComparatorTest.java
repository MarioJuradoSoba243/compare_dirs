package com.ejemplo.comparedirs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryComparatorTest {

    private Path tempRoot;
    private DirectoryComparator comparator;

    @BeforeEach
    void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("compare-dirs-test-");
        comparator = new DirectoryComparator();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempRoot != null && Files.exists(tempRoot)) {
            try (var paths = Files.walk(tempRoot)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // limpieza best effort en tests
                            }
                        });
            }
        }
    }

    @Test
    void shouldReturnIdenticalWhenDirectoriesMatch() throws IOException {
        Path left = Files.createDirectory(tempRoot.resolve("left"));
        Path right = Files.createDirectory(tempRoot.resolve("right"));

        writeFile(left.resolve("a.txt"), "hola");
        writeFile(right.resolve("a.txt"), "hola");

        Files.createDirectories(left.resolve("sub"));
        Files.createDirectories(right.resolve("sub"));
        writeFile(left.resolve("sub/b.txt"), "mundo");
        writeFile(right.resolve("sub/b.txt"), "mundo");

        ComparisonReport report = comparator.compare(left, right);

        assertTrue(report.isIdentical());
        assertTrue(report.getDifferences().isEmpty());
    }

    @Test
    void shouldDetectMissingFileInRightDirectory() throws IOException {
        Path left = Files.createDirectory(tempRoot.resolve("left2"));
        Path right = Files.createDirectory(tempRoot.resolve("right2"));

        writeFile(left.resolve("solo-izquierda.txt"), "dato");

        ComparisonReport report = comparator.compare(left, right);

        assertFalse(report.isIdentical());
        assertEquals(1, report.getDifferences().size());
        assertEquals(DifferenceType.ONLY_IN_LEFT, report.getDifferences().get(0).getType());
    }

    @Test
    void shouldDetectDifferentContent() throws IOException {
        Path left = Files.createDirectory(tempRoot.resolve("left3"));
        Path right = Files.createDirectory(tempRoot.resolve("right3"));

        writeFile(left.resolve("mismo-nombre.txt"), "contenido A");
        writeFile(right.resolve("mismo-nombre.txt"), "contenido B");

        ComparisonReport report = comparator.compare(left, right);

        assertFalse(report.isIdentical());
        assertEquals(1, report.getDifferences().size());
        assertEquals(DifferenceType.CONTENT_MISMATCH, report.getDifferences().get(0).getType());
    }

    @Test
    void shouldFailWhenPathIsNotAbsolute() {
        Path absolute = tempRoot.toAbsolutePath();
        Path relative = Path.of("relativo");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comparator.compare(relative, absolute)
        );

        assertTrue(exception.getMessage().contains("ruta absoluta"));
    }

    private void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
