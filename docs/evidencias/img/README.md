# Capturas de evidencias

Guardar aquí las capturas de pantalla de la entrega. No incluir contraseñas,
contenido del keystore ni datos personales reales.

## Archivos esperados

| Archivo | Qué debe mostrar |
|---|---|
| `apk-firmado.png` | Terminal con `apksigner verify --print-certs` y el APK release |
| `keystore.png` | Existencia del `.jks` y alias `notix`, **sin contraseñas** |
| `cobertura.png` | Reporte JaCoCo con cobertura de línea ≥ 80 % |
| `tests.png` | Resultado de pruebas JVM y DAO sin fallos |
| `trello.png` | Tablero Trello con columnas y tareas distribuidas |
| `commits.png` | Historial GitHub con commits de todos los integrantes |
| `backend.png` | Ambos servicios respondiendo en 8081 y 8082 |
| `app-login.png` | Login de la app |
| `app-crear.png` | Creación o resumen de un trámite |
| `app-firma.png` | Firma simulada |
| `app-notario.png` | Revisión/aprobación del notario |
| `app-delete.png` | Eliminación de un trámite |
| `app-notificacion.png` | Notificación local del sistema |
| `app-perfil.png` | Perfil de usuario |
| `ava-grupal.png` | Confirmación de entrega grupal AVA |
| `ava-individual.png` | Confirmación individual de cada integrante |

## Capturas desde el dispositivo

```bash
adb exec-out screencap -p > docs/evidencias/img/app-login.png
```

Repetir después de cada pantalla que se quiera evidenciar.
