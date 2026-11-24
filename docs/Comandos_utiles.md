# Comandos útiles — Notix

> Referencia rápida para ejecutar, probar y depurar la app Android y los microservicios.
> Todos los datos son ficticios.

---

## 1. Preparar el entorno

```bash
# Comprobar Java
java -version

# En este equipo se usó JDK 21 Temurin
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
export PATH="$JAVA_HOME/bin:$PATH"

# SDK y dispositivo
export ANDROID_HOME=$HOME/Android/Sdk
adb devices
```

Desde la raíz del repositorio:

```bash
cd /home/seiiwl/Documents/git-repos/caso-NotariaX
```

---

## 2. Backend

### Compilar ambos microservicios

```bash
cd backend
./mvnw -DskipTests package
```

Genera:

```text
backend/ms-usuarios/target/ms-usuarios-0.0.1-SNAPSHOT.jar
backend/ms-tramites/target/ms-tramites-0.0.1-SNAPSHOT.jar
```

### Ejecutar con JAR

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

Las bases quedan en:

```text
backend/ms-usuarios/data/
backend/ms-tramites/data/
```

### Ejecutar con Maven

Terminal 1:

```bash
cd backend
./mvnw -pl ms-usuarios spring-boot:run
```

Terminal 2:

```bash
cd backend
./mvnw -pl ms-tramites spring-boot:run
```

### Verificar que están arriba

```bash
curl -i http://localhost:8081/api/usuarios
curl -i http://localhost:8082/api/tramites
```

También:

```bash
ss -ltnp | grep -E '8081|8082'
```

### Pruebas del backend

```bash
cd backend
./mvnw test
```

Resultado esperado: **11 pruebas**, sin fallos.

Solo un módulo:

```bash
./mvnw -pl ms-usuarios test
./mvnw -pl ms-tramites test
```

Una prueba específica:

```bash
./mvnw -pl ms-tramites test -Dtest=TramiteControllerTest
```

### Consolas H2

- `http://localhost:8081/h2-console`
- `http://localhost:8082/h2-console`

Usuario: `sa`  
Contraseña: vacía

### Reiniciar datos demo del backend

Detener ambos servicios y borrar:

```bash
rm -rf backend/ms-usuarios/data backend/ms-tramites/data
```

Al volver a iniciar, los seeders crean usuarios, trámites y avisos demo.

> Si los JAR se ejecutan desde `backend/` y no desde cada módulo, H2 puede crear
> `backend/data/`. Revisar esa ruta antes de borrar.

---

## 3. Endpoints útiles con `curl`

### Usuarios

```bash
# Listar usuarios
curl http://localhost:8081/api/usuarios

# Login Persona
curl -X POST http://localhost:8081/api/usuarios/login \
  -H 'Content-Type: application/json' \
  -d '{"tipoCuenta":"PERSONA","correo":"ana.demo@notariax.test","contrasena":"demo123"}'

# Login Notario
curl -X POST http://localhost:8081/api/usuarios/login \
  -H 'Content-Type: application/json' \
  -d '{"tipoCuenta":"NOTARIO","correo":"notario.demo@notariax.test","contrasena":"notario123"}'

# Obtener usuario
curl http://localhost:8081/api/usuarios/USR-001
```

### Trámites

```bash
# Todos
curl http://localhost:8082/api/tramites

# De un solicitante
curl http://localhost:8082/api/tramites/solicitante/USR-001

# Detalle
curl http://localhost:8082/api/tramites/TX-2026-0042

# Aprobar
curl -X PUT http://localhost:8082/api/tramites/TX-2026-0001/estado \
  -H 'Content-Type: application/json' \
  -d '{"estado":"FINALIZADO","legibilidadConfirmada":true,"rol":"NOTARIO","comentario":"Documentos legibles."}'

# Devolver
curl -X PUT http://localhost:8082/api/tramites/TX-2026-0001/estado \
  -H 'Content-Type: application/json' \
  -d '{"estado":"DEVUELTO","observacion":"Anexo ilegible.","legibilidadConfirmada":false,"rol":"NOTARIO"}'

# Eliminar un trámite no finalizado
curl -i -X DELETE http://localhost:8082/api/tramites/TX-2026-0042
```

### Avisos

```bash
# Avisos de un usuario
curl http://localhost:8082/api/avisos/USR-001

# Marcar como leído
curl -X PUT http://localhost:8082/api/avisos/AVS-001/leido
```

---

## 4. App Android

Todos los comandos se ejecutan desde `Notix/`:

```bash
cd Notix
```

### Compilar

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

APK debug:

```text
Notix/app/build/outputs/apk/debug/app-debug.apk
```

### Instalar y abrir

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.duoc.notix/.MainActivity
```

Alternativa:

```bash
./gradlew :app:installDebug
```

### Limpiar build

```bash
./gradlew :app:clean
```

### Pruebas unitarias JVM

```bash
./gradlew :app:testDebugUnitTest
```

Resultado esperado: **82 pruebas**, sin fallos.

Una clase específica:

```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.duoc.notix.viewmodel.MisTramitesViewModelTest"
```

Reporte:

```text
Notix/app/build/reports/tests/testDebugUnitTest/index.html
```

### Cobertura

```bash
./gradlew :app:jacocoTestReport
./gradlew :app:jacocoTestCoverageVerification
```

Reporte HTML:

```text
Notix/app/build/reports/jacoco/jacocoTestReport/html/index.html
```

Cobertura actual aproximada:

- Línea: **96,30 %**
- Ramas: **74,93 %**
- Umbral mínimo configurado: **80 % de línea**

### Pruebas DAO / instrumentadas

Requieren dispositivo o emulador conectado:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Resultado esperado: **7 pruebas DAO**, sin fallos.

Reporte:

```text
Notix/app/build/reports/androidTests/connected/debug/index.html
```

---

## 5. Conectar la app con el backend

### Emulador

No requiere `adb reverse`. La app detecta el emulador y usa:

```text
http://10.0.2.2:8081/
http://10.0.2.2:8082/
```

### Dispositivo físico por USB

```bash
adb devices
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8082 tcp:8082
adb reverse --list
```

La app detecta el dispositivo físico y usa `127.0.0.1`.

`adb reverse --list` debe mostrar ambos puertos:

```text
UsbFfs tcp:8081 tcp:8081
UsbFfs tcp:8082 tcp:8082
```

Si la lista está vacía, la app no podrá conectarse aunque el backend esté corriendo.

**El reverse se pierde al desconectar el teléfono, reiniciar el equipo o reiniciar ADB.** Repetir:

```bash
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8082 tcp:8082
adb reverse --list
```

Si hay varios dispositivos conectados, usar el número de serie:

```bash
adb devices
adb -s R5GL916XP0P reverse tcp:8081 tcp:8081
adb -s R5GL916XP0P reverse tcp:8082 tcp:8082
```

Después de agregar el reverse, reiniciar la app:

```bash
adb shell am force-stop com.duoc.notix
adb shell am start -n com.duoc.notix/.MainActivity
```

Si sigue fallando, revisar logs:

```bash
adb logcat -d | grep -iE "okhttp|ConnectException|Sin conexión|CLEARTEXT"
```

### Dispositivo físico por Wi-Fi

Editar `Notix/app/src/main/java/com/duoc/notix/data/remote/ApiConfig.kt` y reemplazar
`127.0.0.1` por la IP LAN del PC. Abrir los puertos `8081` y `8082` en el firewall
y recompilar.

---

## 6. Resetear la app

```bash
# Borrar datos, Room, sesión y SharedPreferences
adb shell pm clear com.duoc.notix

# Desinstalar
adb uninstall com.duoc.notix
```

Forzar cierre:

```bash
adb shell am force-stop com.duoc.notix
```

Ver la base Room en un build debug:

```bash
adb shell run-as com.duoc.notix ls -l databases
```

---

## 7. Logs y depuración

### Logcat general

```bash
adb logcat -c
adb logcat | grep -iE "com.duoc.notix|AndroidRuntime|okhttp"
```

Solo crashes:

```bash
adb logcat -d | grep -iE "FATAL EXCEPTION|AndroidRuntime"
```

### Notificaciones

```bash
# Conceder permiso manualmente en Android 13+
adb shell pm grant com.duoc.notix android.permission.POST_NOTIFICATIONS

# Revocar
adb shell pm revoke com.duoc.notix android.permission.POST_NOTIFICATIONS

# Ver notificaciones activas
adb shell dumpsys notification --noredact | grep -A8 com.duoc.notix
```

Para probar una notificación:

1. Tener app y backend activos.
2. Generar una devolución desde `curl`.
3. Abrir **Avisos** en la app para forzar la sincronización.
4. Revisar `dumpsys notification`.

### Estado de la app

```bash
adb shell dumpsys activity activities | grep -A3 com.duoc.notix
adb shell dumpsys package com.duoc.notix | grep -A2 POST_NOTIFICATIONS
```

---

## 8. APK release y firma

Variables esperadas por `app/build.gradle.kts`:

```bash
export NOTIX_KEYSTORE=/ruta/notix-release.jks
export NOTIX_STORE_PASSWORD='...'
export NOTIX_KEY_PASSWORD='...'
```

Generar:

```bash
cd Notix
./gradlew :app:assembleRelease
```

APK:

```text
Notix/app/build/outputs/apk/release/app-release.apk
```

Copia de entrega actual:

```text
~/Notix-1.0-release.apk
```

Verificar firma:

```bash
# apksigner está en build-tools del SDK
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

> Ajustar `36.0.0` a la versión instalada en `$ANDROID_HOME/build-tools/`.

Ver datos del keystore:

```bash
keytool -list -v -keystore "$NOTIX_KEYSTORE" -alias notix
```

> No versionar el `.jks` ni contraseñas. `Notix/.gitignore` ignora `*.jks`.

---

## 9. Git

```bash
git status --short
git diff
git diff --check
git log --oneline -10
git show --stat HEAD
```

Ver archivos ignorados:

```bash
git status --ignored --short
```

---

## 10. Problemas frecuentes

### `JAVA_HOME` inválido

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-temurin-jdk
```

### Puerto ocupado

```bash
ss -ltnp | grep -E '8081|8082'
```

Detener el proceso o cambiar puerto.

### H2 bloqueado

Detener los servicios antes de borrar `data/`.

### Dispositivo no aparece

```bash
adb kill-server
adb start-server
adb devices
```

Aceptar el diálogo de depuración USB en el teléfono.

### App no conecta con backend

1. Verificar `curl` desde el PC.
2. En emulador, confirmar `10.0.2.2`.
3. En dispositivo físico, ejecutar `adb reverse tcp:8081 tcp:8081` y `adb reverse tcp:8082 tcp:8082`.
4. Confirmar con `adb reverse --list` que aparezcan ambos puertos.
5. Reiniciar la app con `adb shell am force-stop com.duoc.notix` y volver a abrirla.
6. Revisar `adb logcat -d | grep -iE "okhttp|ConnectException|CLEARTEXT"`.
7. Revisar firewall solo si se usa la IP LAN en lugar de `adb reverse`.

### El emulador falla con `SIGSEGV`

En este entorno ocurrió con el AVD local. Alternativas:

- Usar dispositivo físico con `adb reverse`.
- Abrir el emulador desde Android Studio.

### Datos antiguos en la app

```bash
adb shell pm clear com.duoc.notix
rm -rf backend/ms-usuarios/data backend/ms-tramites/data
```

Reiniciar backend y app.

---

## 11. Resumen rápido

| Objetivo | Comando |
|---|---|
| Compilar backend | `cd backend && ./mvnw -DskipTests package` |
| Probar backend | `cd backend && ./mvnw test` |
| Correr usuarios | `cd backend/ms-usuarios && java -jar target/ms-usuarios-0.0.1-SNAPSHOT.jar` |
| Correr trámites | `cd backend/ms-tramites && java -jar target/ms-tramites-0.0.1-SNAPSHOT.jar` |
| Compilar app | `cd Notix && ./gradlew :app:assembleDebug` |
| Instalar app | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |
| Pruebas JVM | `cd Notix && ./gradlew :app:testDebugUnitTest` |
| Cobertura | `cd Notix && ./gradlew :app:jacocoTestReport :app:jacocoTestCoverageVerification` |
| Pruebas DAO | `cd Notix && ./gradlew :app:connectedDebugAndroidTest` |
| Abrir app | `adb shell am start -n com.duoc.notix/.MainActivity` |
| USB reverse | `adb reverse tcp:8081 tcp:8081 && adb reverse tcp:8082 tcp:8082` |
| Reset app | `adb shell pm clear com.duoc.notix` |
| Reset backend | `rm -rf backend/ms-usuarios/data backend/ms-tramites/data` |
| Notificaciones | `adb shell dumpsys notification --noredact` |
| APK release | `cd Notix && ./gradlew :app:assembleRelease` |
