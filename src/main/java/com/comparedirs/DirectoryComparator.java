package com.comparedirs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Compares two directory trees and reports differences.
 */
public class DirectoryComparator {

    /**
     * Compares two directories recursively.
     *
     * @param leftRoot left root directory
     * @param rightRoot right root directory
     * @param excludedDirectories directories to exclude, by name or relative path
     * @return sorted list of differences
     * @throws IOException if a filesystem operation fails
     */
    public List<ComparisonDifference> compare(Path leftRoot,
                                              Path rightRoot,
                                              Set<String> excludedDirectories) throws IOException {
        validateRoot(leftRoot, "leftRoot");
        validateRoot(rightRoot, "rightRoot");

        Set<String> exclusions = normalizeExclusions(excludedDirectories);
        Set<Path> allFiles = new HashSet<>();

        collectFiles(leftRoot, exclusions, allFiles);
        collectFiles(rightRoot, exclusions, allFiles);

        List<ComparisonDifference> differences = new ArrayList<>();
        for (Path relative : allFiles) {
            Path leftFile = leftRoot.resolve(relative);
            Path rightFile = rightRoot.resolve(relative);

            boolean leftExists = Files.exists(leftFile);
            boolean rightExists = Files.exists(rightFile);

            if (leftExists && rightExists) {
                if (Files.isRegularFile(leftFile) && Files.isRegularFile(rightFile)
                        && Files.mismatch(leftFile, rightFile) != -1L) {
                    differences.add(new ComparisonDifference(relative, DifferenceType.CONTENT_MISMATCH));
                }
            } else if (leftExists) {
                differences.add(new ComparisonDifference(relative, DifferenceType.ONLY_LEFT));
            } else if (rightExists) {
                differences.add(new ComparisonDifference(relative, DifferenceType.ONLY_RIGHT));
            }
        }

        differences.sort(Comparator.comparing((ComparisonDifference d) -> d.relativePath().toString())
                .thenComparing(d -> d.type().name()));
        return differences;
    }

    private void validateRoot(Path root, String name) {
        Objects.requireNonNull(root, name + " cannot be null");
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException(name + " must be an existing directory: " + root);
        }
    }

    private Set<String> normalizeExclusions(Set<String> excludedDirectories) {
        Set<String> normalized = new HashSet<>();
        if (excludedDirectories == null) {
            return normalized;
        }

        for (String exclusion : excludedDirectories) {
            if (exclusion == null || exclusion.isBlank()) {
                continue;
            }
            String cleaned = exclusion.trim().replace('\\', '/');
            while (cleaned.startsWith("./")) {
                cleaned = cleaned.substring(2);
            }
            while (cleaned.endsWith("/")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
            if (!cleaned.isBlank()) {
                normalized.add(cleaned);
            }
        }

        return normalized;
    }

    private void collectFiles(Path root, Set<String> exclusions, Set<Path> accumulator) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> !path.equals(root))
                    .filter(path -> !Files.isDirectory(path))
                    .map(root::relativize)
                    .filter(relative -> !isExcluded(relative, exclusions))
                    .forEach(accumulator::add);
        }
    }

    private boolean isExcluded(Path relativePath, Set<String> exclusions) {
        if (exclusions.isEmpty()) {
            return false;
        }

        String normalizedPath = relativePath.toString().replace('\\', '/');
        String[] parts = normalizedPath.split("/");

        for (String exclusion : exclusions) {
            if (normalizedPath.equals(exclusion) || normalizedPath.startsWith(exclusion + "/")) {
                return true;
            }
            for (String part : parts) {
                if (part.equals(exclusion)) {
                    return true;
                }
            }
        }
        return false;
    }
}
