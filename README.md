# Notix — MVP de trámites notariales digitales

Aplicación Android académica para el caso **NotariaX** (DSY1105, Duoc UC). Permite registrar usuarios, crear trámites notariales simulados, adjuntar un documento, enviarlos al notario, revisarlos y consultar su estado con historial y avisos. Incluye además dos microservicios Spring Boot con API REST propia, conectados a la app mediante Retrofit.

> ⚠️ Proyecto académico con **datos ficticios**. No realiza trámites notariales reales, no usa firmas reales, biometría real ni servicios de NotariaX.

## Documentación

- **[Guía de ejecución y pruebas](docs/Guia_ejecucion_y_pruebas.md)** → cómo levantar el backend, conectar emulador/teléfono y probar el flujo completo.
- **[Documentación técnica y estado del proyecto](docs/Documentacion_Notix.md)** → arquitectura, persistencia Room, funcionalidades listas, pendientes y checklist de pruebas.
- **[Carpeta de implementación EP4](docs/implementacion/estado_actual.md)** → estado actual para continuar el trabajo, plan, guía técnica y pauta del encargo.
- [Contexto del caso](docs/contexto.md) → alcance y requisitos originales.

## Estado rápido

| Área | Estado |
|---|---|
| Login / registro Persona, Empresa y Notario | ✅ |
| Catálogo y creación de trámites | ✅ |
| Persistencia con Room | ✅ |
| Bandeja del notario, aprobar y devolver | ✅ |
| Historial y avisos en base de datos | ✅ |
| Firma simulada obligatoria (trazo/e-token/notarial) | ✅ |
| Corregir y reenviar tras devolución | ✅ |
| Confirmación de legibilidad al aprobar | ✅ |
| Logout Persona / Empresa / Notario | ✅ |
| Perfil de usuario y navegación inferior | ✅ Perfil, Trámites y Avisos conectados |
| Microservicios Spring Boot `ms-usuarios` y `ms-tramites` | ✅ Implementados y probados |
| Integración app Android ↔ backend (Retrofit) | ✅ Login, registro, trámites y avisos contra la API |
| API externa visible en la app | ✅ Próximo feriado de Nager.Date con fallback |
| Notificaciones del sistema | ❌ Pendiente |
| Pruebas unitarias de la app | ✅ 82 pruebas JVM; cobertura de línea 96,26 % |
| Pruebas DAO con Room | ✅ 7 pruebas instrumentadas en dispositivo |
| DELETE/cancelar desde la app | ✅ Con confirmación y sincronización con backend |

## Persistencia

La app usa **Room** (`notix_database`) como caché local; el backend es la fuente de verdad para las escrituras. En Room se guardan:

- Usuarios
- Trámites
- Documentos (nombre, tamaño y estado; **no el archivo**)
- Historial de cambios de estado
- Avisos y su estado de lectura

Los datos **sobreviven al cierre de la app**. La sesión iniciada **no** se conserva. La base está en versión 3 y la migración 2 → 3 (`tramites.firmaEvidencia`) conserva los datos; `fallbackToDestructiveMigration()` queda solo como respaldo si falta una ruta de migración.

Todos los detalles están en [docs/Documentacion_Notix.md](docs/Documentacion_Notix.md#6-persistencia-de-datos-room).

## Backend EP4 (Fase 2)

El directorio [`backend/`](backend/README.md) contiene dos microservicios Spring Boot 4.2 con Java 21, Maven Wrapper, JPA e H2 persistente:

| Servicio | Puerto | Contenido |
|---|---|---|
| `ms-usuarios` | `8081` | Registro, login y CRUD de usuarios |
| `ms-tramites` | `8082` | Trámites, documentos, historial y avisos |

```bash
cd backend
./mvnw test
./mvnw spring-boot:run -pl ms-usuarios    # terminal 1
./mvnw spring-boot:run -pl ms-tramites    # terminal 2
```

La app consume estos servicios con **Retrofit** y usa **Room como caché local**:

- Login y registro se validan en `ms-usuarios`; el usuario recibido se guarda en Room.
- Los trámites se crean, aprueban, devuelven y reenvían contra `ms-tramites`.
- Al entrar a Mis Trámites, Bandeja o Avisos se refresca la copia local desde el backend.
- Si el backend no responde, se muestra la última copia de Room y las operaciones de escritura informan el error.
- El login permite un fallback local si el backend está caído y el usuario ya existe en Room.
- La tarjeta de feriados consulta el próximo feriado de Chile en **Nager.Date**; si falla, muestra un texto de respaldo sin bloquear el flujo.

`ApiConfig.kt` elige la URL automáticamente: `10.0.2.2` en emulador y `127.0.0.1` en dispositivo físico. Para un teléfono conectado por USB, ejecutar:

```bash
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8082 tcp:8082
```

También se puede reemplazar la URL por la IP LAN del PC si se prefiere Wi-Fi.

Contratos, endpoints y reglas de estado están en [`backend/README.md`](backend/README.md).

## Cómo ejecutar la app

Requisitos: Android Studio, JDK 21, SDK Android 37 y un emulador/dispositivo API 24+.

Para el flujo completo, primero levantar los dos microservicios (ver sección Backend) y luego la app:

```bash
cd Notix
chmod +x gradlew        # si hace falta
./gradlew assembleDebug
```

Sin backend la app puede abrirse con la caché local, pero registrar usuarios, crear trámites o cambiar estados mostrará un error de conexión.

O abrir la carpeta `Notix/` en Android Studio y ejecutar `app`.

## Pruebas y cobertura

```bash
cd Notix
./gradlew :app:testDebugUnitTest
./gradlew :app:jacocoTestReport
./gradlew :app:jacocoTestCoverageVerification
```

Resultado actual: **82 pruebas unitarias**, sin fallos, y **96,26 % de cobertura de línea** en `viewmodel`, `repository`, `data/repository`, `data/validation`, `data/remote` y `util`.

Pruebas DAO con Room en dispositivo/emulador:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Resultado actual: **7 pruebas DAO**, sin fallos. Verifican inserciones, transacciones, reemplazo de documentos/historial, borrado en cascada y avisos.

Reporte HTML:

```text
Notix/app/build/reports/jacoco/jacocoTestReport/html/index.html
```

El umbral de 80 % está configurado en Gradle mediante `jacocoTestCoverageVerification`.

## Cuentas demo

| Perfil | Correo | Contraseña |
|---|---|---|
| Persona | `ana.demo@notariax.test` | `demo123` |
| Notario | `notario.demo@notariax.test` | `notario123` |

También se puede crear una cuenta desde la pantalla de registro.

## Estructura

```text
caso-NotariaX/
├── backend/       # Microservicios Spring Boot (Fase 2 EP4)
│   ├── ms-usuarios/
│   └── ms-tramites/
├── docs/          # Contexto, guía y documentación técnica
├── index.html     # Prototipo web legado (referencia)
├── views/         # Pantallas HTML del prototipo legado
└── Notix/         # Proyecto Android (entrega principal)
    └── app/src/main/java/com/duoc/notix/
        ├── data/local/     # Room
        ├── data/remote/    # Retrofit, DTOs y clientes de API
        ├── repository/     # Acceso a datos y sincronización
        ├── viewmodel/      # Estado de pantalla
        ├── ui/             # Compose
        └── model/          # Dominio
```

## Tecnologías

App: Kotlin · Jetpack Compose · Material 3 · Room · Retrofit · OkHttp · Gson · KSP · ViewModel · StateFlow · Coroutines.

Backend: Java 21 · Spring Boot 4.2 · Spring Web MVC · Spring Data JPA · H2 · Maven.

> Estado EP4: cerradas las Fases 1 (flujos), 2 (microservicios), 3 (Retrofit + API externa + DELETE), 4 (pruebas de cobertura ≥ 80 %) y la generación del APK firmado. Siguen pendientes las evidencias de entrega y las pruebas de UI.
