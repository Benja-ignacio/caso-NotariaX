# Notix — MVP de trámites notariales digitales

Aplicación Android académica para el caso **NotariaX** (DSY1105, Duoc UC). Permite registrar usuarios, crear trámites notariales simulados, adjuntar un documento, enviarlos al notario, revisarlos y consultar su estado con historial y avisos. Incluye además dos microservicios Spring Boot con API REST propia, todavía no conectados a la app.

> ⚠️ Proyecto académico con **datos ficticios**. No realiza trámites notariales reales, no usa firmas reales, biometría real ni servicios de NotariaX.

## Documentación

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
| Microservicios Spring Boot `ms-usuarios` y `ms-tramites` | ✅ Implementados y probados por separado |
| Integración app Android ↔ backend (Retrofit) | ❌ Pendiente (Fase 3) |
| API externa visible en la app | ❌ Pendiente (Fase 3) |
| Notificaciones del sistema | ❌ Pendiente |
| Pruebas unitarias/UI de la app | ⚠️ Solo de ejemplo |

## Persistencia

La app usa **Room** (`notix_database`) como fuente de verdad. Se guardan:

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

La app Android **aún no consume** estos servicios: Room sigue siendo su fuente de verdad hasta la Fase 3 (Retrofit). Contratos, endpoints y reglas de estado están en [`backend/README.md`](backend/README.md).

## Cómo ejecutar la app

Requisitos: Android Studio, JDK 21, SDK Android 37 y un emulador/dispositivo API 24+.

```bash
cd Notix
chmod +x gradlew        # si hace falta
./gradlew assembleDebug
```

O abrir la carpeta `Notix/` en Android Studio y ejecutar `app`.

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
        ├── repository/     # Acceso a datos
        ├── viewmodel/      # Estado de pantalla
        ├── ui/             # Compose
        └── model/          # Dominio
```

## Tecnologías

App: Kotlin · Jetpack Compose · Material 3 · Room · KSP · ViewModel · StateFlow · Coroutines.

Backend: Java 21 · Spring Boot 4.2 · Spring Web MVC · Spring Data JPA · H2 · Maven.

> Estado EP4: cerradas la Fase 1 (flujos de la app) y la Fase 2 (microservicios Spring Boot). Siguen pendientes la integración con Retrofit, la API externa, las pruebas de la app con cobertura ≥ 80 %, el APK firmado y las evidencias de entrega.
