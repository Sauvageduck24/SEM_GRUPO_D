# AppSalud — P2.1

Seguridad en Entornos Móviles · Universidad de Málaga.

Somos JADE Salud, el Grupo D del Supergrupo 1. El equipo está formado por Ángel Gallego Güeto, David López Callejón, Jorge de Torres Suárez y Esteban Sánchez Gámez. Nuestro dominio es Salud: historial médico personal.

## 5.1. Qué hará la aplicación

Queremos crear una carpeta y agenda personal para reunir antecedentes, tratamientos, citas e informes médicos y tenerlos a mano en las consultas. El usuario podrá gestionar esa información, fotografiar informes y buscar datos de medicamentos mediante una API, es decir, consultando un servicio externo. Los datos más sensibles serán los antecedentes y los informes, que guardaremos en el dispositivo para poder consultarlos sin conexión; la consulta de medicamentos necesitará internet.

Las citas las introducirá el usuario manualmente. La app no estará conectada con las agendas de centros sanitarios.

Cubriríamos los cuatro requisitos de la práctica de esta forma:

- Autenticación: iniciar y cerrar sesión y comprobar el acceso al historial.
- Almacenamiento local: guardar los datos médicos y las fotografías de informes.
- API remota: consultar información sobre medicamentos.
- Permiso peligroso: pedir `android.permission.CAMERA` cuando se quiera fotografiar un informe con la cámara integrada.

Elegimos CAMERA porque necesitamos la cámara para capturar informes. El enunciado permite cambiar el permiso sugerido si tiene sentido dentro del dominio. Si el usuario lo deniega, podrá seguir utilizando las otras funciones.

Como ampliaciones opcionales, valoraremos usar IA para organizar y consultar informes, revisando sus resultados, y añadir recordatorios de citas y medicación con horarios confirmados por el usuario. Primero completaremos la versión básica.

## 5.2. Cómo nos organizaremos

Mantendremos Kotlin, Jetpack Compose y Gradle Kotlin DSL, que son las tecnologías del proyecto base.

- Paquete común: `es.uma.seguridad.salud`.
- Versión mínima: `minSdk 26` (Android 8.0).
- Versión objetivo: `targetSdk 37`.
- Versión de compilación: `compileSdk 37`.

Mantenemos targetSdk 37 para asumir las restricciones de seguridad de esa versión y conservar la configuración que Esteban ya ha compilado y probado.

Organizaremos el código con una Clean Architecture sencilla: separaremos las pantallas, la lógica de la aplicación y el acceso a los datos para poder modificar una parte sin mezclarla con las demás. Usaremos nombres claros, funciones con una responsabilidad y componentes reutilizables.

Trabajaremos en ramas `feature/<tarea>` y `fix/<tarea>`. Antes de incorporar un cambio a `main`, otro compañero revisará la pull request, donde se propone ese cambio. Si modificamos código o configuración, compilaremos y comprobaremos el comportamiento afectado.

### Reparto del trabajo

| Persona | Qué hará | Qué trabajará de seguridad |
| --- | --- | --- |
| Ángel | Login, sesión, acceso al historial y navegación | Identificar al usuario, comprobar qué puede consultar y proteger la sesión y las credenciales |
| David | Base de almacenamiento, resumen de salud y antecedentes | Separar los datos de cada usuario y estudiar su protección, cifrado y copias de seguridad según la práctica |
| Jorge | Consulta de medicamentos mediante API y gestión de tratamientos | Proteger la comunicación con HTTPS, validar las respuestas y revisar qué datos se envían |
| Esteban | Cámara, informes y agenda de citas | Pedir los permisos necesarios, gestionar su denegación y proteger el acceso a fotografías y archivos |

Cada uno hará las pantallas, la lógica y el guardado de datos de su parte usando la base común. Revisaremos la carga antes de programar y ajustaremos tareas si un bloque requiere más tiempo.

### Cómo comprobaremos lo aprendido

Cada integrante explicará qué riesgo ha considerado, qué decisión ha tomado y qué resultado ha obtenido al comprobarla. Por ejemplo:

- Ángel comprobará el acceso sin sesión y después de cerrarla.
- David comprobará que una cuenta no pueda consultar el historial de otra y cómo quedan guardados los datos.
- Jorge comprobará los errores de conexión, las respuestas inválidas y la seguridad de la comunicación.
- Esteban comprobará qué ocurre al conceder, denegar o revocar la cámara y quién puede acceder a las imágenes.

Guardaremos los pasos y resultados de las pruebas con su autor y el cambio correspondiente. Utilizaremos datos de prueba y señalaremos lo que cada comprobación demuestra y sus límites. Todos revisaremos trabajo de otro compañero y participaremos en la auditoría del Bloque IV.

Estas comprobaciones están planificadas para las siguientes prácticas. Todavía no hemos implementado esos controles y los concretaremos con sus enunciados.

## 5.3. Proyecto base y repositorio

En P2.1 mantenemos la pantalla de ejemplo `Hello Android!`. Esteban documentó que el proyecto compilaba, se instalaba y se ejecutaba correctamente el 11/10/2026.

Para compilar necesitamos SDK API 37 y JDK 25. Desde la raíz del proyecto:

- macOS/Linux: `sh ./gradlew :app:assembleDebug`.
- Windows: `.\gradlew.bat :app:assembleDebug`.

El `.gitignore` excluye archivos generados, configuración local, APK y claves. David revisó los dos commits iniciales sin observar archivos sensibles. El repositorio es público y los cuatro integrantes tienen cuentas con permisos de colaboración; el profesorado puede consultar su contenido mediante el enlace.

## 5.4. Reconocimiento de una aplicación real

David analizó MedTimer full 1.26.0, del dominio Salud, el 13/10/2026 en un emulador Android 14/API 34. Su paquete es `com.futsch1.medtimer`.

Consultó su ficha de Google Play e instaló el APK del GitHub oficial. El análisis corresponde a ese archivo, sin afirmar que sea idéntico al distribuido por Play.

| Qué se analizó | Herramienta | Resultado |
| --- | --- | --- |
| Permisos | `aapt2 dump permissions` | 14 solicitudes; listado y justificación en el informe |
| Componentes exportados | apktool y manifiesto | 3 de 17: MainActivity, PreviewActivity y ProfileInstallReceiver; este último protegido por DUMP |
| Firma | `apksigner verify --print-certs -v` | Verifica v3; firmante Florian Fetz |
| Versiones de Android | `aapt2 dump badging` | minSdk 28 y targetSdk 36 |
| Piezas instaladas | `adb shell pm path` | Una: `base.apk`, sin splits |

En el reconocimiento no se identificó un permiso claramente ajeno a las funciones de la app. Se señaló que conviene revisar USE_FINGERPRINT, que está obsoleto, y la presencia de PreviewActivity en la versión publicada. Estas observaciones no demuestran por sí solas una vulnerabilidad.

Los comandos, resultados y capturas están en el [informe de reconocimiento](docs/p21/reconocimiento-david.md).
