package com.comparedirs;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a difference found while comparing two directories.
 *
 * @param relativePath path relative to compared roots
 * @param type difference type
 */
public record ComparisonDifference(Path relativePath, DifferenceType type) {

    public ComparisonDifference {
        Objects.requireNonNull(relativePath, "relativePath cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
    }

    /**
     * Builds a shell command suggestion that can help fix or inspect this difference.
     *
     * @param leftRoot left directory root
     * @param rightRoot right directory root
     * @return shell command suggestion
     */
    public String suggestedCommand(Path leftRoot, Path rightRoot) {
        Path leftPath = leftRoot.resolve(relativePath);
        Path rightPath = rightRoot.resolve(relativePath);

        return switch (type) {
            case CONTENT_MISMATCH -> String.format("diff \"%s\" \"%s\"", leftPath, rightPath);
            case ONLY_LEFT -> String.format("cp \"%s\" \"%s\"", leftPath, rightPath);
            case ONLY_RIGHT -> String.format("cp \"%s\" \"%s\"", rightPath, leftPath);
        };
    }
}
