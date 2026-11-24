# Contexto del proyecto — NotariaX (DSY1105)

> Documento de enfoque. Se irá modificando a medida que se definan los flujos y los datos.
> Fuente: Formato de caso DSY1105 · Duoc UC · Escuela de Informática y Telecomunicaciones · CITT.

---

## 0. Identificación del caso

| Antecedente | Información |
|---|---|
| Desafío | Gestión y seguimiento móvil de trámites notariales |
| Organización | NotariaX |
| Rubro | Servicios de notaría digital en Chile |
| Sede / coordinador | CITT Duoc UC, Sede San Joaquín · Jorge Niochet |
| Asignatura | DSY1105 – Desarrollo de Aplicaciones Móviles |
| Entrega del caso al docente | 3 de septiembre de 2026 |

### La organización
NotariaX es una infraestructura digital que facilita la atención notarial mediante una plataforma en línea para personas naturales, empresas y notarios. Es una microempresa con más de **600 usuarios registrados** y una atención diaria de **1 a 5 usuarios**. Ofrece distintos tipos de trámites y mecanismos de firma, para facilitar la gestión documental y la interacción entre quien solicita un trámite y el notario que lo revisa o autoriza.

### Situación actual
La plataforma está operativa y tiene tres flujos generales:

1. **Firma electrónica simple:** firma manuscrita digital.
2. **Firma electrónica avanzada:** puede involucrar e-token, certificado digital o la adquisición de una firma avanzada.
3. **Firma notariada:** más de **60 formatos predefinidos**, o adjuntar documentos propios para autorización ante notario.

Algunas etapas requieren gestionar varios documentos, usar distintos mecanismos de firma o **cambiar de dispositivo** durante el proceso. Para el desarrollo académico se trabaja con un **flujo simplificado**: carga o selección de documentos → ingreso y verificación de antecedentes → representación de la firma → envío al notario → revisión → eventual devolución con observaciones → consulta del estado.

### Problema
Mejorar la experiencia móvil de gestión y seguimiento de trámites notariales digitales: facilitar la interacción cuando hay varios documentos, distintos tipos de firma y etapas de revisión, reducir errores de interacción y que las personas sepan con claridad el estado de sus solicitudes. Acciones como el uso de e-token mantienen dependencia del computador; el desafío **no busca eliminar todas las restricciones tecnológicas actuales**, sino validar con un MVP una experiencia más simple, clara y continua para lo que se pueda representar dentro del alcance.

---

## 1. ¿Qué es esto?

Un **MVP académico** que representa el ciclo básico de un **trámite notarial digital** para NotariaX: carga y gestión de documentos, revisión de antecedentes, representación de la firma, intercambio de observaciones y consulta del estado del trámite.

**No es** una solución productiva, no se publica en tiendas y **no debe usarse para realizar, validar o autorizar trámites notariales reales**.

## 2. Objetivo

Proponer y validar una experiencia **móvil más simple, clara y continua** para las etapas del trámite que puedan representarse dentro del alcance de la asignatura, reduciendo errores de interacción y permitiendo que las personas conozcan con claridad el estado de sus solicitudes.

## 3. Alcance

### Dentro del alcance
- Un **flujo principal completo** (cada equipo lo prioriza), con las etapas que el equipo incorpore: envío → revisión → observación → corrección → finalización.
- Solo los **perfiles necesarios** para demostrar ese flujo.
- Uso de **datos ficticios, sintéticos, públicos o anonimizados**.
- Integraciones representadas mediante **mocks / APIs de prueba / servicios simulados**.
- Casos de prueba y documentación suficiente para que el MVP pueda continuarse después.

### Fuera del alcance
- Trámites notariales reales.
- Integración real con proveedores de firma electrónica avanzada, e-token, certificados digitales o sistemas de NotariaX.
- Publicación en tiendas de aplicaciones.
- Acceso a sistemas internos, bases de datos, infraestructura o servicios productivos de NotariaX.

> Cualquier funcionalidad adicional debe justificarse por su aporte al desafío y mantenerse dentro del alcance del semestre.

## 4. Perfiles de usuario

Perfiles de referencia (se priorizan solo los necesarios para el flujo elegido):

| Perfil | Rol en el flujo |
|---|---|
| Persona natural | Inicia el trámite, carga documentos, corrige observaciones, consulta estado |
| Representante de persona jurídica / empresa | Igual que el anterior, en nombre de una empresa |
| Notario | Revisa antecedentes y documentos, acepta/finaliza o devuelve con observaciones |
| Administrador | Gestión general (catálogos, usuarios, etc.) — a definir si entra al MVP |

## 5. Requisitos funcionales (RF)

| ID | Requisito |
|---|---|
| RF-01 | Cargar uno o varios documentos ficticios, o seleccionar un formato predeterminado |
| RF-02 | Completar o verificar la información necesaria para representar un trámite |
| RF-03 | Seleccionar o representar el tipo de firma requerido dentro del escenario de prueba |
| RF-04 | Enviar la documentación al notario para su revisión |
| RF-05 | Revisar antecedentes y documentos asociados al trámite (rol notario) |
| RF-06 | Aceptar/finalizar el trámite simulado, o devolverlo con observaciones para corrección |
| RF-07 | Visualizar observaciones y permitir la corrección y reenvío |
| RF-08 | Consultar el estado de trámites y documentos |
| RF-09 | Mantener trazabilidad básica: historial de cambios de estado con fecha/hora simuladas y rol responsable |
| RF-10 | Simular, cuando corresponda, mecanismos externos (e-token, firma electrónica avanzada) |
| RF-11 | (Opcional) Simular validación biométrica del dispositivo (huella / reconocimiento facial), sin capturar datos reales |
| RF-12 | (Opcional) Notificaciones cuando aporten valor al flujo (ej. cambio de estado) |

## 6. Requisitos no funcionales (RNF)

| ID | Requisito |
|---|---|
| RNF-01 | **Plataforma:** uso principal desde dispositivos móviles **Android**, en contexto académico y de demostración |
| RNF-02 | **Usabilidad:** interfaz clara, legible, con navegación simple y fácil de usar desde un teléfono |
| RNF-03 | **Claridad de estado:** el usuario debe poder saber en todo momento en qué estado está su solicitud |
| RNF-04 | **Prevención de errores:** reducir omisiones y errores de interacción en los escenarios de prueba |
| RNF-05 | **Datos:** exclusivamente ficticios, sintéticos, públicos o anonimizados |
| RNF-06 | **Privacidad/seguridad:** sin RUT, nombres, teléfonos, domicilios, firmas, biometría, certificados, credenciales, tokens ni respaldos reales |
| RNF-07 | **Biometría:** solo simulada; no se captura ni almacena ningún dato biométrico real |
| RNF-08 | **Integraciones:** mediante mocks / APIs de prueba; sin credenciales, llaves ni accesos productivos |
| RNF-09 | **Conectividad:** puede usar internet para comunicarse con servicios de prueba |
| RNF-10 | **Trazabilidad:** el historial debe ser suficiente para entender la evolución de un trámite |
| RNF-11 | **Mantenibilidad / continuidad:** documentación suficiente para que otro equipo pueda continuar el MVP |
| RNF-12 | **Tecnologías:** ajustadas a las definidas para DSY1105 |
| RNF-13 | **Demostrabilidad:** funcionamiento verificable mediante casos de prueba |

## 7. Modelo de información (datos del trámite)

Elementos que el MVP puede manejar para representar el proceso:

- Identificador interno del trámite
- Tipo o categoría de trámite
- Formato o documento asociado
- Uno o varios documentos ficticios de prueba
- Tipo de firma requerida
- Estado del trámite
- Fecha y hora simuladas
- Rol participante o responsable
- Antecedentes necesarios para completar el flujo
- Observaciones de revisión o corrección
- Historial básico de cambios de estado

## 7.1 Flujos de firma de NotariaX (referencia para modelar)

| Flujo | Descripción | En el MVP |
|---|---|---|
| Firma electrónica simple | Firma manuscrita digital | Fácil de implementar de verdad (trazo en pantalla con datos ficticios) |
| Firma electrónica avanzada | e-token, certificado digital o adquisición de firma avanzada | Simulada con mock |
| Firma notariada | +60 formatos predefinidos o documento propio, autorizado ante notario | Flujo de revisión del notario (aceptar / devolver con observaciones) |

> Cada equipo elige cuál(es) representar. Hay que demostrar **un flujo principal completo**.

---

## 8. Integraciones (todas simuladas)

- **Firma electrónica avanzada (API de proveedor):** necesidad futura de NotariaX → mock.
- **e-token:** depende del computador en la realidad → se representa por simulación.
- **Certificados digitales:** simulados.
- **Biometría del dispositivo:** simulada.
- **Servicios de otras empresas:** posibilidad futura, no se implementa.

## 9. Criterios de éxito

- El MVP representa de forma comprensible un flujo principal de gestión de trámites notariales digitales.
- Se puede cargar o seleccionar documentación ficticia y consultar el estado del trámite.
- Se representan las etapas de envío, revisión, observación, corrección y finalización incorporadas por el equipo.
- Existe trazabilidad básica para entender la evolución de un trámite.
- Disminuyen las omisiones y errores de interacción en los escenarios de prueba.
- La experiencia móvil es clara, legible y coherente con los perfiles considerados.
- Las dependencias externas o firmas avanzadas se representan de manera comprensible mediante simulaciones.
- El equipo demuestra el funcionamiento con casos de prueba y entrega documentación suficiente.

## 10. Evolución del proyecto: Evaluación Parcial 4 (EP4)

El caso evolucionó desde la definición inicial del MVP hacia la **Evaluación Parcial 4 de DSY1105**. Notix ya cuenta con una aplicación Android en Kotlin y Jetpack Compose, persistencia local Room y flujos demostrativos para persona/empresa y notario. La EP4 pide completar esa base con una solución móvil funcional e integrada, microservicios propios, consumo de una API externa, pruebas unitarias, APK firmado y evidencia de trabajo colaborativo.

La pauta organiza la evaluación en estos indicadores: **IE 3.1.1**, pantallas y flujos; **IE 3.1.2**, microservicios y base de datos; **IE 3.1.3**, integración CRUD entre la app y los microservicios; **IE 3.1.4**, consumo de API externa; **IE 3.2.1**, pruebas unitarias con cobertura mínima del 80 % de la lógica; **IE 3.3.1**, APK firmado y configuración; e **IE 3.3.2**, GitHub, Trello y colaboración demostrable. La pauta advierte que si la app no se ejecuta en la defensa no se pueden evaluar los indicadores posteriores.

### Arquitectura y alcance objetivo EP4

- **App Android:** Kotlin, Jetpack Compose y arquitectura UI → ViewModel → Repository. Room se conserva como almacenamiento local/caché; la integración remota debe permitir enviar, recibir y actualizar información desde la interfaz.
- **Microservicios propios:** dos servicios Spring Boot, uno de usuarios y otro de trámites, con base de datos activa y endpoints funcionales. El plan propone los puertos 8081 y 8082 y H2 persistente en archivo como alternativa simple para la demostración.
- **API externa:** consumirla mediante Retrofit, mostrar el resultado dentro de la app y aislar fallos de red para que no bloqueen los trámites. La guía técnica proponía Mindicador.cl para UF/UTM; la implementación eligió **Nager.Date** para mostrar el próximo feriado de Chile porque Mindicador no respondía correctamente en el entorno de prueba.
- **Integridad del flujo:** firma representada de forma simulada; corrección y reenvío de trámites devueltos; confirmación de legibilidad antes de aprobar; acciones del notario respetando el estado; cierre de sesión para Persona/Empresa.
- **Entrega técnica:** pruebas de ViewModels/repositorios y validaciones; APK de release firmado con `.jks` y configuración reproducible; README con los seis apartados exigidos; repositorio GitHub, planificación Trello, commits técnicos de todos los integrantes y entrega grupal e individual por AVA.

Estos puntos describen el **objetivo de la EP4**, no certifican por sí solos que estén implementados. La pauta permite datos ficticios y servicios de prueba; Notix sigue siendo un MVP académico y no debe conectarse a sistemas productivos ni gestionar trámites reales.

### Estado de referencia al preparar la EP4

Según la documentación técnica del repositorio y la auditoría consignada en la guía EP4, la base existente es la app Android local con Room: acceso, catálogo, creación y seguimiento de trámites, bandeja notarial, aprobación/devolución, historial y avisos internos. La auditoría identifica como brechas EP4 los microservicios Spring Boot, Retrofit, API externa, flujos de firma y reenvío, legibilidad obligatoria, logout de persona/empresa, cobertura de pruebas, APK firmado y evidencias de planificación/colaboración. La documentación técnica también registra que los adjuntos persisten solo como metadatos y que los avisos no son notificaciones del sistema.

Este estado es una **línea base documental**, no una verificación automática del estado de cada archivo. Para el avance actualizado consultar [`Documentacion_Notix.md`](Documentacion_Notix.md) y la carpeta [`implementacion/`](implementacion/estado_actual.md), que reúne el estado actual, la guía técnica, el plan y la pauta EP4.

## 11. Material de apoyo del caso (referencia original)

| Material | Para qué se usa | Estado |
|---|---|---|
| Descripción conceptual del flujo de trámites notariales digitales | Comprender contexto, perfiles y etapas generales | Definida en este documento y material del caso |
| Catálogo ficticio de tipos de trámite, estados y tipos de firma | Modelar formularios, reglas y flujos del MVP | Representado en los modelos y datos demo de Notix |
| Documentos y registros sintéticos de ejemplo | Pruebas, demostraciones y validación funcional | Datos demo locales documentados en `Documentacion_Notix.md` |

## 12. Pendientes y decisiones de continuidad

- [ ] Completar y validar las brechas funcionales descritas en la guía EP4.
- [ ] Construir y ejecutar los microservicios Spring Boot con base de datos y endpoints CRUD.
- [ ] Integrar la app con los servicios propios mediante Retrofit, conservando Room según la estrategia elegida.
- [ ] Elegir e integrar una API externa mediante Retrofit con presentación en UI y manejo de fallos.
- [ ] Implementar y medir cobertura unitaria de al menos 80 % de la lógica requerida.
- [ ] Generar y comprobar APK firmado, `.jks` y configuración de release.
- [ ] Completar README de seis apartados, evidencia de Trello/commits colaborativos y entregas AVA.
- [ ] Mantener la app demostrable y probar la ejecución integral antes de la defensa.

## 13. Notas / decisiones

- El flujo académico se mantiene con datos ficticios/sintéticos y firmas, proveedores e integraciones simulados cuando corresponda.
- La guía histórica [`implementacion/Guia_implementacion_Notix.md`](implementacion/Guia_implementacion_Notix.md) describe etapas anteriores del MVP; no debe tomarse como registro del estado actual.
- La guía concreta de EP4 y el plan de desarrollo contienen propuestas técnicas y checklists; distinguir siempre sus recomendaciones del trabajo efectivamente realizado.
