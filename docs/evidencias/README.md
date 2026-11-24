# Evidencias de entrega — Notix

> Carpeta para capturas y respaldos de la EP4. No inventar capturas ni datos del equipo.
> El APK y el keystore no deben publicarse en GitHub.

## Archivos esperados

| Archivo | Contenido |
|---|---|
| `img/apk-firmado.png` | Captura de `apksigner verify` y del APK release |
| `img/keystore.png` | Existencia y alias del keystore, sin contraseñas |
| `img/trello.png` | Captura del tablero Trello con columnas y tareas |
| `img/cobertura.png` | Captura del reporte JaCoCo ≥ 80 % |
| `img/jvm-tests.png` | Pruebas JVM sin fallos |
| `img/dao-tests.png` | Pruebas DAO instrumentadas sin fallos |
| `img/commits.png` | Captura de commits de todos los integrantes en GitHub |
| `img/trello.png` | Tablero Trello |
| `img/backend.png` | Ambos servicios respondiendo |
| `img/login.png`, `img/firma-electronica.png`, `img/bandeja-notario.png`, `img/evaluacion-tramite.png`, `img/eliminar-tramite.png`, `img/notificaciones.png`, `img/perfil.png`, `img/feriados-API.png` | Flujo principal de la app y API externa |
| `img/ava-grupal.png` | Confirmación de entrega grupal AVA |
| `img/ava-individual.png` | Confirmación de activación individual de todos los integrantes |

## APK y keystore

- APK local de entrega: `~/Notix-1.0-release.apk`
- Ruta generada: `Notix/app/build/outputs/apk/release/app-release.apk`
- Keystore local: `Notix/notix-release.jks`
- Ambos están ignorados por Git.
- Entregar el APK por AVA, Drive o GitHub Releases.
- Entregar el keystore solo si el docente lo solicita, por un canal privado o AVA. Nunca en un enlace público.

Comandos para generar la evidencia del APK:

```bash
cd Notix

$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
  "$HOME/Notix-1.0-release.apk"

sha256sum "$HOME/Notix-1.0-release.apk"

$ANDROID_HOME/build-tools/36.0.0/aapt dump permissions \
  "$HOME/Notix-1.0-release.apk" | grep POST_NOTIFICATIONS
```

## Trello

El tablero debe tener, como mínimo:

- **Por hacer**
- **En progreso**
- **En revisión**
- **Terminado**

Tareas sugeridas:

1. Flujos Persona/Empresa.
2. Firma simulada.
3. Bandeja y revisión del notario.
4. Corregir y reenviar.
5. Perfil y logout.
6. Microservicios Spring Boot.
7. Integración Retrofit.
8. API externa Nager.Date.
9. DELETE desde la UI.
10. Notificaciones locales.
11. Pruebas y cobertura.
12. APK firmado.
13. Documentación y README.
14. Evidencias y entrega AVA.

## GitHub

Comprobar:

```bash
git status
git log --oneline -10
git log --format='%h %an <%ae>' -10
git push origin main
```

La entrega requiere commits de todos los integrantes y acceso del docente al repositorio.

## AVA

Antes de cerrar:

- Subir código de app + backend.
- Adjuntar APK firmado.
- Adjuntar `.jks` solo si se solicita.
- Adjuntar captura de Trello y cobertura.
- Activar entrega individual de todos los integrantes.
- No hacer commits después de la fecha límite.
