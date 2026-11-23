# Guía de ejecución y pruebas — Notix

> Guía práctica para levantar el backend, conectar la app y probar el flujo completo de la EP4.
> Datos ficticios. No usar con trámites, firmas ni documentos reales.

---

## 1. Arquitectura de ejecución

La app Android se conecta a **dos microservicios** y a **una API externa**:

| Componente | Puerto / URL | Uso |
|---|---|---|
| `ms-usuarios` | `http://localhost:8081` | Registro, login y usuarios |
| `ms-tramites` | `http://localhost:8082` | Trámites, documentos, historial y avisos |
| Nager.Date | `https://date.nager.at/api/v3/NextPublicHolidays/CL` | Próximo feriado de Chile |
| App Android | — | Room como caché + Retrofit |

Cada microservicio tiene su propia base H2 en archivo:

- `backend/ms-usuarios/data/msusuarios.mv.db`
- `backend/ms-tramites/data/mstramites.mv.db`

El orden recomendado es: **backend primero, app después**.

---

## 2. Requisitos

- JDK 21.
- Android Studio o SDK Android 37.
- Emulador Android o dispositivo físico con depuración USB.
- Puertos `8081` y `8082` libres.
- Maven no es necesario: el proyecto incluye `mvnw`.

Comprobar Java:

```bash
java -version
```

Si el entorno no tiene configurado JDK 21:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
export PATH="$JAVA_HOME/bin:$PATH"
```

> Ajustar la ruta según el equipo. En este repositorio se usó
> `/usr/lib/jvm/java-21-temurin-jdk`.

---

## 3. Levantar el backend

### Opción A — JAR (recomendada)

Desde la raíz del repositorio:

```bash
cd backend
./mvnw -DskipTests package
```

Luego abrir **dos terminales**:

```bash
# Terminal 1
cd backend/ms-usuarios
java -jar target/ms-usuarios-0.0.1-SNAPSHOT.jar
```

```bash
# Terminal 2
cd backend/ms-tramites
java -jar target/ms-tramites-0.0.1-SNAPSHOT.jar
```

### Opción B — Maven directo

También funciona, sin generar JAR:

```bash
# Terminal 1
cd backend
./mvnw -pl ms-usuarios spring-boot:run
```

```bash
# Terminal 2
cd backend
./mvnw -pl ms-tramites spring-boot:run
```

Cada proceso queda ocupando su terminal. Se detienen con `Ctrl+C`.

### Verificar

```bash
curl http://localhost:8081/api/usuarios
curl http://localhost:8082/api/tramites
```

Deben aparecer los usuarios y trámites demo.

Consolas H2:

- `http://localhost:8081/h2-console`
- `http://localhost:8082/h2-console`

Usuario `sa`, contraseña vacía.

### Detener y reiniciar la demo

Detener ambos procesos y borrar las bases:

```bash
rm -rf backend/ms-usuarios/data backend/ms-tramites/data
```

Al volver a levantar, los seeders vuelven a crear los datos demo.

> No borrar `data/` con los servicios activos: H2 mantiene un bloqueo de archivo.

---

## 4. Ejecutar la app

### Emulador

1. Iniciar el AVD.
2. Comprobar `adb devices`.
3. Instalar y abrir la app.

La app detecta el emulador y usa automáticamente `10.0.2.2:8081` y `10.0.2.2:8082`. No hace falta `adb reverse`.

### Dispositivo físico por USB

1. Activar opciones de desarrollador y depuración USB.
2. Autorizar el equipo cuando el teléfono lo pregunte.
3. Comprobar:

```bash
adb devices
```

Debe aparecer el dispositivo como `device`.

4. Redirigir los puertos:

```bash
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8082 tcp:8082
adb reverse --list
```

La app detecta el dispositivo físico y usa `127.0.0.1`.

> Si se desconecta y reconecta el teléfono, repetir los `adb reverse`.

### Instalar la app

Opción verificada:

```bash
cd Notix
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

También se puede abrir `Notix/` en Android Studio y ejecutar `app`.

### Abrir la app desde consola

```bash
adb shell am start -n com.duoc.notix/.MainActivity
```

### Dispositivo físico por Wi-Fi (alternativa)

Si se prefiere no usar USB:

1. Obtener la IP LAN del PC.
2. Editar `Notix/app/src/main/java/com/duoc/notix/data/remote/ApiConfig.kt`.
3. Reemplazar `127.0.0.1` por la IP del PC, por ejemplo `http://192.168.1.50:8081/`.
4. Abrir los puertos `8081` y `8082` en el firewall.
5. Recompilar e instalar.

---

## 5. Credenciales demo

| Perfil | Correo | Clave |
|---|---|---|
| Persona | `ana.demo@notariax.test` | `demo123` |
| Notario | `notario.demo@notariax.test` | `notario123` |

En el login hay que seleccionar el perfil correcto (**Persona** o **Notario**).

---

## 6. Flujo completo de prueba

### Paso 0 — Estado limpio (opcional)

Con los servicios detenidos:

```bash
rm -rf backend/ms-usuarios/data backend/ms-tramites/data
adb shell pm clear com.duoc.notix
```

Luego levantar backend e instalar/abrir la app.

### Paso 1 — Login Persona

1. Abrir **Iniciar Sesión**.
2. Seleccionar **Persona**.
3. Correo: `ana.demo@notariax.test`.
4. Clave: `demo123`.
5. Entrar.

Resultado esperado:

- Encabezado “Hola, Ana”.
- Contadores de Mis Trámites.
- Trámites demo: `TX-2026-0042` devuelto, `TX-2026-0039` y `TX-2026-0001` enviados, `TX-2026-0031` finalizado.

### Paso 2 — Catálogo y feriados externos

1. Pulsar **Nuevo trámite**.
2. Revisar la tarjeta “Feriados de referencia”.

Resultado esperado:

- Si Nager.Date responde: nombre y fecha del próximo feriado de Chile.
- Si no responde: “Feriados no disponibles. Puedes continuar con tu trámite.”
- En ambos casos se puede continuar.

### Paso 3 — Crear un trámite

1. Seleccionar **Poder simple**.
2. Revisar antecedentes precargados.
3. Adjuntar un archivo (opcional; si no se adjunta se usa un nombre demo).
4. Marcar **“Declaro que los antecedentes proporcionados son verídicos y legibles.”**
5. Pulsar **Revisar y enviar solicitud**.
6. Confirmar la firma simulada:
   - **SIMPLE:** dibujar un trazo.
   - **AVANZADA:** usar el código demo de 6 dígitos.
   - **NOTARIADA:** marcar la autorización simulada.
7. Revisar el resumen y pulsar **Confirmar y enviar**.

Resultado esperado:

- La app vuelve a Mis Trámites.
- Aparece un ID nuevo, por ejemplo `TX-2026-0043`, en estado “En revisión”.
- Comprobar en backend:

```bash
curl http://localhost:8082/api/tramites/TX-2026-0043
```

### Paso 4 — Aprobar como notario

1. Cerrar sesión.
2. Entrar con perfil **Notario**, correo `notario.demo@notariax.test`, clave `notario123`.
3. Abrir el trámite nuevo en la bandeja.
4. Marcar **“Confirmo que los documentos son legibles.”**
5. Pulsar **Aprobar trámite**.

Resultado esperado en backend:

```bash
curl http://localhost:8082/api/tramites/TX-2026-0043
```

- `estado: FINALIZADO`
- `legibilidadConfirmada: true`
- Historial con evento de `NOTARIO`.

### Paso 5 — Devolución y reenvío

**Devolver un trámite enviado:**

1. Como notario, abrir `TX-2026-0001`.
2. Pulsar **Devolver con obs.**
3. Escribir una observación y confirmar.

**Corregir y reenviar:**

1. Cerrar sesión y entrar como Persona.
2. Abrir `TX-2026-0001`.
3. Pulsar **Corregir y reenviar**.
4. Editar antecedentes si se desea.
5. Reenviar.

Resultado esperado:

- Vuelve a `ENVIADO`.
- Se limpia la observación.
- Se agrega historial y aviso.

**Probar reemplazo obligatorio:**

1. Abrir `TX-2026-0042`, que está `DEVUELTO` y tiene un documento `OBSERVADO`.
2. Corregir y reenviar sin adjuntar archivo.
3. La app debe impedir el envío y pedir un documento de reemplazo.
4. Adjuntar un archivo y reenviar.

**Eliminar un trámite:**

1. Como Persona, abrir un trámite no finalizado.
2. Pulsar **Eliminar trámite**.
3. Confirmar la acción.
4. Verificar que desaparece de la lista y del backend:

```bash
curl http://localhost:8082/api/tramites
```

### Paso 6 — Avisos

1. Abrir **Avisos** desde el encabezado o la barra inferior.
2. Verificar los avisos generados por creación, devolución y aprobación.
3. Tocar un aviso para marcarlo como leído.

Los avisos se leen desde `ms-tramites` y se cachean en Room.

### Paso 7 — Perfil y navegación inferior

1. Tocar **Perfil** en la barra inferior.
2. Verificar nombre, correo, tipo de cuenta, RUN, documento, teléfono y comuna.
3. Si la cuenta es Empresa o Notario, revisar los datos adicionales.
4. Tocar **Cerrar sesión** y confirmar.
5. Verificar que vuelve a `Welcome`.

La barra inferior tiene tres tabs conectados: **Trámites**, **Avisos** y **Perfil**. El antiguo “Inicio” se eliminó porque no tenía una pantalla propia.

### Paso 8 — Comprobar persistencia

1. Cerrar la app.
2. Volver a abrirla.
3. La sesión no se conserva, pero los datos locales siguen en Room.
4. Al iniciar sesión de nuevo, las pantallas sincronizan con el backend.

### Paso 9 — Probar sin backend

1. Detener `ms-usuarios` y `ms-tramites`.
2. Abrir la app.

Resultado esperado:

- Se mantiene la última lista local de trámites y avisos.
- El login puede funcionar con la copia local si el usuario ya existe en Room.
- Crear, aprobar, devolver o reenviar muestra un error de conexión.
- No se pierde la caché.

---

## 7. Pruebas automáticas

### Backend

```bash
cd backend
./mvnw test
```

Resultado esperado: **11 pruebas**, sin fallos.

### App Android

```bash
cd Notix
./gradlew :app:testDebugUnitTest :app:assembleDebug
./gradlew :app:jacocoTestReport
./gradlew :app:jacocoTestCoverageVerification
```

Resultado esperado:

- **82 pruebas unitarias**, sin fallos.
- **96,26 % de cobertura de línea** y 75,61 % de ramas.
- Reporte: `Notix/app/build/reports/jacoco/jacocoTestReport/html/index.html`.
- El umbral de 80 % se verifica con `jacocoTestCoverageVerification`.

Pruebas DAO en dispositivo/emulador:

```bash
cd Notix
./gradlew :app:connectedDebugAndroidTest
```

Resultado esperado: **7 pruebas DAO**, sin fallos.

---

## 8. Problemas frecuentes

### `JAVA_HOME` inválido

Error:

```text
ERROR: JAVA_HOME is set to an invalid directory
```

Solución:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
```

### Puerto ocupado

```bash
ss -ltnp | grep -E '8081|8082'
```

Detener el proceso que ocupa el puerto o cambiar el puerto en `application.properties` y en `ApiConfig.kt`.

### El emulador no arranca

En este entorno el AVD terminó con `SIGSEGV`. Alternativas:

- Usar un dispositivo físico con `adb reverse`.
- Abrir el emulador desde Android Studio.
- Revisar memoria disponible y la aceleración KVM.

### La app no conecta con el backend

Comprobar en este orden:

1. `curl http://localhost:8081/api/usuarios` desde el PC.
2. `curl http://localhost:8082/api/tramites` desde el PC.
3. En emulador, confirmar que se usa `10.0.2.2`.
4. En dispositivo físico, ejecutar `adb reverse` y confirmar con `adb reverse --list`.
5. Revisar que el firewall no bloquee los puertos.

### Nager.Date no responde

Es un fallo controlado. La tarjeta debe mostrar el texto de respaldo y el flujo de trámites debe seguir funcionando. Comprobar la API desde el PC:

```bash
curl https://date.nager.at/api/v3/NextPublicHolidays/CL
```

### La app muestra datos antiguos

La sincronización se ejecuta al entrar a Mis Trámites, Bandeja o Avisos. Para reiniciar por completo:

```bash
adb shell pm clear com.duoc.notix
rm -rf backend/ms-usuarios/data backend/ms-tramites/data
```

Luego reiniciar backend y app.

### H2 indica archivo bloqueado

Detener ambos servicios antes de borrar `data/`. H2 mantiene el archivo abierto mientras el proceso está activo.

---

## 9. Checklist rápido

- [ ] JDK 21 disponible.
- [ ] Puertos 8081 y 8082 libres.
- [ ] `ms-usuarios` responde en 8081.
- [ ] `ms-tramites` responde en 8082.
- [ ] Emulador usa `10.0.2.2` o dispositivo físico tiene `adb reverse`.
- [ ] Login Persona funciona.
- [ ] El catálogo muestra el próximo feriado o fallback.
- [ ] Se crea un trámite y aparece en el backend.
- [ ] El notario aprueba con legibilidad.
- [ ] El backend queda en `FINALIZADO`.
- [ ] Se prueba devolución y reenvío.
- [ ] Se elimina un trámite no finalizado y desaparece del backend.
- [ ] Perfil muestra los datos y permite cerrar sesión.
- [ ] La barra inferior navega entre Trámites, Avisos y Perfil.
- [ ] Los avisos se sincronizan.
- [ ] Sin backend se conserva la caché local y se informa el error.
- [ ] `./mvnw test` pasa (11 pruebas).
- [ ] `./gradlew :app:testDebugUnitTest` pasa (82 pruebas).
- [ ] `./gradlew :app:connectedDebugAndroidTest` pasa (7 pruebas DAO).
- [ ] `jacocoTestCoverageVerification` pasa con cobertura de línea ≥ 80 %.
