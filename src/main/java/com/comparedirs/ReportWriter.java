package com.comparedirs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds and persists comparison reports.
 */
public class ReportWriter {

    /**
     * Creates a concise report suitable for stdout.
     *
     * @param differences differences found
     * @return concise report text
     */
    public String buildConsoleReport(List<ComparisonDifference> differences) {
        if (differences.isEmpty()) {
            return "No differences found.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Found ").append(differences.size()).append(" difference(s):").append(System.lineSeparator());
        for (ComparisonDifference difference : differences) {
            builder.append("- ")
                    .append(difference.type())
                    .append(" | ")
                    .append(difference.relativePath())
                    .append(System.lineSeparator());
        }
        return builder.toString().stripTrailing();
    }

    /**
     * Creates a full report that includes suggested commands and optional diff output.
     *
     * @param differences differences found
     * @param leftRoot left root directory
     * @param rightRoot right root directory
     * @param includeDiffOutput whether to include diff command output for content mismatches
     * @return detailed report text
     */
    public String buildDetailedReport(List<ComparisonDifference> differences,
                                      Path leftRoot,
                                      Path rightRoot,
                                      boolean includeDiffOutput) {
        StringBuilder builder = new StringBuilder();
        builder.append("Directory comparison report").append(System.lineSeparator())
                .append("Left : ").append(leftRoot).append(System.lineSeparator())
                .append("Right: ").append(rightRoot).append(System.lineSeparator())
                .append("Total differences: ").append(differences.size()).append(System.lineSeparator())
                .append(System.lineSeparator());

        if (differences.isEmpty()) {
            builder.append("No differences found.").append(System.lineSeparator());
            return builder.toString();
        }

        for (ComparisonDifference difference : differences) {
            builder.append("Type: ").append(difference.type()).append(System.lineSeparator())
                    .append("Path: ").append(difference.relativePath()).append(System.lineSeparator())
                    .append("Command: ").append(difference.suggestedCommand(leftRoot, rightRoot)).append(System.lineSeparator());

            if (includeDiffOutput && difference.type() == DifferenceType.CONTENT_MISMATCH) {
                builder.append("Diff output:").append(System.lineSeparator())
                        .append(runDiffCommand(leftRoot.resolve(difference.relativePath()),
                                rightRoot.resolve(difference.relativePath())))
                        .append(System.lineSeparator());
            }

            builder.append(System.lineSeparator());
        }

        return builder.toString().stripTrailing() + System.lineSeparator();
    }

    /**
     * Writes report content to the provided path.
     *
     * @param outputFile target report path
     * @param reportContent report content
     * @throws IOException if writing fails
     */
    public void writeReport(Path outputFile, String reportContent) throws IOException {
        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputFile, reportContent, StandardCharsets.UTF_8);
    }

    private String runDiffCommand(Path leftFile, Path rightFile) {
        ProcessBuilder processBuilder = new ProcessBuilder("diff", "-u", leftFile.toString(), rightFile.toString());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output;
            try (var stream = process.getInputStream()) {
                output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "(no textual differences reported by diff)";
            }
            if (exitCode == 1) {
                return output.isBlank() ? "(diff produced no output)" : output.stripTrailing();
            }
            return "(diff command failed with exit code " + exitCode + ")";
        } catch (IOException e) {
            return "(diff command unavailable: " + e.getMessage() + ")";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "(diff command interrupted)";
        }
    }

    /**
     * Creates a compact file report without command details.
     *
     * @param differences differences found
     * @return compact report
     */
    public String buildCompactFileReport(List<ComparisonDifference> differences) {
        return buildConsoleReport(new ArrayList<>(differences)) + System.lineSeparator();
    }
}
