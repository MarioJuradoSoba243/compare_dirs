# compare_dirs

Aplicación Java 17 para comparar dos directorios recursivamente y generar un informe detallado de diferencias.

## Qué detecta

- Ficheros/directorios que existen solo en el directorio izquierdo.
- Ficheros/directorios que existen solo en el directorio derecho.
- Diferencias de tipo (fichero vs carpeta).
- Diferencias de contenido en ficheros con la misma ruta relativa.
- Errores de lectura durante el proceso.

## Compilar y ejecutar

```bash
mvn clean package
java -cp target/compare-dirs-1.0.0.jar com.ejemplo.comparedirs.DirectoryCompareApplication /ruta/absoluta/dir1 /ruta/absoluta/dir2
```

Códigos de salida:

- `0`: directorios idénticos.
- `1`: se encontraron diferencias.
- `2`: error de uso/validación de parámetros.
