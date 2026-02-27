# compare_dirs

Herramienta en Java 17 para comparar dos carpetas y reportar diferencias.

## Uso básico

```bash
mvn -q exec:java -Dexec.mainClass=com.comparedirs.CompareDirsApplication -Dexec.args="carpetaA carpetaB --exclude .git,target"
```

La salida por `stdout` es resumida (tipo de diferencia y ruta), sin comandos de sistema.

## Opciones

- `--exclude <dir1,dir2>`: excluye carpetas completas. Se puede repetir.
- `--file <ruta>` o `-f <ruta>`: guarda el informe en fichero.
- `--detail`: genera un informe detallado en el fichero de `--file`.
  - Incluye comandos sugeridos (`diff` / `cp`).
  - Para `CONTENT_MISMATCH`, intenta incluir también salida de `diff -u`.

## Ejemplos

Informe resumido por pantalla y en fichero:

```bash
mvn -q exec:java -Dexec.mainClass=com.comparedirs.CompareDirsApplication -Dexec.args="carpetaA carpetaB -f informe.txt"
```

Informe detallado en fichero:

```bash
mvn -q exec:java -Dexec.mainClass=com.comparedirs.CompareDirsApplication -Dexec.args="carpetaA carpetaB --exclude .git --file informe_detallado.txt --detail"
```
