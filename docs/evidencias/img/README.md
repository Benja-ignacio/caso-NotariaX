# Capturas de evidencias

Guardar aquí las capturas de pantalla de la entrega. No incluir contraseñas,
contenido del keystore ni datos personales reales.

## Archivos esperados

| Archivo | Qué debe mostrar |
|---|---|
| `apk-firmado.png` | Terminal con `apksigner verify --print-certs` y el APK release |
| `keystore.png` | Existencia del `.jks` y alias `notix`, **sin contraseñas** |
| `cobertura.png` | Reporte JaCoCo con cobertura de línea ≥ 80 % |
| `jvm-tests.png` | Resultado de pruebas JVM sin fallos |
| `dao-tests.png` | Resultado de pruebas DAO sin fallos |
| `trello.png` | Tablero Trello con columnas y tareas distribuidas |
| `commits.png` | Historial GitHub con commits de todos los integrantes |
| `backend.png` | Ambos servicios respondiendo en 8081 y 8082 |
| `login.png` | Login de la app |
| `firma-electronica.png` | Firma simulada |
| `bandeja-notario.png` | Bandeja del notario |
| `evaluacion-tramite.png` | Evaluación/aprobación del notario |
| `eliminar-tramite.png` | Eliminación de un trámite |
| `notificaciones.png` | Notificación local del sistema |
| `permitir-notificaciones.png` | Permiso de notificaciones |
| `perfil.png` | Perfil de usuario |
| `observaciones.png` / `cerrar-sesion.png` | Flujo de observaciones y logout |
| `ava-grupal.png` | Confirmación de entrega grupal AVA |
| `ava-individual.png` | Confirmación individual de cada integrante |

## Capturas desde el dispositivo

```bash
adb exec-out screencap -p > docs/evidencias/img/app-login.png
```

Repetir después de cada pantalla que se quiera evidenciar.
