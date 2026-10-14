# AppSalud — P2.1

Repositorio grupal de **Seguridad en Entornos Móviles**, Universidad de Málaga. Práctica **P2.1: El entorno de desarrollo y el artefacto de la aplicación**, apartados 5.1–5.5.

**Planificación de P2.1 acordada por el equipo.** El 14/10/2026, Ángel confirma la aceptación del grupo de la idea de aplicación, la organización y el reparto inicial descritos en este README. Se conserva la base Android existente y el reconocimiento realizado por David. El reparto podrá ajustarse para mantener una carga comparable.

## Equipo y dominio

| Campo | Información |
| --- | --- |
| Identificación del equipo | Supergrupo 1, Grupo D |
| Nombre propio del equipo | JADE Salud |
| Integrantes | Ángel Gallego Güeto; Jorge de Torres Suárez; David López Callejón; Esteban Sánchez Gámez |
| Dominio asignado | Salud — Historial médico personal |
| Nombre de la aplicación y del proyecto | `AppSalud`, coherente con `settings.gradle.kts` y `strings.xml` |
| Repositorio | [SEM_GRUPO_D](https://github.com/Sauvageduck24/SEM_GRUPO_D) |

## Descripción de la aplicación (5.1)

AppSalud será una **carpeta y agenda personal de salud** en la que cada usuario, tras autenticarse, podrá reunir sus antecedentes, tratamientos, citas e informes médicos procedentes de distintos centros. El usuario añadirá las citas y los datos del historial manualmente, fotografiará informes en papel desde la aplicación y podrá consultar información sobre medicamentos mediante una API remota. Los antecedentes clínicos y el contenido de los informes serán los datos más sensibles y se guardarán localmente para su consulta sin conexión; la búsqueda de medicamentos requerirá acceso a internet.

### Problema y valor para el usuario

Los informes en papel, fotografías sueltas, tratamientos y datos de las próximas consultas suelen estar repartidos en distintos lugares. La aplicación permitirá encontrarlos desde una misma interfaz y tener a mano la información que el usuario necesita para una consulta médica.

El valor de la aplicación es **organizar la información que el propio usuario incorpora**, incluso si procede de centros diferentes. El usuario deberá mantenerla actualizada: las citas se registran manualmente y la versión básica no incluye conexión con las agendas de centros sanitarios ni importación automática de la historia clínica de ClicSalud+.

### Funciones de la versión básica

- **Acceso personal:** iniciar y cerrar sesión y acceder al historial correspondiente al usuario autenticado.
- **Resumen e historial:** registrar, consultar, editar y borrar antecedentes relevantes, como alergias, y tratamientos con medicamento, pauta indicada y fechas.
- **Agenda personal:** registrar, consultar, editar y borrar citas con fecha, hora, centro y motivo, y ver las próximas consultas. Se anotan citas ya concertadas por el usuario.
- **Carpeta de informes:** capturar una fotografía con la cámara integrada, añadir título y fecha, y guardar, consultar o borrar el informe.
- **Consulta de medicamentos:** buscar un medicamento y mostrar información de una API remota, con estados de carga, resultado vacío y error. El servicio y el contrato de la petición se concretarán en las prácticas posteriores.

### Cuatro requisitos técnicos mínimos

| Requisito | Función que lo cubre |
| --- | --- |
| Autenticación | Inicio y cierre de sesión y control del acceso al historial del usuario |
| Almacenamiento local de datos sensibles | Persistencia de antecedentes, tratamientos, citas y fotografías de informes en el dispositivo |
| Al menos una llamada a una API remota | Consulta de información sobre medicamentos; forma parte de la versión básica y no depende de implementar IA |
| Al menos un permiso peligroso justificado | `android.permission.CAMERA` para fotografiar informes en papel desde una cámara integrada en la aplicación |

Se utilizará **CAMERA** en lugar del permiso de almacenamiento sugerido para Salud en la tabla del enunciado, cuyo apartado 5.1 permite cambiar el permiso si se justifica por el dominio. Seleccionar documentos mediante Storage Access Framework no requiere permisos del sistema y, por sí solo, no cubriría este requisito; tampoco basta con delegar la captura en otra aplicación. `CAMERA` tiene nivel de protección `dangerous` y se solicitará en tiempo de ejecución al elegir fotografiar un informe; si el usuario lo deniega, podrá seguir consultando su historial. Referencias: [acceso a documentos](https://developer.android.com/training/data-storage/shared/documents-files) y [permiso CAMERA](https://developer.android.com/reference/android/Manifest.permission#CAMERA).

### Ampliaciones opcionales: IA y recordatorios

Estas ampliaciones se valorarán después de completar la versión básica y revisar los enunciados posteriores:

- **Organización asistida de informes:** extraer de la fotografía el título, la fecha, el centro y el tipo de documento, proponiendo datos que el usuario revisará y corregirá antes de guardarlos.
- **Consulta de informes con IA:** ayudar a localizar información en los documentos que el usuario haya incorporado, mostrando el informe de origen para poder comprobar la respuesta. La función consultará información registrada y no decidirá ni modificará tratamientos.
- **Recordatorios de citas y medicación:** permitir que el usuario configure, modifique y cancele avisos a partir de una cita o de una pauta introducida y confirmada. La programación de notificaciones no necesita IA; si esta propone un horario a partir de un informe, el usuario confirmará el medicamento, la dosis y el horario antes de activarlo.

Quedan **fuera de la versión básica comprometida** y sin responsable asignado todavía. Antes de incorporarlas se estimarán su esfuerzo y reparto, el servicio de IA, el tratamiento de datos, las credenciales y el coste, así como los permisos y las restricciones de Android para notificaciones y ejecución en segundo plano. La aplicación debe cumplir los cuatro requisitos sin depender de estas ampliaciones.

**Alcance de P2.1:** se define y organiza el proyecto, sin implementar estas funcionalidades todavía. El código actual mantiene la pantalla de la plantilla de Android Studio (`Hello Android!`). La descripción, las funciones y la arquitectura definen la planificación acordada para las prácticas posteriores.

## Convenciones del equipo (5.2)

Se conserva el proyecto existente como base común. Las versiones de Android y el paquete de esta tabla coinciden con la configuración versionada; la estrategia de integración y el reparto inicial se recogen como convenciones del equipo.

| Decisión | Configuración o convención | Motivo |
| --- | --- | --- |
| Paquete común | `es.uma.seguridad.salud`, tanto `namespace` como `applicationId` | Mantener una única identidad para la aplicación y evitar cambios de paquete durante la integración |
| `minSdk` | `26` (Android 8.0) | Cumplir la base sugerida en P2.1 y acotar la compatibilidad a API 26 o superior |
| `targetSdk` | `37` | Asumir las restricciones de comportamiento y seguridad de API 37 y mantener el objetivo alineado con el entorno de compilación y la prueba documentada por Esteban |
| `compileSdk` | `release(37)` | Conservar la plataforma contra la que se ha compilado el proyecto |
| Tecnología base | Kotlin, Jetpack Compose y Gradle Kotlin DSL | Mantener la plantilla requerida y los archivos existentes |
| Ramas | `main` como rama estable; `feature/<tarea>` para funcionalidades y `fix/<tarea>` para correcciones | Desarrollar cada cambio en una rama identificable |
| Revisión e integración | Pull request revisada por otro integrante antes de fusionar | Revisar el cambio y su documentación antes de incorporarlo a la base común |
| Comprobaciones | Revisar el diff; ejecutar `:app:assembleDebug` si se modifica código o configuración; instalar y probar el flujo afectado cuando cambie el comportamiento | Verificar lo que cambia y documentar el resultado; los cambios solo de documentación no requieren reconstruir el APK |

**Justificación de `targetSdk`:** se conserva API 37 para adoptar sus restricciones de seguridad y comportamiento, y para mantener coherencia con `compileSdk = 37` y el emulador API 37 de la verificación del 11 de octubre. Esa comprobación pertenece al entorno de Esteban; cada integrante debe disponer del SDK y del JDK requeridos antes de compilar. Las versiones se revisarán conjuntamente si alguna práctica exige un cambio, sin modificarlas de forma aislada.

### Arquitectura y convenciones de código

Se utilizará una **Clean Architecture sencilla, organizada por funcionalidades**, adaptando los principios utilizados en CutMax al proyecto Kotlin y Jetpack Compose. Cada funcionalidad separará `presentation` (pantallas y ViewModels), `domain` (entidades, acciones y contratos de repositorio) y `data` (implementaciones de repositorio y acceso local o remoto). Los componentes y recursos compartidos se ubicarán en `core` cuando exista una necesidad de reutilización.

Las pantallas delegarán las acciones en sus ViewModels; la lógica de negocio se mantendrá independiente de Compose y del almacenamiento, y los repositorios permitirán cambiar el acceso a datos sin modificar la interfaz. Se crearán los casos de uso y las abstracciones que necesite cada funcionalidad, manteniendo la estructura proporcionada al tamaño del proyecto.

Como convenciones de **Clean Code**, se utilizarán nombres descriptivos, funciones con una responsabilidad, componentes reutilizables y tratamiento explícito de errores. Se evitarán duplicación, dependencias circulares y llamadas directas a la API o a la base de datos desde las pantallas. Esta arquitectura se implementará en las prácticas posteriores; en P2.1 se documenta la organización prevista.

### Responsables

El apartado 5.2 exige una persona responsable de cada requisito mínimo. El equipo acepta el siguiente reparto inicial, comunicado por Ángel el 14/10/2026. Los bloques incluyen pantallas, persistencia de cada funcionalidad, gestión de errores, comprobaciones y documentación; su carga se revisará antes de implementarlos.

| Responsable | Requisito que coordina | Alcance de su bloque |
| --- | --- | --- |
| Ángel Gallego Güeto | Autenticación | Pantallas de acceso y cierre de sesión, gestión de sesión, control de acceso a las pantallas internas y navegación común |
| David López Callejón | Almacenamiento local de datos sensibles | Base de almacenamiento y contratos compartidos, separación de datos por usuario, pantalla de resumen y gestión de antecedentes: alta, consulta, edición y borrado |
| Jorge de Torres Suárez | API remota | Búsqueda y ficha de medicamentos desde la API, estados de carga y error, y gestión de tratamientos: alta, consulta, edición y borrado, incluida su persistencia |
| Esteban Sánchez Gámez | Permiso peligroso y su justificación | Permiso CAMERA y su denegación, captura y carpeta de informes con título y fecha, y agenda de citas manuales; incluida la persistencia de informes y citas |

David coordina la base de almacenamiento, pero **cada responsable implementa la persistencia de sus propios datos** utilizando los contratos acordados. David no recibe toda la programación de tratamientos, citas e informes. Jorge combina la integración de API con tratamientos, y Esteban combina cámara e informes con una agenda sencilla, para compensar las diferencias de carga entre los cuatro requisitos.

Todos incluyen las capas necesarias de presentación, dominio y datos en su bloque, así como sus comprobaciones y documentación. La arquitectura y los componentes comunes se acuerdan entre los cuatro; Ángel coordina la navegación, pero no asume en solitario toda la infraestructura ni todas las integraciones.

**Revisión de cambios:** Ángel y David revisan mutuamente sus cambios; Jorge y Esteban hacen lo mismo. Los cambios en contratos compartidos requieren revisión de los responsables afectados antes de integrar. La protección de cada requisito se abordará en el Bloque III y el reparto de la auditoría de otra aplicación en el Bloque IV se acordará cuando se conozca su enunciado.

**Equilibrio de carga:** el reparto es inicial y ajustable. Antes de programar se desglosará cada bloque en tareas y se estimará su esfuerzo según las tecnologías elegidas y el enunciado de la práctica correspondiente. El bloque de Esteban puede crecer al combinar cámara, informes y citas, y el de Ángel depende de la solución de autenticación. Si las estimaciones o el trabajo real muestran diferencias, se trasladarán tareas concretas a quien tenga menor carga; por ejemplo, la agenda de citas podrá reasignarse.

La estimación incluirá implementación, integración, comprobaciones y documentación, además del trabajo grupal ya realizado por cada integrante. Todos colaboran en la integración y revisan cambios de otro compañero. Se mantendrá una persona identificada por requisito, aunque cambie la distribución de tareas. La IA y los recordatorios se estimarán y repartirán aparte si se decide incorporarlos.

Para **P2.1**, la base y su verificación ya están documentadas por Esteban, el reconocimiento lo ha realizado David y Ángel prepara el cierre de la definición y las convenciones. Esto no acredita que cada integrante haya completado sus entregables individuales.

## Proyecto base y preparación del repositorio (5.3)

El repositorio incluye el módulo `app`, las fuentes y recursos de la plantilla, los ficheros Gradle Kotlin DSL, el catálogo de versiones y el Gradle Wrapper completo. No se necesitan archivos grupales adicionales: la descripción, las convenciones y la tabla de reconocimiento se incluyen en este README.

La configuración conservada utiliza Gradle `9.6.0`, Android Gradle Plugin `9.4.1` y el plugin de Compose `2.2.10`. `gradle/gradle-daemon-jvm.properties` fija el JDK del daemon en versión `25`; la compatibilidad del código Java del módulo está configurada en `11`.

Para compilar, abrir el proyecto en Android Studio y configurar el SDK de plataforma API 37 y el JDK requerido por Gradle. La ruta del SDK se mantiene en `local.properties`, que es local y está excluido de Git. No copiar rutas personales al repositorio.

Desde la raíz, en PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`. Para instalarlo y arrancar la plantilla, con un emulador o dispositivo de API 26 o superior conectado y `adb` disponible en el PATH:

```powershell
.\gradlew.bat :app:installDebug --console=plain
adb shell am start -W -n es.uma.seguridad.salud/.MainActivity
```

En Linux/macOS, desde la raíz:

```sh
sh ./gradlew :app:assembleDebug --console=plain
sh ./gradlew :app:installDebug --console=plain
adb shell am start -W -n es.uma.seguridad.salud/.MainActivity
```

La instalación y el lanzamiento requieren un dispositivo conectado. Si hay varios, seleccionar el destino mediante `ANDROID_SERIAL` y `adb -s <serial>`.

### Verificación realizada

Comprobación local del **11 de octubre de 2026**:

- `.\gradlew.bat :app:clean :app:assembleDebug :app:installDebug --console=plain`: **BUILD SUCCESSFUL**, 38 tareas ejecutadas e instalación correcta en el emulador API 37.
- Lanzamiento de `es.uma.seguridad.salud/.MainActivity` con ADB: **Status: ok**.
- `adb shell dumpsys activity activities`: `MainActivity` figura como `ResumedActivity`.

### Archivos excluidos y acceso compartido

`.gitignore` excluye las carpetas `build` de todos los módulos, cachés de Gradle y Kotlin, configuración local del IDE, `local.properties`, `AGENTS.md`, artefactos APK/AAB, almacenes de claves, claves privadas y archivos habituales de secretos. El Gradle Wrapper, incluido su JAR, permanece versionable. `app/.gitignore` conserva además la exclusión de `/build`.

La preparación inicial declaró el índice local sin archivos generados ni de claves. David revalidó el remoto el 13/10/2026: `main` en `e35a522da4cf2f5f95674d771719edda5f540c5d`, con dos commits de Esteban. La revisión de los archivos de ambos commits no encontró APK, almacenes de claves, claves privadas o `local.properties` versionados. Esto no certifica ausencia absoluta de secretos. David dispone de permiso WRITE y publica el reconocimiento en una rama mediante PR. La consulta autenticada a la API de GitHub del **14/10/2026** confirma que el repositorio es **público** y enumera tres cuentas con permisos de colaboración: `Sauvageduck24` (admin), `daviidltp` (write) y `AnglGG` (write). La lectura del repositorio está disponible para los integrantes y el profesorado mediante su URL. **Pendiente:** confirmar la cuenta y los permisos del cuarto integrante, y comunicar al profesorado el enlace por el canal indicado; no se ha confirmado que esa comunicación se haya realizado.

## Reconocimiento de una aplicación real (5.4)

**Completado por David López Callejón el 13/10/2026.** MedTimer pertenece al dominio Salud: recordatorios y registro de medicación. Se consultó su ficha de Play y se instaló el APK full firmado del autor, sin cuenta de Google ni datos médicos. [Informe reproducible y evidencias](docs/p21/reconocimiento-david.md).

| Datos del análisis | Valor |
| --- | --- |
| Aplicación elegida y versión | MedTimer `1.26.0`, versionCode `187`, variante full |
| Nombre del paquete | `com.futsch1.medtimer` |
| Procedencia | **APK full del release oficial de GitHub; aplicación también publicada en Google Play**. [Play](https://play.google.com/store/apps/details?id=com.futsch1.medtimer) · [Release v1.26.0](https://github.com/Futsch1/medTimer/releases/tag/v1.26.0). No se afirma identidad con el artefacto servido por Play. |
| Dispositivo/emulador, versión de Android y fecha del análisis | AVD dedicado Pixel 6 ARM64, Android 14/API 34, parche `2023-09-05`; 13/10/2026 |
| Versiones de ADB, apktool, aapt2 y apksigner utilizadas | ADB `37.0.0-14910828` (`1.0.41`); apktool `3.0.1`; aapt2 `2.20-13193326`; apksigner `0.9`, Build Tools `36.0.0` |

Tabla sobre el **base.apk extraído de la aplicación instalada**:

| Qué mirar | Con qué | Observación |
| --- | --- | --- |
| Permisos declarados | `aapt2 dump permissions base.apk` | **14 solicitudes:** `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `ACCESS_NOTIFICATION_POLICY`, `USE_FULL_SCREEN_INTENT`, `VIBRATE`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `USE_BIOMETRIC`, `USE_FINGERPRINT` (prefijo `android.permission`), más `com.futsch1.medtimer.NOTIFICATION_PROCESSED` y `com.futsch1.medtimer.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Las dos declaraciones propias no se cuentan dos veces. Niveles, estado concedido y justificación en el informe. |
| Componentes exportados | apktool + manifiesto completo | **3 de 17:** activities `com.futsch1.medtimer.MainActivity` y `androidx.compose.ui.tooling.PreviewActivity`, sin permiso; receiver `androidx.profileinstaller.ProfileInstallReceiver`, protegido por `android.permission.DUMP`. Los tres declaran `exported=true` y están enabled. Se revisaron 5 activities, 8 receivers, 2 services, 2 providers y 0 activity-alias. |
| Esquemas de firma | `apksigner verify --print-certs -v base.apk` | **Verifica v3**; v1/v2/v3.1/v4/SourceStamp false en esta ejecución. Firmante `C=49, ST=BW, L=Stuttgart, CN=Florian Fetz`. SHA-256 del certificado: `69bc7c72e499045b44c87fc8463f599472deb408c48de38de8554ae7754b9d84`. |
| `minSdk` y `targetSdk` | `aapt2 dump badging base.apk` | `28` y `36`, respectivamente; compileSdk observado `37`. |
| Número de piezas instaladas | `adb -s emulator-5580 shell pm path com.futsch1.medtimer` + pull de todas las rutas | **1 pieza: `base.apk`, sin splits.** 15.484.781 bytes. SHA-256: `e92c955e54a62262d0b776259faf6b04a2070bbf5920f38f1fa0db2d3aaf4330`, igual a la descarga oficial. [Mapeo ruta -> pieza -> hash](docs/p21/evidencias/piezas.json). |

Observaciones del reconocimiento:

- **Permisos difíciles de justificar:** ubicación en segundo plano tiene una función documentada de posponer avisos hasta llegar a casa. No se identifica un permiso claramente ajeno; `USE_FINGERPRINT` merece revisar su necesidad por estar obsoleto desde API 28 y tener la app minSdk 28. Los cuatro permisos peligrosos permanecen denegados, así como alarmas exactas; abrir la app no demuestra uso de todos los permisos. Los permisos normales concedidos no equivalen a todos los accesos especiales concedidos.
- **Superficie exportada:** son los tres componentes anteriores. `PreviewActivity` de tooling en release merece revisión; `ProfileInstallReceiver` exige DUMP y no está abierto a una app ordinaria. Se documenta superficie declarada, sin explotar terceros ni afirmar vulnerabilidades probadas.

APK y decodificados completos permanecen fuera de Git. El informe enlaza capturas propias, comandos y salidas seleccionadas.

## Estado de los entregables grupales (5.5)

| Entregable | Evidencia o estado |
| --- | --- |
| Proyecto Android que compila y se ejecuta | Fuentes y configuración versionadas; verificación local documentada por Esteban el 11/10/2026. Aceptado como base común |
| README con equipo, integrantes, dominio, descripción y cuatro requisitos | Completado y aceptado por el equipo; incluye la función de cámara y su justificación |
| Convenciones y justificación del `targetSdk` | Convenciones y justificación documentadas como base común |
| Responsables de los cuatro requisitos | Cuatro responsables y alcances acordados en 5.2; reparto inicial ajustable según el esfuerzo |
| Tabla de reconocimiento de una aplicación real completada | Realizada por David: MedTimer full 1.26.0 instalado, extraído y analizado; informe y evidencias enlazados |
| `.gitignore` correcto y ausencia de archivos sensibles en el histórico | Exclusiones revisadas; David documenta la inspección de los dos commits iniciales sin artefactos o claves observados. Revisión acotada, no garantía absoluta |
| Repositorio compartido con integrantes y profesorado | Repositorio público con acceso de lectura por URL; tres cuentas colaboradoras verificadas el 14/10/2026. Falta confirmar la cuenta del cuarto integrante y la comunicación del enlace al profesorado |

### Qué falta para cerrar la entrega grupal de P2.1

1. Revisar los cambios del README y el reconocimiento de David e integrarlos en `main` mediante pull request; la rama de trabajo reúne ambas partes.
2. Confirmar la cuenta y los permisos de colaboración del cuarto integrante.
3. Comunicar al profesorado el enlace del repositorio por el canal indicado.

La idea, el alcance, las convenciones y el reparto inicial ya están definidos. Las funcionalidades se desarrollarán en las prácticas posteriores; esta entrega documenta la planificación y conserva el proyecto base.

El reconocimiento conserva su procedencia explícita: se buscó MedTimer en Google Play y se instaló el APK del release oficial de GitHub; no se afirma que sea el artefacto distribuido por Play.

### Entrega y decisiones de implementación

La entrega grupal es el repositorio según el apartado 5.5 del [enunciado de P2.1 en Campus Virtual](https://informatica.cv.uma.es/mod/resource/view.php?id=31706), **antes de P2.2**. La documentación, el proyecto base y las evidencias del reconocimiento deben quedar integrados en `main` tras su revisión. El cuaderno de hasta cuatro páginas y el APK individuales pertenecen al apartado 6 y se preparan por separado; el estado de cada entrega debe confirmarlo su autor.

Las tecnologías concretas de autenticación y almacenamiento, el proveedor y contrato de la API de medicamentos y las librerías necesarias se elegirán al revisar las prácticas posteriores. P2.1 define sus funciones, responsables y organización; no exige tener implementados estos mecanismos. Antes de introducir dependencias, se comprobará su compatibilidad con Kotlin, Compose y la configuración del proyecto.
