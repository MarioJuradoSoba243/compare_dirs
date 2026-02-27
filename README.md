# compare_dirs

Herramienta en Java 17 para comparar dos carpetas y reportar diferencias.

## Uso

```bash
mvn -q exec:java -Dexec.mainClass=com.comparedirs.CompareDirsApplication -Dexec.args="carpetaA carpetaB --exclude .git,target"
```

## Tipos de salida

- `CONTENT_MISMATCH`: muestra comando sugerido `diff "<archivo_en_A>" "<archivo_en_B>"`.
- `ONLY_LEFT`: muestra comando sugerido `cp "<archivo_en_A>" "<archivo_en_B>"`.
- `ONLY_RIGHT`: muestra comando sugerido `cp "<archivo_en_B>" "<archivo_en_A>"`.

## Exclusión de carpetas

Puedes repetir `--exclude` múltiples veces y también pasar varios valores separados por coma.
