# Documento de alcance · JADE Salud

Bloque II · Grupo D del Supergrupo 1 · Ángel Gallego Güeto, David López Callejón, Jorge de Torres Suárez y Esteban Sánchez Gámez

## 1. La aplicación

AppSalud es un historial médico personal que reúne antecedentes, tratamientos, citas e informes para que el paciente los tenga a mano cuando va a una consulta, y está pensada sobre todo para personas con tratamientos largos o que acuden a varios especialistas y pierden la pista de lo que les ha dicho cada uno. La versión actual permite iniciar sesión, ver el listado de registros de salud, consultarlos sin conexión gracias a una copia guardada en el móvil, abrir el detalle de cada registro y pedir el permiso de cámara para fotografiar informes, aunque todavía no guarda las fotos. Las citas las introduce el propio usuario y la app no está conectada con ningún centro sanitario.

## 2. Datos que maneja

| Dato | Dónde se guarda hoy | Quién puede leerlo hoy | Cómo se protegerá |
| --- | --- | --- | --- |
| Token de sesión | En claro en `shared_prefs/sesion.xml` y también en logcat | Cualquiera con adb y la depuración activada | Bloque III |
| Usuario y contraseña | La contraseña no se guarda, pero viaja por HTTP y aparece en logcat por el interceptor BODY | Quien vea el tráfico de red o lea logcat | Bloque III |
| Registros de salud | En claro en Room, en `appbase.db` y sus ficheros `-wal` y `-shm` | Cualquiera con adb y la app depurable, y además cualquier usuario con sesión puede pedir al servidor registros de otro | Bloque III |
| Perfil (nombre, correo y DNI) | En el servidor, y si se consulta pasa por HTTP y logcat | Quien vea el tráfico o lea logcat | Bloque III |
| Fotos de informes | Todavía no se guardan, solo se pide el permiso | Por definir cuando se implemente | Bloque III |

El detalle de cada dato, con las rutas exactas y los matices sobre quién puede leerlo, está en el [inventario de datos sensibles de la P2.2](p22/INVENTARIO_DATOS.md).

## 3. Superficie expuesta

| Vía de entrada | Cómo se invoca hoy | Qué comprueba hoy | Se trata en |
| --- | --- | --- | --- |
| Activity principal exportada | `adb shell am start -n es.uma.seguridad.salud/.MainActivity` | Nada, está exportada por el filtro del lanzador y abre la app sin pedir nada | Sesión 3.4 · P3.3 |
| Enlace profundo `appsegura://detalle/{id}` | Enlace web, mensaje, otra app o adb | Nada en la interfaz, la navegación solo exige que el identificador sea un entero, y los datos solo llegan si hay una sesión guardada porque lo comprueba el repositorio | Sesión 3.4 · P3.3 |
| Llamadas a la API | curl con un token válido | Solo que el token exista, sin mirar de quién es el registro, así que ana puede leer el registro 3, que es de luis, y todo viaja por HTTP | Sesión 3.5 · P3.2 (el canal) · Bloque IV (la autorización) |
| Almacenamiento local | adb run-as | Nada, las preferencias y la base de datos están en claro | Sesión 3.3 · P3.1 |
| Registros | adb logcat | Nada, la contraseña, el token y los registros salen por el interceptor BODY y por el `Log.d` del inicio de sesión | Sesión 3.6 · P3.3 |
| Vista de recientes | Botón del sistema | FLAG_SECURE deja en blanco la miniatura guardada | Sesión 3.6 · P3.3 |
| Permiso de cámara | Botón «Permitir cámara para informes» | Lo concede el usuario y, si lo deniega, el resto de la app sigue funcionando | Sesión 3.3 · P3.1 (cuando se guarden las fotos) |
| Actividades de depuración | Intent explícito | Nada, las añaden las librerías de pruebas y de previsualización solo en la versión debug | Sesión 3.4 · P3.3 |
| Copias de seguridad | Copia del sistema Android | Con `allowBackup=true` los datos pueden salir en una copia según las reglas de la plantilla | Sesión 3.3 · P3.1 |

## 4. Modelo de amenazas preliminar

- Una persona cercana, como la pareja o un compañero de trabajo, coge el móvil desbloqueado y quiere ver el historial, y lo tiene fácil porque la sesión se queda guardada y basta con abrir la app.
- Un usuario de la propia app siente curiosidad por los registros de otra persona, y le basta con cambiar el número del registro en la petición al servidor, porque el servidor no comprueba a quién pertenece.
- Una web o una app maliciosa abre el enlace `appsegura://detalle/{id}` para llevar al usuario a una pantalla concreta o para ir probando números de registro.
- Alguien conectado a la misma wifi pública que el usuario lee la contraseña, el token y los registros de salud, porque todo viaja sin cifrar.

## 5. Reparto de trabajo

| Requisito técnico mínimo | Responsable | Qué trabajará de seguridad |
| --- | --- | --- |
| Autenticación | Ángel | Identificar al usuario, comprobar qué puede consultar y proteger la sesión y las credenciales |
| Almacenamiento local | David | Separar los datos de cada usuario y protegerlos en el dispositivo y en las copias de seguridad |
| API remota | Jorge | Proteger la comunicación con HTTPS, validar las respuestas y revisar qué datos se envían |
| Permiso peligroso (cámara) | Esteban | Pedir el permiso cuando haga falta, gestionar su denegación y proteger el acceso a las fotografías |
