package com.ejemplo.comparedirs;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Representa una diferencia concreta entre dos directorios para una ruta relativa.
 */
public final class FileDifference {
    private final DifferenceType type;
    private final Path relativePath;
    private final String details;

    /**
     * Construye una diferencia.
     *
     * @param type tipo de diferencia
     * @param relativePath ruta relativa dentro del árbol comparado
     * @param details detalle adicional
     */
    public FileDifference(DifferenceType type, Path relativePath, String details) {
        this.type = Objects.requireNonNull(type, "type no puede ser null");
        this.relativePath = Objects.requireNonNull(relativePath, "relativePath no puede ser null");
        this.details = details == null ? "" : details;
    }

    public DifferenceType getType() {
        return type;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public String getDetails() {
        return details;
    }
}
