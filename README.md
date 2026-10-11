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

La revisión del índice local no muestra archivos generados ni archivos de claves entre los archivos preparados para Git. En el momento de esta preparación no hay commits ni referencias de historial en el repositorio local; esto no verifica el contenido remoto. **Pendiente:** confirmar el acceso de todos los integrantes y del profesorado al repositorio y comprobar el histórico remoto antes de entregar. No se ha hecho commit ni push.

## Reconocimiento de una aplicación real (5.4)

**Pendiente de completar.** No se dispone de una selección ni de resultados del análisis de una aplicación real para el reconocimiento grupal. La comprobación de ejecución del proyecto propio no sustituye esta actividad.

| Datos del análisis | Valor |
| --- | --- |
| Aplicación elegida y versión | **Pendiente** |
| Nombre del paquete | **Pendiente** |
| Procedencia | **Pendiente**: Google Play, del mismo dominio; aplicación del sistema solo si no se consigue una del dominio |
| Dispositivo/emulador, versión de Android y fecha del análisis | **Pendiente** |
| Versiones de ADB, apktool, aapt2 y apksigner utilizadas | **Pendiente** |

Tabla requerida sobre el **artefacto base** de la aplicación elegida:

| Qué mirar | Con qué | Vuestra observación |
| --- | --- | --- |
| Permisos declarados | `aapt2 dump permissions base.apk` | **Pendiente de obtener** |
| Componentes exportados | `apktool d -o reconocimiento-local/decodificado reconocimiento-local/base.apk` y lectura del manifiesto | **Pendiente de obtener**: nombres, tipos y número total |
| Esquemas de firma | `apksigner verify --print-certs -v base.apk` | **Pendiente de obtener**: esquemas verificados y certificado |
| `minSdk` y `targetSdk` | `aapt2 dump badging base.apk` | **Pendiente de obtener** |
| Número de piezas instaladas | `adb shell pm path <paquete>` | **Pendiente de obtener**: número de rutas y sufijo de cada pieza |

Para completarla, partir de la aplicación instalada: localizar el paquete con `adb shell pm list packages`, consultar **todas** las rutas con `adb shell pm path` y descargar cada pieza con `adb pull`. Conservar los APK y decodificados en `reconocimiento-local/`, excluida de Git, y analizar el APK base con las herramientas de la tabla. En Windows, usar `aapt2.exe` y `apksigner.bat` desde `build-tools/<version>` del SDK, y ejecutar apktool con su lanzador o `java -jar <ruta-a-apktool.jar>`.

Observaciones para el cuaderno del equipo:

- ¿Hay algún permiso que no se pueda justificar por la función de la aplicación? **Pendiente de evaluar**.
- ¿Cuántos componentes tiene exportados y cuáles son? **Pendiente de evaluar**.

## Estado de los entregables grupales (5.5)

| Entregable | Estado |
| --- | --- |
| Proyecto Android que compila y se ejecuta | **Verificado** localmente; pendiente de acuerdo como base común |
| README con equipo, integrantes, dominio, descripción y cuatro requisitos | **Preparado parcialmente**: falta el acuerdo sobre el permiso peligroso |
| Convenciones y justificación del `targetSdk` | **Documentadas con propuestas**; faltan ratificación, estrategia de ramas acordada y asignación de responsables |
| Tabla de reconocimiento de una aplicación real completada | **Pendiente**; plantilla y procedimiento preparados |
| `.gitignore` correcto y ausencia de archivos sensibles en el histórico | **Exclusiones revisadas**; índice local sin archivos generados ni de claves, sin historial local; comprobación remota pendiente |
| Repositorio compartido con integrantes y profesorado | `origin` configurado; **accesos pendientes de confirmar** |

La entrega grupal es el repositorio según 5.5, antes de P2.2. El cuaderno individual y el APK individual pertenecen a la entrega individual del apartado 6, ya resuelta según las instrucciones del proyecto.
