package com.comparedirs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * CLI entry point for directory comparison.
 */
public final class CompareDirsApplication {

    private CompareDirsApplication() {
    }

    /**
     * Program entry point.
     *
     * @param args CLI args: leftDir rightDir [--exclude dirA,dirB]...
     */
    public static void main(String[] args) {
        try {
            CliArguments cliArguments = CliArguments.parse(args);
            DirectoryComparator comparator = new DirectoryComparator();
            List<ComparisonDifference> differences = comparator.compare(
                    cliArguments.leftRoot(),
                    cliArguments.rightRoot(),
                    cliArguments.exclusions()
            );

            printReport(differences, cliArguments.leftRoot(), cliArguments.rightRoot());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error while comparing directories: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void printReport(List<ComparisonDifference> differences, Path leftRoot, Path rightRoot) {
        if (differences.isEmpty()) {
            System.out.println("No differences found.");
            return;
        }

        System.out.printf("Found %d difference(s):%n", differences.size());
        for (ComparisonDifference difference : differences) {
            System.out.printf("- %s | %s%n  Command: %s%n",
                    difference.type(),
                    difference.relativePath(),
                    difference.suggestedCommand(leftRoot, rightRoot));
        }
    }

    private static void printUsage() {
        System.err.println("Usage: compare-dirs <leftDir> <rightDir> [--exclude <dir1,dir2>]...");
    }

    /**
     * Immutable parsed CLI arguments.
     */
    public record CliArguments(Path leftRoot, Path rightRoot, Set<String> exclusions) {

        /**
         * Parses CLI arguments.
         *
         * @param args raw CLI args
         * @return parsed arguments
         */
        public static CliArguments parse(String[] args) {
            if (args == null || args.length < 2) {
                throw new IllegalArgumentException("At least two directory arguments are required.");
            }

            Path left = Path.of(args[0]);
            Path right = Path.of(args[1]);
            Set<String> exclusions = new LinkedHashSet<>();

            int index = 2;
            while (index < args.length) {
                String token = args[index];
                if (!"--exclude".equals(token)) {
                    throw new IllegalArgumentException("Unknown option: " + token);
                }
                index++;
                if (index >= args.length) {
                    throw new IllegalArgumentException("Missing value for --exclude option.");
                }

                String value = args[index];
                Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(v -> !v.isBlank())
                        .forEach(exclusions::add);
                index++;
            }

            return new CliArguments(left, right, exclusions);
        }
    }
}
