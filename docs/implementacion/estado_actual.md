# Estado actual de Notix y guía para continuar la EP4

> **Propósito:** dar a una IA o integrante una descripción fiel del repositorio antes de implementar el plan EP4. Este archivo documenta el código inspeccionado el **23-11-2026**; no reemplaza la pauta ni certifica que el proyecto compile o que los servicios externos funcionen.
>
> **Orden de referencia:** la pauta PDF define qué se evalúa; `plan_de_desarrollo_ep4_notix.md` organiza los requisitos; `implementacion_ep4_notix.md` propone un diseño de solución; este documento aclara qué existe realmente en el código fuente. Si hay contradicciones, no asumir que una recomendación ya fue implementada: inspeccionar el código y actualizar esta guía tras cada hito.

---

## 1. Resumen ejecutivo

Notix es una app Android académica para representar trámites notariales con información ficticia. La entrega implementada en el repositorio es una app Kotlin/Jetpack Compose con arquitectura **Compose → ViewModel → Repository → Room**. Tiene login/registro local, selección de formatos, creación y consulta de trámites, firma simulada obligatoria, bandeja notarial, devolución/aprobación con legibilidad confirmada, corrección/reenvío, historial, avisos locales y logout para los tres perfiles. Además, `backend/` contiene dos microservicios Spring Boot (`ms-usuarios` y `ms-tramites`) con H2 persistente, API REST y seeders demo.

En el árbol de fuentes inspeccionado existen los microservicios Spring Boot de `backend/`, pero **no** existen capa Retrofit, API externa, pruebas unitarias funcionales de la app, configuración de APK firmado ni artefactos `.jks`/APK. La app sigue usando Room como fuente de verdad hasta que se implemente la Fase 3.

La app no procesa trámites reales. Mantener datos de demostración y no incorporar credenciales, personas, firmas, documentos ni sistemas productivos.

> **Fase 1 de la EP4 cerrada el 23-11-2026:** firma simulada, corregir/reenviar, legibilidad obligatoria, bloqueo de estados del notario y logout Persona/Empresa. Verificado con compilación y `assembleDebug`; no se ejecutó la app en emulador ni se midió cobertura.
>
> **Fase 2 de la EP4 cerrada el 23-11-2026:** `ms-usuarios` (8081) y `ms-tramites` (8082) con Spring Boot 4.2, Java 21, Maven, JPA, H2 en archivo, seeders y endpoints REST. Verificado con `./mvnw test` (11 pruebas) y llamadas HTTP reales a ambos servicios. La app Android todavía no consume el backend.

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
| Dependencias remotas | No declaradas: Retrofit, Gson/Moshi y OkHttp no aparecen en el catálogo Gradle actual |
| Pruebas JVM | JUnit está declarado; `ExampleUnitTest` es el único test encontrado y solo comprueba una suma |
| Manifest de red | No tiene permiso `INTERNET` ni `usesCleartextTraffic` en la versión observada |
| Firma release | No hay `signingConfigs`; `release` solo desactiva optimización |
| Backend | Java 21, Spring Boot `4.2.0`, Maven Wrapper `3.3.4` con Maven `3.10.1` |
| Backend persistencia | Spring Data JPA / Hibernate `7.4.15`, H2 `2.5.252` en archivo |
| Backend puertos | `ms-usuarios` 8081 y `ms-tramites` 8082 |
| Integración app ↔ backend | No implementada: no hay Retrofit ni permiso `INTERNET` |

**Precaución Gradle:** el catálogo observado declara Kotlin `2.2.10` y KSP `2.1.20-1.0.31`. Antes de modificar estas versiones, comprobar compatibilidad con la configuración y el wrapper existentes. No actualizar versiones en bloque ni reemplazar el catálogo a partir de ejemplos.

## 4. Paquetes y convenciones que no se deben confundir

- Los archivos físicos bajo `model/` declaran `package com.duoc.notix.data.model`. Importar modelos con `com.duoc.notix.data.model.*`; la ruta física no refleja el package.
- `AuthRepository` está físicamente en `repository/AuthRepository.kt`, pero declara `com.duoc.notix.data.repository`.
- `TramiteRepository` está en `repository/TramiteRepository.kt` y declara `com.duoc.notix.repository`.
- Algunos nombres de archivo conservan capitalización irregular: `AuthViewmodel.kt`, `TramitesViewmodel.kt`; las clases son `AuthViewModel` y `MisTramitesViewModel`.
- No mover paquetes ni renombrar clases como parte incidental de la integración. Si se decide normalizar, hacerlo como cambio acotado, actualizar todos los imports y revisar compilación antes de continuar.

## 5. Flujos y datos que sí existen

### 5.1 Navegación y sesión

`MainActivity` controla `Welcome`, `Login`, `CrearCuenta`, `MisTramites`, `CatalogoFormatos`, `CrearTramite`, `BandejaNotario` y `Avisos` mediante `enum Pantalla`.

- Login y registro derivan al notario a la bandeja y a persona/empresa a Mis Trámites.
- `usuarioId` se conserva con `rememberSaveable`, pero parte con el valor por defecto `USR-001`; la sesión no se restaura de Room ni de preferencias al reiniciar.
- El logout de Notario, Persona y Empresa vuelve a `Welcome` con confirmación y limpia el usuario en memoria. No hay `SessionManager` persistente.
- `onVerTramite` en `MainActivity` está vacío; el detalle lo maneja actualmente un diálogo dentro de `MisTramitesScreen`.
- No se observó un `BackHandler` para coordinar el botón atrás.

### 5.2 Autenticación local

`AuthRepository` consulta/escribe `UsuarioDao`. Normaliza el correo, revisa duplicados, tipo de cuenta y contraseña. `LoginViewModel` y `AuthViewModel` extienden `AndroidViewModel` y crean `NotixDatabase`/repositorio internamente; no reciben dependencias por constructor. Esto dificulta pruebas unitarias JVM aisladas y debe considerarse al diseñar los tests.

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
- Room es la única fuente de datos existente; describirlo como “caché” solo será correcto después de que se implemente y use un backend remoto.
- Cambiar entidades requiere incrementar versión y definir la migración. La 2 → 3 conserva datos; el fallback actual puede borrar la base local si falta una ruta.
- `DatabaseSeeder` asegura usuarios demo en cada inicio y agrega trámites solamente si no encuentra ninguno. Evitar que el seeder sobrescriba o compita con datos remotos cuando se conecte la API.

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
- **La app no consume estos servicios todavía.** No hay Retrofit, permiso `INTERNET`, DTO remotos ni sincronización Room ↔ backend. Por tanto, no describir el backend como “integrado” ni Room como “caché” aún.

## 6. Hallazgos críticos para no seguir afirmaciones incorrectas del plan

La guía EP4 contiene propuestas, pero algunos diagnósticos deben verificarse contra el fuente y no copiarse a ciegas:

1. **Legibilidad en creación (resuelto):** `CrearTramiteScreen` ahora inicializa `legibilidadConfirmada = false` y exige marcar la declaración de la persona que crea el trámite. Ese booleano es independiente de `Tramite.legibilidadConfirmada`, que sigue siendo la confirmación del notario al aprobar.
2. **Legibilidad en revisión (resuelto):** `RevisionTramiteDialog` incluye checkbox obligatorio, bloquea Aprobar sin marcarlo y persiste `legibilidadConfirmada = true`. `BandejaNotarioViewModel` y `TramiteRepository` también validan estado y legibilidad.
3. **Autenticación y pruebas:** los ViewModels de login/registro no tienen inyección; los tests que los instancien requieren refactor o una estrategia deliberada. No es suficiente añadir MockK sin separar construcción de Room.
4. **Dos paquetes repository:** no asumir que Auth y Tramite comparten el mismo package.
5. **“Firma” en el modelo (resuelto):** `Tramite` incorpora `firmaEvidencia` con valores ficticios por `TipoFirma`; no representa una firma legal ni certificados reales.
6. **CRUD:** el backend ya expone `DELETE` para trámites y usuarios, pero la app no tiene eliminar/cancelar en UI, repository ni DAO. La pauta pide flujos CRUD desde interfaz; definir qué representa DELETE y sus efectos antes de conectarlo.
7. **Fechas e IDs:** el backend usa `fechaCreacion`/`fechaActualizacion` y genera IDs secuenciales `TX-2026-XXXX`; la app todavía no tiene fecha de creación, usa strings formateados en el historial y `(1000..9999).random()` para el ID. No ordenar por ID esperando orden cronológico en la app.
8. **API externa recomendada ≠ integrada:** Mindicador es una recomendación del plan técnico, no una decisión confirmada por el código. Si se elige, aislar sus DTO/client/repository y no hacer que su disponibilidad condicione trámites.
9. **Room como caché es una decisión futura:** el backend existe, pero el diseño remoto primero + Room caché descrito en `implementacion_ep4_notix.md` todavía no está conectado. No reemplazar repositorios locales antes de definir cómo se actualizan las pantallas observadas por Flow.
10. **No declarar verificación no realizada:** este documento se basa en inspección estática, en la compilación/`assembleDebug` de la app, en `./mvnw test` del backend y en llamadas HTTP locales verificadas. No afirma que la app se haya probado en emulador, que exista integración app-backend, que se alcance cobertura o que se pase una defensa.

## 7. Objetivo EP4 frente al estado observado

| Área EP4 | Estado observado en fuentes |
|---|---|
| App Android y flujos existentes | Base implementada con Compose/Room; brechas de flujo arriba descritas |
| `ms-usuarios` y `ms-tramites` Spring Boot + BD | Implementados en `backend/` con H2 persistente y endpoints REST |
| Retrofit contra microservicios | No declarado ni implementado; la app no consume el backend |
| API externa visible en UI | No declarada ni implementada |
| Firma simulada específica por `TipoFirma` | Implementada y obligatoria antes de enviar; evidencia persistida en Room |
| Corregir/re-enviar devueltos | Implementado, con reemplazo de documentos observados e historial |
| Confirmación de legibilidad al aprobar | Implementada en diálogo, ViewModel y repositorio |
| DELETE/cancelación desde UI | No implementado en la app; el backend sí expone `DELETE` |
| Tests de lógica y cobertura ≥80 % | No demostrados en la app; hay únicamente test de ejemplo. El backend tiene 11 pruebas propias |
| APK firmado y `.jks` | No encontrados; Gradle no tiene `signingConfigs` |
| README EP4, Trello y colaboración | El README existente no reemplaza la evidencia requerida; confirmar y completar puntos faltantes con datos reales del equipo |

La EP4 requiere app funcional integrada, microservicios con BD y endpoints, integración CRUD, API externa visible, pruebas con cobertura mínima del 80 %, APK firmado y evidencia GitHub/Trello. Los microservicios ya existen y funcionan por separado, pero la integración desde la app y la API externa siguen pendientes. La pauta PDF es la fuente normativa.

## 8. Orden de trabajo sugerido y reglas de continuidad

Seguir el plan `implementacion_ep4_notix.md`, pero completar cada hito sin romper el flujo existente:

1. ~~Cerrar y validar los flujos pendientes de la app~~ **Hecho (Fase 1, 23-11-2026):** firma simulada, reenvío (`DEVUELTO → ENVIADO`), legibilidad obligatoria, bloqueo de estados y logout. Verificado con compilación y `assembleDebug`; falta prueba manual en emulador.
2. ~~Crear los microservicios con contratos acordados, persistencia y datos ficticios reproducibles.~~ **Hecho (Fase 2, 23-11-2026):** `ms-usuarios` y `ms-tramites` con Spring Boot 4.2, H2, seeders, endpoints y 11 pruebas. Verificado con `./mvnw test` y llamadas HTTP locales. No consumir todavía estos contratos desde la app sin actualizar DTO y documentación.
3. Añadir Retrofit y capa remota, definir fuente de verdad y estrategia de error/refresco antes de reemplazar escrituras Room. Conservar los `Flow` que alimentan Compose o actualizar deliberadamente las pantallas.
4. Añadir API externa de forma aislada, con carga/error/fallback; nunca bloquear operaciones notariales simuladas.
5. Refactorizar dependencias donde se necesite para tests, luego implementar pruebas de reglas, transiciones y errores; medir cobertura real, no inferirla por cantidad de tests.
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
| Firma simulada | `Notix/app/src/main/java/com/duoc/notix/ui/components/FirmaSimuladaDialog.kt` |
| Corrección y reenvío | `Notix/app/src/main/java/com/duoc/notix/ui/components/CorreccionTramiteDialog.kt` |
| Estado y casos de uso de pantalla | `Notix/app/src/main/java/com/duoc/notix/viewmodel/` |
| Versiones y plugins | `Notix/gradle/libs.versions.toml`, `Notix/app/build.gradle.kts` |
| Permisos Android | `Notix/app/src/main/AndroidManifest.xml` |
| Backend de usuarios | `backend/ms-usuarios/src/main/java/com/duoc/notix/usuarios/` |
| Backend de trámites | `backend/ms-tramites/src/main/java/com/duoc/notix/tramites/` |
| Contratos y ejecución del backend | `backend/README.md` |
| Requisitos EP4 | `Ev. Parcial 4_Encargo_Estudiante_ DSY1105.pdf` |

## 10. Referencias del proyecto

- [`../../backend/README.md`](../../backend/README.md): microservicios, endpoints, reglas de estado y ejecución local.
- [`../contexto.md`](../contexto.md): caso, alcance académico y objetivo EP4.
- [`plan_de_desarrollo_ep4_notix.md`](plan_de_desarrollo_ep4_notix.md): mapa de fases según la pauta.
- [`implementacion_ep4_notix.md`](implementacion_ep4_notix.md): arquitectura y propuesta técnica detallada; revisar cada afirmación frente al código.
- [`../Documentacion_Notix.md`](../Documentacion_Notix.md): documentación del MVP y detalles de Room; algunas fechas/estados pueden quedar rezagados.
- [`Guia_implementacion_Notix.md`](Guia_implementacion_Notix.md): plan histórico, no fuente del estado actual.
