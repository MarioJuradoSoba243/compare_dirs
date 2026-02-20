package com.ejemplo.comparedirs;

/**
 * Tipos de diferencias detectables durante la comparación.
 */
public enum DifferenceType {
    ONLY_IN_LEFT,
    ONLY_IN_RIGHT,
    TYPE_MISMATCH,
    CONTENT_MISMATCH,
    ERROR
}
