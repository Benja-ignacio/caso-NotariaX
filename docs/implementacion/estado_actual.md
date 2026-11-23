# Estado actual de Notix y guía para continuar la EP4

> **Propósito:** dar a una IA o integrante una descripción fiel del repositorio antes de implementar el plan EP4. Este archivo documenta el código inspeccionado el **23-11-2026**; no reemplaza la pauta ni certifica que el proyecto compile o que los servicios externos funcionen.
>
> **Orden de referencia:** la pauta PDF define qué se evalúa; `plan_de_desarrollo_ep4_notix.md` organiza los requisitos; `implementacion_ep4_notix.md` propone un diseño de solución; este documento aclara qué existe realmente en el código fuente. Si hay contradicciones, no asumir que una recomendación ya fue implementada: inspeccionar el código y actualizar esta guía tras cada hito.

---

## 1. Resumen ejecutivo

Notix es una app Android académica para representar trámites notariales con información ficticia. La entrega implementada en el repositorio es una app Kotlin/Jetpack Compose con arquitectura **Compose → ViewModel → Repository → Room + Retrofit**. Tiene login/registro, selección de formatos, creación y consulta de trámites, firma simulada obligatoria, bandeja notarial, devolución/aprobación con legibilidad confirmada, corrección/reenvío, historial, avisos y logout para los tres perfiles. Además, `backend/` contiene dos microservicios Spring Boot (`ms-usuarios` y `ms-tramites`) con H2 persistente, API REST y seeders demo, consumidos por la app con Retrofit.

En el árbol de fuentes inspeccionado existen Retrofit, API externa (Nager.Date), microservicios, pruebas de backend, 82 pruebas unitarias JVM con cobertura de línea de 96,30 % y 7 pruebas DAO instrumentadas. **No** existen pruebas de UI, keystore versionado ni APK dentro del repositorio; la configuración de firma usa variables de entorno. Room actúa como caché local de un backend que ya es la fuente de verdad para las escrituras.

La app no procesa trámites reales. Mantener datos de demostración y no incorporar credenciales, personas, firmas, documentos ni sistemas productivos.

> **Fase 1 de la EP4 cerrada el 23-11-2026:** firma simulada, corregir/reenviar, legibilidad obligatoria, bloqueo de estados del notario y logout Persona/Empresa. Verificado con compilación y `assembleDebug`; no se ejecutó la app en emulador ni se midió cobertura.
>
> **Fase 2 de la EP4 cerrada el 23-11-2026:** `ms-usuarios` (8081) y `ms-tramites` (8082) con Spring Boot 4.2, Java 21, Maven, JPA, H2 en archivo, seeders y endpoints REST. Verificado con `./mvnw test` (11 pruebas) y llamadas HTTP reales a ambos servicios.
>
> **Fase 3 de la EP4 cerrada el 23-11-2026:** Retrofit contra ambos microservicios, sincronización Room ↔ backend, fallback de login sin red y tarjeta del próximo feriado de Nager.Date con fallback. Verificado con compilación, `assembleDebug`, una prueba JVM de contrato y un flujo end-to-end en **dispositivo físico** con `adb reverse`: login, creación de `TX-2026-0043`, sincronización en Mis Trámites y aprobación desde la Bandeja del Notario; el backend confirmó `FINALIZADO`. También se verificó el login local y la caché sin backend, y la tarjeta mostró “Inmaculada Concepción · 08/12/2026”. El emulador local falló con `SIGSEGV`, pero el dispositivo físico sí completó el flujo.
>
> **Fase 4 de la EP4 cerrada el 23-11-2026:** 82 pruebas JVM con JUnit, MockK y `kotlinx-coroutines-test`; cobertura de línea 96,30 % y de ramas 74,93 %. JaCoCo genera reporte y `jacocoTestCoverageVerification` exige 80 % de línea. Se agregaron 7 pruebas DAO instrumentadas con Room en memoria. Se refactorizaron `LoginViewModel` y `AuthViewModel` para inyectar `AuthRepository`.
>
> **Notificaciones locales implementadas el 23-11-2026:** canal, permiso `POST_NOTIFICATIONS`, avisos nuevos al sincronizar, `PendingIntent` y deduplicación en SharedPreferences. Verificado en dispositivo físico con una devolución generada desde el backend.
>
> **DELETE desde la app implementado el 23-11-2026:** botón con confirmación para trámites no finalizados, llamada a `DELETE /api/tramites/{id}` y borrado local en cascada de documentos, historial y avisos. Verificado en dispositivo físico: `TX-2026-0042` desapareció del backend y de la UI.

## 2. Estructura real

```text
caso-NotariaX/
├── backend/                             # microservicios Spring Boot (Fase 2)
│   ├── pom.xml
│   ├── mvnw
│   ├── ms-usuarios/                     # puerto 8081
│   └── ms-tramites/                     # puerto 8082
├── docs/
│   ├── contexto.md
│   ├── Documentacion_Notix.md
│   └── implementacion/
│       ├── plan_de_desarrollo_ep4_notix.md
│       ├── implementacion_ep4_notix.md
│       ├── estado_actual.md             # este documento
│       ├── Guia_implementacion_Notix.md
│       └── Ev. Parcial 4_Encargo_Estudiante_ DSY1105.pdf
├── Notix/                               # proyecto de app Android
│   ├── settings.gradle.kts              # solo incluye :app
│   ├── app/build.gradle.kts
│   ├── gradle/libs.versions.toml
│   └── app/src/main/java/com/duoc/notix/
│       ├── MainActivity.kt
│       ├── model/                       # ruta física de modelos
│       ├── data/local/                  # Room, DAOs, seeder, mappers
│       ├── data/remote/                 # Retrofit, DTOs, APIs y mapeos
│       ├── data/validation/
│       ├── repository/
│       ├── viewmodel/
│       ├── ui/screens/
│       ├── ui/components/
│       └── util/
├── index.html, css/, views/             # prototipo web legado, no app Android
└── README.md
```

El backend existe en `backend/` como proyecto Maven independiente con dos módulos (`ms-usuarios` y `ms-tramites`). El `settings.gradle.kts` de la app solo incluye `:app`, por lo que el backend no se construye con Gradle ni desde Android Studio. No se encontraron archivos fuente en `data/remote`, configuración Retrofit, `.jks`, APK de entrega ni evidencia versionada de Trello.

## 3. Tecnologías y configuración observadas

| Elemento | Valor del código |
|---|---|
| App | Kotlin, Jetpack Compose y Material 3 |
| Namespace / applicationId | `com.duoc.notix` |
| SDK | min 24, target 36, compile SDK 37 (minor API 1) |
| AGP / Kotlin | AGP `9.2.1`, Kotlin `2.2.10` |
| Persistencia | Room `2.8.5`, KSP |
| Java bytecode | `VERSION_11` en `app/build.gradle.kts` |
| Navegación | Enum `Pantalla` y `when` en `MainActivity`; no Navigation Compose |
| Dependencias remotas | Retrofit `2.11.0`, Gson converter, OkHttp logging `4.12.0` |
| Pruebas JVM | JUnit 4, MockK `1.13.12`, coroutines-test `1.8.1` y JaCoCo; 82 pruebas |
| Pruebas DAO | Room en memoria; 7 pruebas instrumentadas |
| Manifest de red | `INTERNET` y `usesCleartextTraffic="true"` para backend local |
| Firma release | `signingConfigs` configurado con keystore externo; no versionar `.jks` ni contraseñas |
| Backend | Java 21, Spring Boot `4.2.0`, Maven Wrapper `3.3.4` con Maven `3.10.1` |
| Backend persistencia | Spring Data JPA / Hibernate `7.4.15`, H2 `2.5.252` en archivo |
| Backend puertos | `ms-usuarios` 8081 y `ms-tramites` 8082 |
| Integración app ↔ backend | Implementada con Retrofit; permiso `INTERNET` y tráfico local habilitados |

**Precaución Gradle:** el catálogo observado declara Kotlin `2.2.10` y KSP `2.1.20-1.0.31`. Antes de modificar estas versiones, comprobar compatibilidad con la configuración y el wrapper existentes. No actualizar versiones en bloque ni reemplazar el catálogo a partir de ejemplos.

## 4. Paquetes y convenciones que no se deben confundir

- Los archivos físicos bajo `model/` declaran `package com.duoc.notix.data.model`. Importar modelos con `com.duoc.notix.data.model.*`; la ruta física no refleja el package.
- `AuthRepository` está físicamente en `repository/AuthRepository.kt`, pero declara `com.duoc.notix.data.repository`.
- `TramiteRepository` está en `repository/TramiteRepository.kt` y declara `com.duoc.notix.repository`.
- Algunos nombres de archivo conservan capitalización irregular: `AuthViewmodel.kt`, `TramitesViewmodel.kt`; las clases son `AuthViewModel` y `MisTramitesViewModel`.
- No mover paquetes ni renombrar clases como parte incidental de la integración. Si se decide normalizar, hacerlo como cambio acotado, actualizar todos los imports y revisar compilación antes de continuar.

## 5. Flujos y datos que sí existen

### 5.1 Navegación y sesión

`MainActivity` controla `Welcome`, `Login`, `CrearCuenta`, `MisTramites`, `CatalogoFormatos`, `CrearTramite`, `BandejaNotario`, `Avisos` y `Perfil` mediante `enum Pantalla`.

- Login y registro derivan al notario a la bandeja y a persona/empresa a Mis Trámites.
- `usuarioId` se conserva con `rememberSaveable`, pero parte con el valor por defecto `USR-001`; la sesión no se restaura de Room ni de preferencias al reiniciar.
- El logout de Notario, Persona y Empresa vuelve a `Welcome` con confirmación y limpia el usuario en memoria. No hay `SessionManager` persistente.
- La barra inferior de Persona/Empresa tiene tres tabs conectados: `Trámites`, `Avisos` y `Perfil`. Se eliminó el `Inicio` vacío.
- `PerfilScreen` muestra nombre, correo, tipo de cuenta, RUN, documento, teléfono, comuna y datos de empresa/notaría; permite cerrar sesión.
- `onVerTramite` en `MainActivity` está vacío; el detalle lo maneja actualmente un diálogo dentro de `MisTramitesScreen`.
- No se observó un `BackHandler` para coordinar el botón atrás.

### 5.2 Autenticación local

`AuthRepository` usa `UsuariosApi` para login/registro y guarda el usuario en `UsuarioDao`. Si falla la red, el login puede validar contra la copia local. Normaliza el correo y el backend valida tipo de cuenta, clave y duplicados. `LoginViewModel` y `AuthViewModel` extienden `AndroidViewModel` y crean `NotixDatabase`/repositorio internamente; no reciben dependencias por constructor. Esto dificulta pruebas unitarias JVM aisladas y debe considerarse al diseñar los tests.

Las contraseñas se guardan en texto plano en Room para la demo. No reutilizar esa implementación como autenticación de producción. Los usuarios ficticios se siembran desde `DatabaseSeeder` al iniciar `MainActivity`.

### 5.3 Trámites

- El catálogo actual tiene tres formatos: `F01` Poder simple (`SIMPLE`), `F02` Declaración jurada simple (`AVANZADA`) y `F03` Autorización de viaje (`NOTARIADA`).
- `CrearTramiteViewModel` completa antecedentes desde el usuario, valida RUN usando `RutValidator`, exige nombre, correo de forma superficial (`@`) y comuna.
- El selector de archivos usa `ActivityResultContracts.GetContent`; la app conserva el nombre del archivo como metadato, no el contenido ni una URI persistente. Si no se selecciona uno, el ViewModel inventa un nombre PDF de demostración. Por tanto, la app actualmente no exige seleccionar un documento real.
- El ID se genera localmente con `TX-2026-${(1000..9999).random()}`; puede colisionar y no proviene de un backend.
- Al crear se exige completar el paso de firma simulada según `TipoFirma`: trazo en `Canvas` (`SIMPLE_CANVAS`), código de 6 dígitos (`AVANZADA_TOKEN_SIMULADO`) o autorización notarial (`NOTARIADA_SIMULADA`). La evidencia se guarda en `Tramite.firmaEvidencia`.
- Al crear se inserta el trámite, documentos e historial en Room y se agrega aviso local. No hay transmisión remota.
- Mis Trámites y Bandeja del Notario observan `Flow` de Room y presentan filtros/contadores. Ordenan por ID descendente, no por fecha.
- El notario solo puede actuar sobre trámites `ENVIADO`. Aprobar exige marcar el checkbox de legibilidad y persiste `legibilidadConfirmada = true`; devolver exige observación no vacía. El repositorio también rechaza transiciones inválidas.
- Un trámite `DEVUELTO` puede corregirse y reenviarse desde el detalle de la persona: se editan antecedentes, se exige reemplazar documentos `OBSERVADO`, se limpia la observación, vuelve a `ENVIADO` y se registran historial, aviso y `legibilidadConfirmada = false`.
- Las operaciones de cambio de estado y reenvío usan transacciones de Room para trámite, documentos, historial y aviso.
- No hay acción para cancelar/eliminar un trámite desde la UI.

### 5.4 Base de datos Room

`NotixDatabase` registra cinco entidades: `UsuarioEntity`, `TramiteEntity`, `DocumentoEntity`, `EventoHistorialEntity` y `AvisoEntity`. Está en versión **3**, con `exportSchema = false`, migración explícita `MIGRATION_2_3` (agrega `tramites.firmaEvidencia`) y `fallbackToDestructiveMigration()` como respaldo.

- Hay relación de documentos e historial con trámite y borrado en cascada. Los avisos no tienen FK.
- Los archivos adjuntos no se almacenan. Los documentos guardan nombre, tamaño y estado.
- Room actúa como caché local: las pantallas observan sus `Flow`, pero las escrituras se envían primero al backend y luego se guarda la respuesta.
- Cambiar entidades requiere incrementar versión y definir la migración. La 2 → 3 conserva datos; el fallback actual puede borrar la base local si falta una ruta.
- `DatabaseSeeder` asegura usuarios demo en cada inicio y agrega trámites solamente si no encuentra ninguno. La sincronización posterior reemplaza documentos/historial con los datos del backend.
- La sincronización no elimina de Room los trámites que el backend ya no devuelve; el DELETE desde esta app sí borra la copia local, pero un borrado hecho desde otro cliente no se refleja automáticamente.

### 5.5 Backend Spring Boot (Fase 2)

`backend/` es un proyecto Maven padre con dos microservicios independientes:

| Servicio | Puerto | Datos | Endpoints principales |
|---|---|---|---|
| `ms-usuarios` | 8081 | `usuarios` en H2 `./data/msusuarios` | registro, login, listar/obtener, actualizar y eliminar |
| `ms-tramites` | 8082 | `tramites`, `documentos`, `historial_eventos`, `avisos` en H2 `./data/mstramites` | listar, filtrar por solicitante, detalle, crear, cambiar estado, reenviar, eliminar y avisos |

- Usa Spring Boot 4.2, Spring Web MVC, Spring Data JPA, Hibernate 7, H2 y Jakarta Validation.
- `POST /api/usuarios/registro` normaliza el correo, valida RUN/documento y exige datos adicionales para Empresa y Notario.
- El login diferencia `404` (no existe), `403` (tipo de cuenta incorrecto) y `401` (clave incorrecta).
- `ms-tramites` aplica la misma máquina de estados que la app: aprobar exige `legibilidadConfirmada=true`; devolver exige observación; reenviar solo desde `DEVUELTO`; `FINALIZADO` no admite cambios ni eliminación.
- El backend genera historial y avisos; el cliente solo envía la acción y los datos editables.
- Los seeders crean `USR-001`, `USR-NOTARIO-01` y cuatro trámites demo (`TX-2026-0001`, `0031`, `0039`, `0042`) con avisos.
- Los errores usan `{timestamp, status, error, message}`.
- Las pruebas (`./mvnw test`) cubren contexto y flujos HTTP con MockMvc: 11 pruebas en total.
- **La app ya consume estos servicios** mediante Retrofit; ver 5.6.

### 5.6 Integración Retrofit y API externa (Fase 3)

- `data/remote/ApiConfig.kt` elige `10.0.2.2` en emulador y `127.0.0.1` en dispositivo físico; este último requiere `adb reverse tcp:8081 tcp:8081` y `adb reverse tcp:8082 tcp:8082`. Nager.Date usa `https://date.nager.at/`.
- `RetrofitProviders.kt` crea clientes separados con OkHttp, Gson y logging básico.
- `AuthRepository` usa `ms-usuarios`; guarda el usuario en Room y permite login local si falla la red.
- `TramiteRepository` usa `ms-tramites` para crear, aprobar, devolver, reenviar y leer/marcar avisos. Después de cada escritura guarda la respuesta en Room.
- `MisTramitesViewModel`, `BandejaNotarioViewModel` y `AvisosViewModel` refrescan desde el backend al iniciar. La UI sigue observando Room.
- `FeriadosRepository` consulta Nager.Date con `runCatching`; si falla, `CatalogoFormatosScreen` muestra “Feriados no disponibles” y el flujo continúa.
- Los errores HTTP leen `message` del JSON del backend; los errores de red muestran falta de conexión.
- `AndroidManifest.xml` declara `INTERNET` y `usesCleartextTraffic="true"` para HTTP local. Es una decisión académica, no de producción.
- Los DTOs remotos y mapeos están en `data/remote/dto/` y `data/remote/RemoteMappers.kt`.

## 6. Hallazgos críticos para no seguir afirmaciones incorrectas del plan

La guía EP4 contiene propuestas, pero algunos diagnósticos deben verificarse contra el fuente y no copiarse a ciegas:

1. **Legibilidad en creación (resuelto):** `CrearTramiteScreen` ahora inicializa `legibilidadConfirmada = false` y exige marcar la declaración de la persona que crea el trámite. Ese booleano es independiente de `Tramite.legibilidadConfirmada`, que sigue siendo la confirmación del notario al aprobar.
2. **Legibilidad en revisión (resuelto):** `RevisionTramiteDialog` incluye checkbox obligatorio, bloquea Aprobar sin marcarlo y persiste `legibilidadConfirmada = true`. `BandejaNotarioViewModel` y `TramiteRepository` también validan estado y legibilidad.
3. **Autenticación y pruebas (resuelto):** `LoginViewModel` y `AuthViewModel` ahora reciben `AuthRepository` por constructor y exponen una factory para `Context`. Sus pruebas usan MockK sin crear Room.
4. **Dos paquetes repository:** no asumir que Auth y Tramite comparten el mismo package.
5. **“Firma” en el modelo (resuelto):** `Tramite` incorpora `firmaEvidencia` con valores ficticios por `TipoFirma`; no representa una firma legal ni certificados reales.
6. **CRUD (resuelto para trámites):** el backend y la app permiten eliminar trámites no finalizados. La UI pide confirmación, llama al endpoint y borra localmente en cascada. Un trámite `FINALIZADO` no se puede eliminar.
7. **Fechas e IDs:** el backend usa `fechaCreacion`/`fechaActualizacion` y genera IDs secuenciales `TX-2026-XXXX`; la app ya no genera IDs locales. La app aún no guarda `fechaCreacion` en el dominio y ordena los trámites por ID. No asumir orden cronológico en la UI.
8. **API externa (resuelta):** se eligió Nager.Date para el próximo feriado de Chile y se aisló en `FeriadosRepository` con fallback. No es una dependencia obligatoria para crear o enviar trámites.
9. **Room como caché (implementado parcialmente):** las lecturas se refrescan desde el backend y las escrituras remoto-primero actualizan Room. No hay resolución de conflictos ni borrado de trámites que desaparezcan del backend; documentar esa limitación si se prueba concurrencia.
10. **No declarar verificación no realizada:** este documento se basa en inspección estática, en la compilación/`assembleDebug` de la app, en 82 pruebas JVM con JaCoCo (96,30 % de línea), en `./mvnw test` del backend, en llamadas HTTP locales, en una prueba JVM de contrato y en el flujo end-to-end ejecutado en dispositivo físico. El emulador local falló con `SIGSEGV`, por lo que no se usó. No se afirma cobertura de pruebas de UI ni que la app esté lista para producción.

## 7. Objetivo EP4 frente al estado observado

| Área EP4 | Estado observado en fuentes |
|---|---|
| App Android y flujos existentes | Base implementada con Compose/Room; brechas de flujo arriba descritas |
| `ms-usuarios` y `ms-tramites` Spring Boot + BD | Implementados en `backend/` con H2 persistente y endpoints REST |
| Retrofit contra microservicios | Implementado para login, registro, trámites y avisos |
| API externa visible en UI | Implementada con Nager.Date (próximo feriado) y fallback |
| Firma simulada específica por `TipoFirma` | Implementada y obligatoria antes de enviar; evidencia persistida en Room |
| Corregir/re-enviar devueltos | Implementado, con reemplazo de documentos observados e historial |
| Confirmación de legibilidad al aprobar | Implementada en diálogo, ViewModel y repositorio |
| DELETE/cancelación desde UI | Implementado con confirmación y borrado local; backend y app coherentes |
| Tests de lógica y cobertura ≥80 % | 82 pruebas unitarias JVM, 96,30 % de línea, 7 pruebas DAO y umbral JaCoCo de 80 %. El backend tiene 11 pruebas propias |
| APK firmado y `.jks` | No encontrados; Gradle no tiene `signingConfigs` |
| README EP4, Trello y colaboración | El README existente no reemplaza la evidencia requerida; confirmar y completar puntos faltantes con datos reales del equipo |

La EP4 requiere app funcional integrada, microservicios con BD y endpoints, integración CRUD, API externa visible, pruebas con cobertura mínima del 80 %, APK firmado y evidencia GitHub/Trello. La app ya consume los microservicios con Retrofit, muestra el próximo feriado de Nager.Date, elimina trámites no finalizados, tiene notificaciones locales y 96,30 % de cobertura de línea; quedan notificaciones con app cerrada, pruebas de UI y evidencias de entrega. La pauta PDF es la fuente normativa.

## 8. Orden de trabajo sugerido y reglas de continuidad

Seguir el plan `implementacion_ep4_notix.md`, pero completar cada hito sin romper el flujo existente:

1. ~~Cerrar y validar los flujos pendientes de la app~~ **Hecho (Fase 1, 23-11-2026):** firma simulada, reenvío (`DEVUELTO → ENVIADO`), legibilidad obligatoria, bloqueo de estados y logout. Verificado con compilación y `assembleDebug`; falta prueba manual en emulador.
2. ~~Crear los microservicios con contratos acordados, persistencia y datos ficticios reproducibles.~~ **Hecho (Fase 2, 23-11-2026):** `ms-usuarios` y `ms-tramites` con Spring Boot 4.2, H2, seeders, endpoints y 11 pruebas. Verificado con `./mvnw test` y llamadas HTTP locales. No consumir todavía estos contratos desde la app sin actualizar DTO y documentación.
3. ~~Añadir Retrofit y capa remota, definir fuente de verdad y estrategia de error/refresco antes de reemplazar escrituras Room.~~ **Hecho (Fase 3, 23-11-2026):** backend remoto-primero, Room como caché y refresco al entrar a las pantallas. Falta resolver conflictos y eliminaciones remotas.
4. ~~Añadir API externa de forma aislada, con carga/error/fallback; nunca bloquear operaciones notariales simuladas.~~ **Hecho (Fase 3):** Nager.Date con el próximo feriado en el catálogo y texto de respaldo.
5. ~~Refactorizar dependencias donde se necesite para tests, luego implementar pruebas de reglas, transiciones y errores; medir cobertura real, no inferirla por cantidad de tests.~~ **Hecho (Fase 4, 23-11-2026):** 82 pruebas JVM, 96,30 % de línea, 74,93 % de ramas, 7 pruebas DAO y umbral JaCoCo de 80 %. Faltan pruebas de UI.
6. Configurar y generar APK firmado con secretos fuera del código y documentar requisitos de construcción. No registrar contraseñas de keystore en Git.
7. Ajustar README y documentación a lo efectivamente implementado y agregar evidencia colaborativa real; no inventar integrantes, commits, capturas ni tablero.

Reglas para cualquier IA que continúe:

- Antes de editar, volver a inspeccionar el archivo fuente vigente y `git status`; el contexto puede quedar desactualizado tras nuevos commits.
- No reemplazar archivos completos si basta un cambio puntual; preservar estilos, nombres, datos demo y callbacks existentes.
- No añadir seguridad/servicios productivos ni datos personales reales. Mantener firmas/biometría como simulación.
- No agregar migraciones destructivas sin dejar clara la pérdida de Room; usar migración explícita si los datos locales deben conservarse.
- No afirmar cobertura, compilación, pruebas, APK, integración ni entrega AVA si no se ejecutaron o no hay evidencia verificable.
- Actualizar este archivo y `Documentacion_Notix.md` al cerrar cada fase, reflejando el código real y anotando verificaciones realizadas.

## 9. Archivos principales para empezar

| Responsabilidad | Archivo |
|---|---|
| Navegación y usuario en sesión | `Notix/app/src/main/java/com/duoc/notix/MainActivity.kt` |
| Esquema Room / DAOs / seeder | `Notix/app/src/main/java/com/duoc/notix/data/local/` |
| Operaciones de autenticación | `Notix/app/src/main/java/com/duoc/notix/repository/AuthRepository.kt` (package `data.repository`) |
| Operaciones de trámites | `Notix/app/src/main/java/com/duoc/notix/repository/TramiteRepository.kt` (package `repository`) |
| Reglas de RUN | `Notix/app/src/main/java/com/duoc/notix/data/validation/RutValidator.kt` |
| Flujos de UI | `Notix/app/src/main/java/com/duoc/notix/ui/screens/` |
| Perfil de usuario | `Notix/app/src/main/java/com/duoc/notix/ui/screens/PerfilScreen.kt` |
| Estado del perfil | `Notix/app/src/main/java/com/duoc/notix/viewmodel/PerfilViewModel.kt` |
| Clientes Retrofit y DTOs | `Notix/app/src/main/java/com/duoc/notix/data/remote/` |
| Feriados externos | `Notix/app/src/main/java/com/duoc/notix/data/repository/FeriadosRepository.kt` |
| Notificaciones locales | `Notix/app/src/main/java/com/duoc/notix/util/NotificacionUtil.kt` |
| Firma simulada | `Notix/app/src/main/java/com/duoc/notix/ui/components/FirmaSimuladaDialog.kt` |
| Corrección y reenvío | `Notix/app/src/main/java/com/duoc/notix/ui/components/CorreccionTramiteDialog.kt` |
| Estado y casos de uso de pantalla | `Notix/app/src/main/java/com/duoc/notix/viewmodel/` |
| Pruebas unitarias | `Notix/app/src/test/java/com/duoc/notix/` |
| Pruebas DAO | `Notix/app/src/androidTest/java/com/duoc/notix/data/local/NotixDaoTest.kt` |
| Reporte de cobertura | `Notix/app/build/reports/jacoco/jacocoTestReport/html/index.html` |
| Versiones y plugins | `Notix/gradle/libs.versions.toml`, `Notix/app/build.gradle.kts` |
| Permisos Android | `Notix/app/src/main/AndroidManifest.xml` |
| Backend de usuarios | `backend/ms-usuarios/src/main/java/com/duoc/notix/usuarios/` |
| Backend de trámites | `backend/ms-tramites/src/main/java/com/duoc/notix/tramites/` |
| Contratos y ejecución del backend | `backend/README.md` |
| Requisitos EP4 | `Ev. Parcial 4_Encargo_Estudiante_ DSY1105.pdf` |

## 10. Referencias del proyecto

- [`../Guia_ejecucion_y_pruebas.md`](../Guia_ejecucion_y_pruebas.md): pasos para levantar backend, conectar emulador/teléfono y probar el flujo completo.
- [`../Comandos_utiles.md`](../Comandos_utiles.md): referencia rápida de build, tests, backend, adb, cobertura y APK.
- [`../../backend/README.md`](../../backend/README.md): microservicios, endpoints, reglas de estado y ejecución local.
- [`../contexto.md`](../contexto.md): caso, alcance académico y objetivo EP4.
- [`plan_de_desarrollo_ep4_notix.md`](plan_de_desarrollo_ep4_notix.md): mapa de fases según la pauta.
- [`implementacion_ep4_notix.md`](implementacion_ep4_notix.md): arquitectura y propuesta técnica detallada; revisar cada afirmación frente al código.
- [`../Documentacion_Notix.md`](../Documentacion_Notix.md): documentación del MVP y detalles de Room; algunas fechas/estados pueden quedar rezagados.
- [`Guia_implementacion_Notix.md`](Guia_implementacion_Notix.md): plan histórico, no fuente del estado actual.
