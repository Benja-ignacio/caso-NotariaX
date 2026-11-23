# Mapa de Desarrollo e Instrucciones — Evaluación Parcial 4 (DSY1105)

> **Proyecto:** Notix (Caso NotariaX)  
> **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles  
> **Objetivo:** Evolucionar el MVP local actual (Room) hacia una arquitectura integrada con microservicios propios en Spring Boot, consumo de una API externa, pruebas unitarias (≥ 80 %) y APK firmado.

---

## 1. Mapa de Evolución de Arquitectura

Actualmente **Notix** funciona 100 % offline con Room. Para obtener el 100 % de logro en la rúbrica de la Evaluación Parcial 4, la arquitectura debe evolucionar al siguiente esquema:

```text
┌────────────────────────────────────────────────────────────────────────────┐
│                        APP MÓVIL: NOTIX (Kotlin + Compose)                 │
│                                                                            │
│   [UI Screens] ──▶ [ViewModels] ──▶ [Repositories]                         │
│                                        │                                   │
│           ┌────────────────────────────┼────────────────────────────┐      │
│           ▼                            ▼                            ▼      │
│  ┌─────────────────┐        ┌─────────────────────┐      ┌──────────────┐  │
│  │ Room (Local)    │        │ Retrofit: Backend   │      │ Retrofit:    │  │
│  │ Caché / Sesión  │        │ (Spring Boot)       │      │ API Externa  │  │
│  └─────────────────┘        └──────────┬──────────┘      └──────┬───────┘  │
└────────────────────────────────────────┼────────────────────────┼──────────┘
                                         │ CRUD (JSON)            │ GET (JSON)
                                         ▼                        ▼
                              ┌─────────────────────┐      ┌──────────────┐
                              │ Microservicios      │      │ Slot API     │
                              │ Spring Boot + BD    │      │ Externa      │
                              │ (Usuarios/Trámites) │      │ (3 opciones) │
                              └─────────────────────┘      └──────────────┘

  TRANSVERSAL (Obligatorio para aprobar):
  ├── 🧪 Pruebas Unitarias (JUnit + MockK) sobre ViewModels / Repositories (≥ 80 %)
  ├── 📦 Firma Digital (.jks + signingConfigs en build.gradle.kts + app-release.apk)
  └── 📋 GitHub (commits de ambos) + Trello + README (6 puntos) + Subida AVA
```

---

## 2. Hoja de Ruta Paso a Paso

### Fase 1: Cerrar los flujos incompletos de la App (`IE 3.1.1` — 15 %)

> ⚠️ **Regla crítica de evaluación:** Si el proyecto no se ejecuta correctamente o presenta errores de flujo en la defensa, los ítems posteriores se califican con puntaje cero.

Antes de conectar servidores, cierra las brechas detectadas en el estado actual de **Notix**:

- [x] **Corrección y reenvío tras devolución:** En `MisTramitesScreen`, cuando un trámite esté en estado `DEVUELTO`, habilitar un botón *"Corregir y reenviar"* que permita editar antecedentes o adjuntar un nuevo documento y devolverlo al estado `ENVIADO` (registrando el evento en el historial).
- [x] **Representación de firma simulada:** Agregar un paso visual antes de confirmar el envío según `TipoFirma`:
  - `SIMPLE`: recuadro táctil (`Canvas`) para dibujar trazo ficticio.
  - `AVANZADA` / `NOTARIADA`: modal de confirmación simulada de e-token o certificado digital.
- [x] **Blindar el diálogo del Notario (`BandejaNotarioScreen`):**
  - Agregar el `Checkbox` obligatorio ligado a `legibilidadConfirmada` para habilitar el botón **Aprobar**.
  - Ocultar o deshabilitar los botones **Aprobar / Devolver** cuando el trámite ya esté en estado `FINALIZADO`.
- [x] **Cierre de sesión (Logout) en Persona/Empresa:** Conectar la acción de salida en la barra superior o perfil para regresar a `WelcomeScreen` y facilitar el cambio de roles durante la demostración.

---

### Fase 2: Construir los Microservicios en Spring Boot (`IE 3.1.2` — 15 %)

Migra las 5 entidades actuales de `NotixDatabase` hacia Spring Boot con una base de datos activa (PostgreSQL, MySQL o H2 persistente en archivo). Dividir el backend en **2 microservicios** cubre el requerimiento en plural de la pauta:

| Microservicio | Puerto sugerido | Tablas que gestiona | Endpoints REST mínimos (CRUD) |
| :--- | :--- | :--- | :--- |
| **1. `ms-usuarios`** | `8081` | `usuarios` | `POST /api/usuarios/registro`<br>`POST /api/usuarios/login`<br>`GET /api/usuarios/{id}` |
| **2. `ms-tramites`** | `8082` | `tramites`, `documentos`, `historial_eventos`, `avisos` | `GET /api/tramites` (todos, vista Notario)<br>`GET /api/tramites/solicitante/{id}`<br>`POST /api/tramites` (Crear)<br>`PUT /api/tramites/{id}/estado` (Aprobar / Devolver / Reenviar)<br>`DELETE /api/tramites/{id}` (Eliminar/cancelar)<br>`GET /api/avisos/{usuarioId}` |

> 💡 **Recomendación:** Implementa un `CommandLineRunner` (Seeder) en Spring Boot con los usuarios demo (`ana.demo@notariax.test` y `notario.demo@notariax.test`) para que la base de datos inicie lista para probar.

---

### Fase 3: Integrar Retrofit — Backend + API Externa (`IE 3.1.3` e `IE 3.1.4` — 30 %)

1. **Dependencias en `Notix/gradle/libs.versions.toml` y `Notix/app/build.gradle.kts`:**
   - `com.squareup.retrofit2:retrofit:2.11.0`
   - `com.squareup.retrofit2:converter-gson:2.11.0`
   - `com.squareup.okhttp3:logging-interceptor:4.12.0`
2. **Permisos y tráfico HTTP en `AndroidManifest.xml`:**
   - Agregar `<uses-permission android:name="android.permission.INTERNET" />`.
   - Agregar `android:usesCleartextTraffic="true"` dentro de `<application>` para permitir conexión HTTP local con Spring Boot (`http://10.0.2.2:8081/` en emulador o IP local en teléfono físico).
3. **Instancias Retrofit separadas en `data/remote/`:**
   - `UsuariosRetrofitClient` (`baseUrl` puerto `8081`) y `TramitesRetrofitClient` (`baseUrl` puerto `8082`).
   - `ExternalRetrofitClient` (`baseUrl` exclusiva de la API externa elegida).
4. **Conectar Repositorios al Backend:** Actualizar `AuthRepository` y `TramiteRepository` para consumir los endpoints CRUD de Spring Boot.
5. **Conectar la API Externa de forma aislada:**
   - Envolver la llamada en `runCatching { ... }`. Si la API externa falla o no hay red, debe retornar `null` y mostrar un texto de respaldo sin bloquear la creación ni el envío de trámites.
   - Mostrar la información obtenida en una tarjeta visible dentro de la interfaz de Compose (`CatalogoFormatosScreen`, `CrearTramiteScreen` o `MisTramitesScreen`).

#### Opciones de API Externa para Notix (Elegir 1)

Todas las opciones son públicas, gratuitas y **no requieren API Key** (cumpliendo con el **RNF-08** del caso NotariaX):

| Opción | API Externa | `baseUrl` y Endpoint `GET` | Aporte al contexto NotariaX | Dónde mostrarla en la UI |
| :--- | :--- | :--- | :--- | :--- |
| **Opción 1** | **Mindicador.cl**<br>*(Indicadores económicos Chile)* | **Base:** `https://mindicador.cl/`<br>**GET:** `api` | Entrega el valor real del día de la **UF** y **UTM** en pesos chilenos (referencia para aranceles y cuantías notariales). | Tarjeta en `CatalogoFormatosScreen` o `CrearTramiteScreen`: *"Valor UF hoy: $XX.XXX CLP (Referencia arancelaria)"*. |
| **Opción 2** | **Nager.Date**<br>*(Feriados legales Chile)* | **Base:** `https://date.nager.at/`<br>**GET:** `api/v3/PublicHolidays/2026/CL` | Entrega los feriados legales de Chile en 2026 para informar plazos de revisión en **días hábiles**. | Banner en `MisTramitesScreen` o `BandejaNotarioScreen`: *"Próximo día inhábil: [Nombre] ([Fecha])"*. |
| **Opción 3** | **Open-Meteo**<br>*(Clima en tiempo real)* | **Base:** `https://api.open-meteo.com/`<br>**GET:** `v1/forecast?latitude=-33.49&longitude=-70.61&current_weather=true` | Entrega temperatura y estado del clima en Santiago/Macul para trámites que requieran firma o retiro presencial. | Encabezado informativo en `WelcomeScreen` o `MisTramitesScreen`. |

---

### Fase 4: Pruebas Unitarias con Cobertura ≥ 80 % (`IE 3.2.1` — 15 %)

La rúbrica exige cubrir al menos el **80 % de la lógica** en módulos como `ViewModel` o `Repository` usando **JUnit, MockK o Kotest**.

1. **Dependencias en `app/build.gradle.kts` (`testImplementation`):**
   - `junit:junit:4.13.2`
   - `io.mockk:mockk:1.13.12`
   - `org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1`
2. **Suites de prueba mínimas (`app/src/test/java/com/duoc/notix/`):**
   - `RutValidatorTest`: RUN válidos, inválidos, dígito `K`, vacíos y formateo.
   - `AuthViewModelTest` / `AuthRepositoryTest`: login exitoso, clave incorrecta, rol equivocado, correo duplicado en registro.
   - `TramitesViewModelTest` / `TramiteRepositoryTest`: creación de trámite, aprobación (`ENVIADO` → `FINALIZADO`), devolución con observación (`ENVIADO` → `DEVUELTO`), reenvío y manejo seguro ante caída de la API externa.
3. **Verificación:** Ejecutar **Run 'Tests in notix' with Coverage** en Android Studio y confirmar que la cobertura en `viewmodel`, `repository` y `data/validation` sea **≥ 80 %**.

---

### Fase 5: APK Firmado, GitHub, Trello y Entrega AVA (`IE 3.3.1` y `IE 3.3.2` — 25 %)

#### 5.1 Generación de APK Firmado
- [ ] En Android Studio: **Build → Generate Signed App Bundle / APK... → APK**.
- [ ] Crear KeyStore `notix_release.jks` y guardar contraseñas y alias.
- [ ] Configurar `signingConfigs` y `buildTypes { release { ... } }` en `Notix/app/build.gradle.kts` usando ruta relativa al `.jks` (ej. `file("../keystore/notix_release.jks")`).
- [ ] Incluir el archivo `app-release.apk` y el `.jks` dentro del repositorio y tomar captura de evidencia.

#### 5.2 GitHub, Trello y `README.md`
- [ ] **Trello:** Tablero con tareas distribuidas entre ambos integrantes (*Por hacer*, *En progreso*, *En revisión*, *Terminado*).
- [ ] **GitHub:** Repositorio público con acceso al docente, evidenciando commits técnicos reales de **ambos integrantes** tanto en la app móvil como en los microservicios.
- [ ] **`README.md` raíz con los 6 apartados obligatorios:**
  1. Nombre del proyecto (`Notix — Caso NotariaX`)
  2. Integrantes
  3. Funcionalidades
  4. Endpoints utilizados (separando **API externa** y **Microservicios Spring Boot**)
  5. Pasos para ejecutar (Backend + App)
  6. Captura del APK firmado y `.jks`

---

## 3. Checklist Final de Entrega (AVA)

- [ ] **Enlace Grupal AVA:** Subido por un integrante con todo el código fuente (app + microservicios), APK firmado, `.jks` y evidencia de Trello.
- [ ] **Enlace Individual AVA:** Activado por **ambos integrantes** con nombre, apellido, número de equipo y nombre de la aplicación *(no activarlo implica nota 1.0 individual)*.
- [ ] **Congelamiento de repositorio:** No realizar **ningún commit** después de la fecha y hora límite *(cualquier cambio posterior asigna nota 1.0 grupal e individual automáticamente)*.