# P3.1 · Aportación de Esteban: cámara e informes

Autor: Esteban Sánchez Gámez. Grupo D, JADE Salud. Fecha: 11/11/2026.

Ámbito tomado del reparto del README y de docs/ALCANCE.md: cámara, informes y agenda de citas; permisos, denegación y protección de fotografías/archivos. Esta aportación completa las comprobaciones de cámara de P2.3 y documenta el estado del dato en reposo de su ámbito para P3.1. No atribuye a Esteban la implementación de sesión, Room ni red realizada por otros integrantes.

## 1. Estado real

La aplicación actual solicita CAMERA para la futura fotografía de informes. Todavía no activa una captura, no genera imágenes de informes, no almacena adjuntos, no exporta fotografías ni implementa una agenda independiente. El listado muestra registros genéricos de salud. Pedir el permiso no equivale a tener una captura implementada ni a haber protegido fotografías.

No hay, por tanto, un archivo de informe existente que cifrar en esta versión. La protección actual del ámbito de Esteban consiste en controlar el permiso, no bloquear la consulta al denegarlo, mantener FLAG_SECURE heredado y no añadir almacenamiento o exposición de fotografías sin diseño. Las capturas generadas por las pruebas instrumentadas son evidencias del laboratorio, no informes guardados por la app.

La rama de Ángel feature/angel-p31-cifrado contiene el cifrado de la sesión y la puerta biométrica. Esta rama de Esteban parte de main y puede integrarse independientemente: no copia ese trabajo ni modifica sus archivos de sesión. No se afirma que el cifrado del token proteja automáticamente imágenes, SQLite o una sesión ya abierta.

## 2. Cambios de código de esta aportación

- Se extrae el permiso de PantallaLista a ui/PermisoCamaraInformes.kt. El listado y sus callbacks conservan el mismo contrato.
- La petición se realiza al pulsar el botón, no al arrancar. Se explica para qué se usará y que consultar registros no requiere cámara.
- Tras una denegación se mantiene el listado. Cuando Android deja de ofrecer el diálogo se muestra una acción explícita para abrir los ajustes de esta aplicación; no hay reintentos automáticos.
- Al volver al primer plano se consulta otra vez el permiso real. Se corrige el estado obsoleto que podía conservar el remember original tras cambiarlo en Ajustes.
- Si no hay cámara se informa y se omite la petición. La declaración de hardware opcional y FLAG_SECURE ya estaban en main y se conservan.
- Con permiso concedido se indica expresamente que la captura todavía no está disponible, para no presentar como terminada una función futura.

Los permisos son revocables; el valor mostrado en pantalla no autoriza por sí solo una futura operación de cámara. Cuando exista esa operación habrá que volver a comprobar CAMERA justo antes de abrir el sensor y tratar su revocación/error. La concesión de CAMERA no autoriza acceso a informes de otro usuario.

Se actualizaron sólo dependencias de pruebas: Espresso 3.7.0, AndroidX JUnit 1.3.0 y runner 1.7.0. La configuración anterior de Espresso usaba una llamada reflectiva que fallaba en Android 17/API 37 antes de ejecutar las pruebas. No se cambia minSdk, targetSdk ni las dependencias funcionales del proyecto.

## 3. Inventario del ámbito de Esteban

| Dato o canal | Estado actual | Control aplicado | Adversario y límite |
| --- | --- | --- | --- |
| Acceso al sensor | Sólo solicitud del permiso; sin captura | CAMERA en tiempo de ejecución, petición voluntaria y hardware opcional | Android controla el acceso; no implica cifrado ni autorización del historial |
| Decisión sobre CAMERA | Permiso del sistema y estado transitorio de interfaz | Consulta al entrar/volver a primer plano y callback del permiso | Evita presentar una concesión antigua; no sustituye comprobar antes de una operación real |
| Fotografía de informe | No se crea ni persiste | No se añade almacenamiento público, URI de adjunto ni permiso de almacenamiento | No se afirma que haya archivos cifrados; al introducir captura habrá que implementar su protección |
| Informes visibles en listado/detalle | Registros genéricos ya existentes | FLAG_SECURE heredado de P2.3 | Limita capturas/miniatura del sistema; no protege HTTP, SQLite, memoria ni una cámara externa |
| Citas de agenda | Sin modelo ni persistencia específica | No hay control de almacenamiento que atribuir todavía | Registrar una cita única exigiría definir recuperación y protección antes de guardar datos reales |

No se modifica globalmente la política de backup: también afecta a la sesión y a la base de datos y requiere coordinación del equipo. Las reglas actuales no deben describirse como una protección de informes. No se ha probado una copia/restauración de imágenes porque no se producen.

## 4. Apartado de diseño de datos en reposo para integrar en el documento común

**Control aplicado hoy:** no hay persistencia de fotografías; se conserva el permiso bajo control del usuario y la protección de la ventana. La base SQLite y el canal HTTP mantienen los límites ya documentados en el alcance. Esta sección sólo cubre el ámbito de Esteban, no sustituye el documento de diseño de todo el equipo.

**Propuesta de Esteban para cuando se implemente la captura, pendiente de acuerdo:** guardar el contenido en almacenamiento interno privado, separado por usuario, y cifrar contenido y metadatos sensibles con un modo autenticado y nonce nuevo por operación. No emplear rutas públicas ni nombres de archivo con diagnósticos o nombres de pacientes. No registrar bytes, rutas sensibles ni contenido clínico en logcat. Cualquier URI compartida debería tener un destinatario, alcance y duración explícitos, sin dar por implementado ahora un proveedor de archivos.

**Recuperación que debe decidirse antes de implementar:** un informe fotografiado puede ser una copia única, mientras que el token de sesión se vuelve a obtener por login. Reutilizar para informes la política de destruir la clave y descartar datos al cambiar la biometría podría hacer perder documentación única. Deben acordarse custodia de clave, autenticación, cierre/cambio de cuenta, copia de seguridad y recuperación antes de activar la persistencia. No se presenta esa propuesta como una decisión colectiva ni como un control ya aplicado.

**Trabajo futuro:** captura real, agenda, cifrado de sus nuevos archivos y política de recuperación quedan identificados como funcionalidades por desarrollar. Las partes grupales de P2.2 y P2.3 no exigen completar ese producto; piden modelo adaptado, inventario, aplicación base y permiso justificado. Tampoco se añade TLS ni otra medida de P3.2/P3.3 en esta aportación.

## 5. Comprobación y límites de la evidencia

laboratorio/probar-camara.ps1 compila el APK y su APK de pruebas, instala ambos y prepara sólo CAMERA de SaludApp. Ejecuta tres pruebas instrumentadas y repite la prueba de listado después de una revocación real de CAMERA fuera del proceso. Restaura la concesión y los flags user-set/user-fixed originales al terminar. Revocar puede terminar el proceso de la app: por eso se hace entre dos ejecuciones del runner.

Las pruebas montan PantallaLista en MainActivity con un registro ficticio local; no hacen login, no consultan backend y no verifican la API ni el acceso biométrico. Las interacciones con el diálogo de permisos y los Ajustes son reales. Conceder en Ajustes se simula mediante los privilegios del runner mientras la app está en segundo plano; al regresar se comprueba que la interfaz lee el nuevo permiso. El caso sin cámara se simula en el parámetro del componente, no se afirma haber retirado el hardware del emulador.

Se conserva FLAG_SECURE: se capturan directamente sólo los diálogos/ajustes del sistema; para los estados de la ventana protegida se guardan textos reales obtenidos de accesibilidad. El runner no guarda contraseñas ni tokens ni vuelca logcat completo. Cada ejecución identifica fecha, paquete, base Git, alcance y SHA-256 del APK probado.

Ejecutar desde PowerShell, con el emulador encendido y desbloqueado:

```powershell
.\laboratorio\probar-camara.ps1
```

La ejecución final pasó con tres pruebas, más una repetida después de revocar CAMERA. Los resultados, capturas y límites están en el [seguimiento de Esteban](../esteban/SEGUIMIENTO_PRACTICAS.md#resultados). También se comprobó compilación conjunta con la rama de cifrado de Ángel en una copia aislada. Esta prueba de componente no acredita que el servidor autorice correctamente un historial ni que se hayan cifrado informes inexistentes.

Referencias técnicas: [permisos en tiempo de ejecución](https://developer.android.com/training/permissions/requesting), [acceso a cámara e información sensible](https://developer.android.com/training/permissions/explaining-access) y [Espresso 3.7.0: corrección de InputManager](https://developer.android.com/jetpack/androidx/releases/test#espresso-3.7.0). Consultadas el 11/11/2026.
