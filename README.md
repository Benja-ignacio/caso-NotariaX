# Notix — Caso NotariaX

> **1. Nombre del proyecto:** Notix — Caso NotariaX

Aplicación Android académica para el caso **NotariaX** (DSY1105, Duoc UC). Permite registrar usuarios, crear trámites notariales simulados, adjuntar un documento, enviarlos al notario, revisarlos y consultar su estado con historial y avisos. Incluye dos microservicios Spring Boot conectados mediante Retrofit.

> Proyecto académico con **datos ficticios**. No realiza trámites notariales reales, no usa firmas reales, biometría real ni servicios de NotariaX.

---

## 2. Integrantes

| Nombre | GitHub |
|---|---|
| Benjamin | [@Benja-ignacio](https://github.com/Benja-ignacio) |
| Juan Alonso Parra Verdejo | [@juanalonsoparraverdejo](https://github.com/juanalonsoparraverdejo) |

Repositorio: [github.com/Benja-ignacio/caso-NotariaX](https://github.com/Benja-ignacio/caso-NotariaX)

> Completar el número de equipo en la entrega AVA si corresponde.

---

## 3. Funcionalidades

| Área | Estado |
|---|---|
| Login / registro Persona, Empresa y Notario | ✅ |
| Catálogo y creación de trámites | ✅ |
| Persistencia Room + sincronización con backend | ✅ |
| Bandeja del notario, aprobar y devolver | ✅ |
| Historial y avisos | ✅ |
| Firma simulada obligatoria | ✅ |
| Corregir y reenviar tras devolución | ✅ |
| Confirmación de legibilidad al aprobar | ✅ |
| Logout y perfil de usuario | ✅ |
| Eliminar trámites no finalizados | ✅ |
| Notificaciones locales de avisos | ✅ Android 13+ |
| API externa de feriados | ✅ Nager.Date con fallback |
| Pruebas unitarias y DAO | ✅ 82 JVM + 7 DAO |
| Cobertura de línea | ✅ 96,30 % |
| Pruebas de UI | ⚠️ Pendiente |
| Notificaciones con app cerrada | ⚠️ Pendiente (WorkManager/FCM) |

### Flujo principal

1. Persona crea un trámite y firma de forma simulada.
2. El trámite se envía al backend.
3. El notario lo revisa en su bandeja.
4. Puede aprobarlo confirmando legibilidad o devolverlo con observación.
5. La persona ve el aviso, corrige, reenvía o elimina el trámite.
6. Room mantiene una caché local; el backend es la fuente de verdad para escrituras.

---

## 4. Endpoints utilizados

### API externa

| API | Endpoint | Uso |
|---|---|---|
| Nager.Date | `GET https://date.nager.at/api/v3/NextPublicHolidays/CL` | Próximo feriado de Chile en el catálogo, con fallback si falla |

### Microservicios Spring Boot

**`ms-usuarios` — puerto 8081**

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/usuarios/registro` | Registrar usuario |
| `POST` | `/api/usuarios/login` | Login |
| `GET` | `/api/usuarios/{id}` | Obtener usuario |
| `GET` | `/api/usuarios` | Listar usuarios |
| `PUT` | `/api/usuarios/{id}` | Actualizar usuario |
| `DELETE` | `/api/usuarios/{id}` | Eliminar usuario |

**`ms-tramites` — puerto 8082**

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/tramites` | Todos los trámites |
| `GET` | `/api/tramites/solicitante/{id}` | Trámites de un solicitante |
| `GET` | `/api/tramites/{id}` | Detalle |
| `POST` | `/api/tramites` | Crear trámite |
| `PUT` | `/api/tramites/{id}/estado` | Aprobar o devolver |
| `PUT` | `/api/tramites/{id}/reenvio` | Corregir y reenviar |
| `DELETE` | `/api/tramites/{id}` | Eliminar trámite no finalizado |
| `GET` | `/api/avisos/{usuarioId}` | Avisos del usuario |
| `PUT` | `/api/avisos/{id}/leido` | Marcar aviso como leído |

Contratos completos: [`backend/README.md`](backend/README.md).

---

## 5. Pasos para ejecutar

### Requisitos

- JDK 21.
- Android Studio y SDK Android 37.
- Emulador o dispositivo físico API 24+.
- Puertos `8081` y `8082` libres.

### Backend

```bash
cd backend
./mvnw -DskipTests package
```

Terminal 1:

```bash
cd backend/ms-usuarios
java -jar target/ms-usuarios-0.0.1-SNAPSHOT.jar
```

Terminal 2:

```bash
cd backend/ms-tramites
java -jar target/ms-tramites-0.0.1-SNAPSHOT.jar
```

Verificar:

```bash
curl http://localhost:8081/api/usuarios
curl http://localhost:8082/api/tramites
```

### App

```bash
cd Notix
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O abrir `Notix/` en Android Studio y ejecutar `app`.

### Emulador

La app usa automáticamente `10.0.2.2:8081` y `10.0.2.2:8082`. No requiere `adb reverse`.

### Dispositivo físico por USB

```bash
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8082 tcp:8082
```

La app usa `127.0.0.1`. Si se reconecta el teléfono, repetir los comandos.

### Cuentas demo

| Perfil | Correo | Clave |
|---|---|---|
| Persona | `ana.demo@notariax.test` | `demo123` |
| Notario | `notario.demo@notariax.test` | `notario123` |

Guía completa: [`docs/Guia_ejecucion_y_pruebas.md`](docs/Guia_ejecucion_y_pruebas.md).

Comandos rápidos: [`docs/Comandos_utiles.md`](docs/Comandos_utiles.md).

---

## 6. Captura del APK firmado y `.jks`

Estado actual del APK release:

| Dato | Valor |
|---|---|
| Archivo local | `Notix/app/build/outputs/apk/release/app-release.apk` |
| Versión | `1.0` (`versionCode 1`) |
| Certificado | `CN=Notix Team, OU=Notix, O=Notix, L=Chile, ST=Santiago, C=CL` |
| SHA-256 del APK | `ee3cfb7595ca42fb3fb854210a77b56d19076b5650b2e04bfbc654b4682a318d` |
| Permiso de notificaciones | ✅ `POST_NOTIFICATIONS` incluido |
| Keystore local | `Notix/notix-release.jks` (ignorado por Git) |

Verificación:

```bash
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
  Notix/app/build/outputs/apk/release/app-release.apk
```

> El APK y el `.jks` **no se versionan** en el repositorio. Se entregan en AVA o por un enlace privado. No publicar el keystore ni sus contraseñas.
>
> Captura pendiente: `docs/evidencias/apk-firmado.png`.

---

## Documentación

- [Comandos útiles](docs/Comandos_utiles.md).
- [Guía de ejecución y pruebas](docs/Guia_ejecucion_y_pruebas.md).
- [Documentación técnica](docs/Documentacion_Notix.md).
- [Estado actual EP4](docs/implementacion/estado_actual.md).
- [Evidencias de entrega](docs/evidencias/README.md).
- [Contexto del caso](docs/contexto.md).

## Persistencia

Room (`notix_database`) actúa como caché local. El backend es la fuente de verdad para las escrituras.

Se guardan usuarios, trámites, documentos (solo metadatos), historial y avisos. Los datos locales sobreviven al cierre de la app; la sesión no se persiste.

La base está en versión 3, con migración `2 → 3` para `tramites.firmaEvidencia`.

## Pruebas y cobertura

```bash
cd Notix
./gradlew :app:testDebugUnitTest
./gradlew :app:jacocoTestReport
./gradlew :app:jacocoTestCoverageVerification
./gradlew :app:connectedDebugAndroidTest
```

- 82 pruebas JVM.
- 7 pruebas DAO instrumentadas.
- Cobertura de línea: 96,30 %.
- Cobertura de ramas: 74,93 %.
- Umbral JaCoCo: 80 % de línea.
- Reporte: `Notix/app/build/reports/jacoco/jacocoTestReport/html/index.html`.

## Estructura

```text
caso-NotariaX/
├── backend/       # Microservicios Spring Boot
│   ├── ms-usuarios/
│   └── ms-tramites/
├── docs/
├── Notix/         # App Android
│   └── app/src/main/java/com/duoc/notix/
│       ├── data/local/     # Room
│       ├── data/remote/    # Retrofit y DTOs
│       ├── repository/
│       ├── viewmodel/
│       ├── ui/
│       └── model/
└── README.md
```

## Tecnologías

App: Kotlin · Jetpack Compose · Material 3 · Room · Retrofit · OkHttp · Gson · KSP · ViewModel · StateFlow · Coroutines.

Backend: Java 21 · Spring Boot 4.2 · Spring Web MVC · Spring Data JPA · H2 · Maven.
