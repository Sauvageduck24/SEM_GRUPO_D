# P3.1 · Verificación grupal de AppSalud

Base de código [9bbed58 de `main`](https://github.com/Sauvageduck24/SEM_GRUPO_D/commit/9bbed58682b1b869525f8a1fc961a42a51eefdc0), con los cambios de la rama de cierre de P3.1. Se usó el backend docente con los registros ficticios de `laboratorio/backend_salud.py` y una instancia aislada del emulador. La aplicación conserva `FLAG_SECURE`. Los estados de pantalla se verifican mediante los textos de accesibilidad.

## Resultados del 23 de noviembre de 2026

En Android 17 (API 37) funcionaron el acceso, la protección con huella, el listado y el detalle del registro 2. Tras forzar el cierre, la app mostró la puerta de desbloqueo. Al cancelar, se mantuvo la puerta con un aviso. Al reintentar con la huella, se abrió el listado. **Salir** devolvió al acceso y el siguiente arranque volvió a pedir las credenciales.

| Comprobación del paso 6 | Evidencia observada |
| --- | --- |
| Preferencias | `sesion.xml` tenía solo la entrada `datos`. El nonce decodificado medía 12 bytes y el cifrado con la etiqueta medía 40 bytes. Ningún valor de sesión comprobado apareció en claro. Después de salir, el mapa de preferencias quedó vacío. |
| SQLite | Se extrajeron `appbase.db`, `appbase.db-wal` y `appbase.db-shm`. SQLite reconoció la cabecera y devolvió dos filas, «Antecedente de prueba» y «Consulta de prueba», con título, contenido, fecha y propietario legibles. Tras el cierre de sesión, la consulta devolvió cero filas. |
| Logcat | El proceso registró `POST /api/login`, `GET /api/elementos` y `GET /api/elementos/2`, cada una con respuesta HTTP 200. El mensaje del repositorio fue `Login correcto`. La búsqueda en el registro del proceso dio cero coincidencias para la contraseña, el token de prueba, la cabecera `Authorization` y los títulos o textos de salud comprobados. |

Gradle completó `assembleDebug`, `lintDebug` y `testDebugUnitTest`. Lint registró 0 errores y 26 avisos. La prueba unitaria del repositorio pasó. El [resultado de la comprobación](evidencias/resultado.json) conserva la huella SHA-256 de la APK y los resultados sin guardar credenciales, texto cifrado ni volcados brutos.

## Reproducir el recorrido

1. Arrancar el backend y preparar un emulador con PIN y una huella de prueba según el [README](../../README.md). Los siguientes comandos son para PowerShell con el SDK instalado, el JDK 25 seleccionado y un único dispositivo ADB conectado.

   ```powershell
   .\gradlew.bat :app:assembleDebug :app:lintDebug :app:testDebugUnitTest -PlaboratorioAdbReverse=true
   adb reverse tcp:8080 tcp:8080
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n es.uma.seguridad.salud/.MainActivity
   ```

2. Iniciar sesión con la cuenta docente, pulsar **Confirmar** y autenticar con la huella. En el emulador se puede usar `adb emu finger touch 1` si se registró el dedo 1. Pulsar **Recargar**, abrir **Consulta de prueba** y confirmar que aparece el detalle 2. Volver al listado.
3. Con la sesión activa, comprobar los tres puntos del paso 6. Para leer las preferencias y obtener el registro del proceso actual se pueden usar estos comandos. Las salidas son para comparar en local y no se adjuntan íntegras.

   ```powershell
   adb shell run-as es.uma.seguridad.salud cat shared_prefs/sesion.xml
   $appProcess = (adb shell pidof es.uma.seguridad.salud).Trim()
   adb logcat -d --pid=$appProcess -v brief
   ```

   Copiar `databases/appbase.db` y sus archivos `-wal` y `-shm`, si están presentes, con `adb exec-out run-as es.uma.seguridad.salud cat ...`. Guardarlos juntos en un directorio temporal. En Windows usar una captura binaria, por ejemplo `subprocess.run` de Python con `capture_output=True` y `Path.write_bytes`, para evitar convertir bytes a texto. Consultar la copia con SQLite:

   ```sql
   SELECT id, titulo, contenido, fecha, propietario FROM elementos ORDER BY id;
   ```

4. Forzar el cierre del proceso con `adb shell am force-stop es.uma.seguridad.salud` y abrirlo de nuevo. Pulsar **Desbloquear**, cancelar y comprobar el aviso sin que aparezca el listado. Reintentar con la huella. Pulsar **Salir**, comprobar el retorno al acceso y volver a consultar las preferencias y el número de filas de la cuenta. Abrir la aplicación otra vez para comprobar que pide el acceso.

## Actualización desde P2.3

Con el proceso detenido se preparó un fichero de prueba `sesion.xml` con los dos campos que escribía P2.3, `token` y `usuario`, y valores ficticios. Se abrió la app y se leyó de nuevo el fichero desde la compilación depurable. El APK de `main` conservó ambos campos tras abrirse en **Acceso**. Tras la corrección, la misma comprobación dejó las preferencias vacías y mostró **Acceso**. La prueba reproduce el fichero de preferencias de P2.3 con datos de prueba.

Se repitió la comprobación con esos dos campos antiguos junto a un paquete `datos` recién generado. La aplicación borró los campos antiguos, conservó el mismo paquete cifrado y lo abrió tras la huella.

Las comprobaciones se limitan al emulador de laboratorio. La consulta de SQLite acredita el estado lógico de la tabla. No se probaron copias del sistema, borrado forense, revocación del token en el servidor ni respaldo de clave por hardware.
