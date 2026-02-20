package com.ejemplo.comparedirs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Servicio para comparar recursivamente dos árboles de directorios.
 */
public final class DirectoryComparator {

    /**
     * Compara dos directorios y devuelve el informe de diferencias.
     *
     * @param leftDirectory directorio izquierdo
     * @param rightDirectory directorio derecho
     * @return informe con las diferencias detectadas
     */
    public ComparisonReport compare(Path leftDirectory, Path rightDirectory) {
        validateDirectory(leftDirectory, "leftDirectory");
        validateDirectory(rightDirectory, "rightDirectory");

        List<FileDifference> differences = new ArrayList<>();

        Map<Path, Path> leftEntries = collectEntries(leftDirectory, differences, "izquierdo");
        Map<Path, Path> rightEntries = collectEntries(rightDirectory, differences, "derecho");

        Set<Path> allRelativePaths = new TreeSet<>();
        allRelativePaths.addAll(leftEntries.keySet());
        allRelativePaths.addAll(rightEntries.keySet());

        for (Path relativePath : allRelativePaths) {
            Path leftPath = leftEntries.get(relativePath);
            Path rightPath = rightEntries.get(relativePath);

            if (leftPath == null) {
                differences.add(new FileDifference(
                        DifferenceType.ONLY_IN_RIGHT,
                        relativePath,
                        "Existe solo en el directorio derecho"
                ));
                continue;
            }

            if (rightPath == null) {
                differences.add(new FileDifference(
                        DifferenceType.ONLY_IN_LEFT,
                        relativePath,
                        "Existe solo en el directorio izquierdo"
                ));
                continue;
            }

            compareEntry(leftPath, rightPath, relativePath, differences);
        }

        return new ComparisonReport(differences);
    }

    private void compareEntry(Path leftPath, Path rightPath, Path relativePath, List<FileDifference> differences) {
        try {
            boolean leftIsDirectory = Files.isDirectory(leftPath);
            boolean rightIsDirectory = Files.isDirectory(rightPath);

            if (leftIsDirectory != rightIsDirectory) {
                differences.add(new FileDifference(
                        DifferenceType.TYPE_MISMATCH,
                        relativePath,
                        "En un directorio es carpeta y en el otro es fichero"
                ));
                return;
            }

            if (!leftIsDirectory && hasDifferentFileContent(leftPath, rightPath)) {
                differences.add(new FileDifference(
                        DifferenceType.CONTENT_MISMATCH,
                        relativePath,
                        "Contenido diferente entre ambos ficheros"
                ));
            }
        } catch (IOException exception) {
            differences.add(new FileDifference(
                    DifferenceType.ERROR,
                    relativePath,
                    "Error al comparar: " + exception.getMessage()
            ));
        }
    }

    private boolean hasDifferentFileContent(Path leftFile, Path rightFile) throws IOException {
        if (Files.size(leftFile) != Files.size(rightFile)) {
            return true;
        }
        return Files.mismatch(leftFile, rightFile) != -1L;
    }

    private Map<Path, Path> collectEntries(Path baseDirectory, List<FileDifference> differences, String sideLabel) {
        Map<Path, Path> entries = new HashMap<>();

        try (var walk = Files.walk(baseDirectory)) {
            walk.filter(path -> !path.equals(baseDirectory))
                    .forEach(path -> entries.put(baseDirectory.relativize(path), path));
        } catch (IOException exception) {
            differences.add(new FileDifference(
                    DifferenceType.ERROR,
                    Path.of("."),
                    "No se pudo leer el directorio " + sideLabel + ": " + exception.getMessage()
            ));
        }

        return entries;
    }

    private void validateDirectory(Path directory, String parameterName) {
        if (directory == null) {
            throw new IllegalArgumentException(parameterName + " no puede ser null");
        }

        if (!directory.isAbsolute()) {
            throw new IllegalArgumentException(parameterName + " debe ser una ruta absoluta");
        }

        if (!Files.exists(directory)) {
            throw new IllegalArgumentException(parameterName + " no existe: " + directory);
        }

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(parameterName + " no es un directorio: " + directory);
        }
    }
}
