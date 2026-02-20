package com.ejemplo.comparedirs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Informe inmutable con el resultado de comparar dos directorios.
 */
public final class ComparisonReport {
    private final List<FileDifference> differences;

    /**
     * Crea un informe a partir de una lista de diferencias.
     *
     * @param differences diferencias detectadas
     */
    public ComparisonReport(List<FileDifference> differences) {
        this.differences = Collections.unmodifiableList(new ArrayList<>(differences));
    }

    /**
     * @return {@code true} si no se detectaron diferencias
     */
    public boolean isIdentical() {
        return differences.isEmpty();
    }

    /**
     * @return lista inmutable de diferencias
     */
    public List<FileDifference> getDifferences() {
        return differences;
    }

    /**
     * Genera una representación en texto lista para consola.
     *
     * @return informe detallado
     */
    public String toDetailedText() {
        if (differences.isEmpty()) {
            return "No se detectaron diferencias. Los directorios son idénticos.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Se detectaron ").append(differences.size()).append(" diferencia(s):")
                .append(System.lineSeparator());

        for (FileDifference difference : differences) {
            builder.append("- [")
                    .append(difference.getType())
                    .append("] ")
                    .append(difference.getRelativePath())
                    .append(" -> ")
                    .append(difference.getDetails())
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }
}
