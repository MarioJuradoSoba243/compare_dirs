package com.ejemplo.comparedirs;

import java.nio.file.Path;

/**
 * Punto de entrada CLI para comparar dos directorios por contenido.
 */
public final class DirectoryCompareApplication {

    private DirectoryCompareApplication() {
        // Clase utilitaria.
    }

    /**
     * Uso: java ...DirectoryCompareApplication <ruta-absoluta-dir-1> <ruta-absoluta-dir-2>
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(2);
        }

        Path leftDirectory = Path.of(args[0]);
        Path rightDirectory = Path.of(args[1]);

        try {
            DirectoryComparator comparator = new DirectoryComparator();
            ComparisonReport report = comparator.compare(leftDirectory, rightDirectory);
            System.out.println(report.toDetailedText());
            System.exit(report.isIdentical() ? 0 : 1);
        } catch (IllegalArgumentException exception) {
            System.err.println("Error de validación: " + exception.getMessage());
            printUsage();
            System.exit(2);
        }
    }

    private static void printUsage() {
        System.out.println("Uso: java -jar compare-dirs.jar <ruta-absoluta-dir-1> <ruta-absoluta-dir-2>");
    }
}
