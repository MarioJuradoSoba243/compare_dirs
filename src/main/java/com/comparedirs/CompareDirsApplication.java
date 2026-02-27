package com.comparedirs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
     * @param args CLI args: leftDir rightDir [--exclude dirA,dirB]... [--file out.txt|-f out.txt] [--detail]
     */
    public static void main(String[] args) {
        try {
            CliArguments cliArguments = CliArguments.parse(args);
            DirectoryComparator comparator = new DirectoryComparator();
            ReportWriter reportWriter = new ReportWriter();

            List<ComparisonDifference> differences = comparator.compare(
                    cliArguments.leftRoot(),
                    cliArguments.rightRoot(),
                    cliArguments.exclusions()
            );

            System.out.println(reportWriter.buildConsoleReport(differences));

            if (cliArguments.outputFile().isPresent()) {
                Path output = cliArguments.outputFile().orElseThrow();
                String reportContent = cliArguments.detail()
                        ? reportWriter.buildDetailedReport(differences, cliArguments.leftRoot(), cliArguments.rightRoot(), true)
                        : reportWriter.buildCompactFileReport(differences);
                reportWriter.writeReport(output, reportContent);
                System.out.println("Report written to: " + output);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error while comparing directories: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: compare-dirs <leftDir> <rightDir> [--exclude <dir1,dir2>]... [--file <path>|-f <path>] [--detail]");
    }

    /**
     * Immutable parsed CLI arguments.
     */
    public record CliArguments(Path leftRoot,
                               Path rightRoot,
                               Set<String> exclusions,
                               Optional<Path> outputFile,
                               boolean detail) {

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
            Path output = null;
            boolean detail = false;

            int index = 2;
            while (index < args.length) {
                String token = args[index];

                switch (token) {
                    case "--exclude" -> {
                        index++;
                        if (index >= args.length) {
                            throw new IllegalArgumentException("Missing value for --exclude option.");
                        }
                        String value = args[index];
                        Arrays.stream(value.split(","))
                                .map(String::trim)
                                .filter(v -> !v.isBlank())
                                .forEach(exclusions::add);
                    }
                    case "--file", "-f" -> {
                        index++;
                        if (index >= args.length) {
                            throw new IllegalArgumentException("Missing value for --file option.");
                        }
                        output = Path.of(args[index]);
                    }
                    case "--detail" -> detail = true;
                    default -> throw new IllegalArgumentException("Unknown option: " + token);
                }
                index++;
            }

            if (detail && output == null) {
                throw new IllegalArgumentException("--detail requires --file (or -f) to be provided.");
            }

            return new CliArguments(left, right, exclusions, Optional.ofNullable(output), detail);
        }

        public CliArguments {
            Objects.requireNonNull(leftRoot, "leftRoot cannot be null");
            Objects.requireNonNull(rightRoot, "rightRoot cannot be null");
            Objects.requireNonNull(exclusions, "exclusions cannot be null");
            Objects.requireNonNull(outputFile, "outputFile cannot be null");
        }
    }
}
