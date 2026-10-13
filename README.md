# SaludApp — P2.1

Repositorio grupal de **Seguridad en Entornos Móviles**, Universidad de Málaga. Práctica **P2.1: El entorno de desarrollo y el artefacto de la aplicación**, apartados 5.1–5.5.

## Equipo y dominio

| Campo | Información |
| --- | --- |
| Identificación del equipo | Supergrupo 1, Grupo D |
| Nombre propio del equipo | JADE Salud |
| Integrantes | Angel Gallego Güeto; Jorge de Torres Suárez; David López Callejón; Esteban Sánchez Gámez |
| Dominio asignado | Salud — Historial médico personal |
| Nombre propuesto de la aplicación | SaludApp; **pendiente de ratificar** |
| Nombre actual del proyecto y de la aplicación | `AppSalud`, según `settings.gradle.kts` y `strings.xml` |
| Repositorio configurado como `origin` | [SEM_GRUPO_D](https://github.com/Sauvageduck24/SEM_GRUPO_D) |

## Descripción de la aplicación (5.1)

SaludApp permitirá a los usuarios autenticarse, consultar y almacenar su historial médico personal, con antecedentes, tratamientos e informes clínicos. Contará con almacenamiento local y comunicación con una API remota. Los datos considerados más sensibles serán los informes médicos y los antecedentes clínicos.

### Cuatro requisitos técnicos mínimos

- **Autenticación:** identificar al usuario antes de acceder a su historial médico; mecanismo concreto **pendiente de acordar**.
- **Almacenamiento local de datos sensibles:** conservar antecedentes clínicos e informes médicos en el dispositivo; tecnología concreta **pendiente de acordar**.
- **API remota:** realizar al menos una llamada para consultar o sincronizar información del historial; servicio, operación y datos intercambiados **pendientes de acordar**.
- **Permiso peligroso justificado:** selección **pendiente de acordar**; propuesta para valorar: `android.permission.CAMERA` para fotografiar informes en papel con la cámara integrada en la aplicación y adjuntarlos al historial.

El apartado 5.1 permite cambiar el permiso natural del dominio si se justifica. Aunque para Salud propone almacenamiento para informes, seleccionar documentos mediante Storage Access Framework no requiere permisos del sistema y, por sí solo, no cubriría el requisito de un permiso peligroso. `CAMERA` sí tiene nivel de protección `dangerous`; la propuesta anterior requiere una decisión del equipo. Referencias: [acceso a documentos](https://developer.android.com/training/data-storage/shared/documents-files) y [permiso CAMERA](https://developer.android.com/reference/android/Manifest.permission#CAMERA).

En P2.1 estos requisitos se describen para las prácticas posteriores. El proyecto conserva únicamente la pantalla de la plantilla de Android Studio (`Hello Android!`).

## Convenciones del equipo (5.2)

Los valores actuales se han leído del proyecto existente y se mantienen porque la compilación funciona. Su adopción como base común y las propuestas siguientes están **pendientes de ratificación por el equipo**.

| Decisión | Configuración actual o propuesta | Estado |
| --- | --- | --- |
| Paquete común | `es.uma.seguridad.salud`, tanto `namespace` como `applicationId` | Configurado; pendiente de ratificación |
| `minSdk` | `26` | Configurado; pendiente de ratificación de la versión mínima soportada |
| `targetSdk` | `37` | Configurado; justificación propuesta debajo, pendiente de ratificación |
| `compileSdk` | `release(37)` | Configuración existente conservada |
| Tecnología base | Kotlin, Jetpack Compose y Gradle Kotlin DSL | Proyecto existente; pendiente de aceptación como base común |
| Ramas y revisión | Propuesta: `main` como rama estable, ramas `feature/<tarea>` y `fix/<tarea>`, integración mediante pull request revisada por otro integrante y compilación correcta antes de integrar | **Pendiente de acordar**; la rama local actual es `main` |

**Justificación propuesta de `targetSdk`:** se mantiene API 37, disponible en el SDK local y comprobada en el emulador, para asumir las restricciones de comportamiento y seguridad de ese nivel y mantener el objetivo alineado con el entorno de compilación y pruebas; el equipo debe ratificar esta elección y justificación.

### Responsables

| Requisito mínimo | Persona responsable |
| --- | --- |
| Autenticación | **Pendiente de asignar** |
| Almacenamiento local de datos sensibles | **Pendiente de asignar** |
| API remota | **Pendiente de asignar** |
| Permiso peligroso y su justificación | **Pendiente de asignar** |

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

En Linux/macOS puede usarse `sh ./gradlew` en lugar de `.\gradlew.bat`. Si hay varios dispositivos, seleccionar el destino mediante `ANDROID_SERIAL` y `adb -s <serial>`.

### Verificación realizada

Comprobación local del **11 de octubre de 2026**:

- `.\gradlew.bat :app:clean :app:assembleDebug :app:installDebug --console=plain`: **BUILD SUCCESSFUL**, 38 tareas ejecutadas e instalación correcta en el emulador API 37.
- Lanzamiento de `es.uma.seguridad.salud/.MainActivity` con ADB: **Status: ok**.
- `adb shell dumpsys activity activities`: `MainActivity` figura como `ResumedActivity`.

### Archivos excluidos y acceso compartido

`.gitignore` excluye las carpetas `build` de todos los módulos, cachés de Gradle y Kotlin, configuración local del IDE, `local.properties`, `AGENTS.md`, artefactos APK/AAB, almacenes de claves, claves privadas y archivos habituales de secretos. El Gradle Wrapper, incluido su JAR, permanece versionable. `app/.gitignore` conserva además la exclusión de `/build`.

La preparación inicial declaró el índice local sin archivos generados ni de claves. David revalidó el remoto el 13/10/2026: `main` en `e35a522da4cf2f5f95674d771719edda5f540c5d`, con dos commits de Esteban. La revisión de los archivos de ambos commits no encontró APK, almacenes de claves, claves privadas o `local.properties` versionados. Esto no certifica ausencia absoluta de secretos. David dispone de permiso WRITE y publica el reconocimiento en una rama mediante PR. **Pendiente:** confirmar el acceso de los demás integrantes y del profesorado; no se fusiona esta PR ni se ratifican acuerdos del equipo.

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

| Entregable | Estado |
| --- | --- |
| Proyecto Android que compila y se ejecuta | **Verificado** localmente; pendiente de acuerdo como base común |
| README con equipo, integrantes, dominio, descripción y cuatro requisitos | **Preparado parcialmente**: falta el acuerdo sobre el permiso peligroso |
| Convenciones y justificación del `targetSdk` | **Documentadas con propuestas**; faltan ratificación, estrategia de ramas acordada y asignación de responsables |
| Tabla de reconocimiento de una aplicación real completada | **Completada por David**: MedTimer full 1.26.0 instalado, extraído y analizado; informe y evidencias enlazados |
| `.gitignore` correcto y ausencia de archivos sensibles en el histórico | **Exclusiones revisadas**; dos commits remotos inspeccionados sin artefactos o claves observados. Revisión acotada, no garantía absoluta. |
| Repositorio compartido con integrantes y profesorado | `origin` configurado; **accesos pendientes de confirmar** |

La entrega grupal es el repositorio según 5.5, antes de P2.2. El cuaderno y el APK individuales pertenecen al apartado 6: David ha construido, verificado y ejecutado su APK personal, y su entrega se prepara fuera de este repositorio. El estado de las entregas individuales de los demás integrantes debe confirmarlo cada autor; este README no lo acredita.
