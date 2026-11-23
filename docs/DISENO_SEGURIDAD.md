# Diseño de seguridad · JADE Salud

Grupo D del Supergrupo 1 · Seguridad en Entornos Móviles · Bloque III

## 1. La aplicación y su modelo de amenazas

AppSalud permite iniciar sesión, consultar un listado de registros de salud y abrir su detalle. Usa una caché de Room para el listado cuando falla la red. El detalle se consulta al backend y puede abrirse mediante un enlace profundo. El permiso de cámara prepara una futura función de informes. El laboratorio usa datos ficticios. El punto de partida es el [alcance de P2.3](ALCANCE.md).

| Adversario y condición | Activo en riesgo | Control aplicado o pendiente |
| --- | --- | --- |
| Persona con acceso al móvil desbloqueado | Sesión y registros en pantalla | La huella protege la apertura de la sesión persistida. Si el proceso conserva una sesión abierta, volver desde segundo plano puede mostrar los datos. |
| Lector con acceso a archivos de la app, como ADB autorizado con un APK debug o un proceso privilegiado | Preferencias y caché | El XML copiado requiere la clave y una operación autorizada para abrirse. La caché SQLite sigue legible. Un proceso comprometido puede leer datos en memoria mientras se usan. |
| Observador del tráfico entre el dispositivo y el backend | Contraseña, token y registros | El backend docente todavía usa HTTP. El canal se aborda en P3.2. |
| App externa que abre un enlace o usuario del servidor con sesión válida | Navegación y registros de otras cuentas | La puerta gobierna la sesión local. Se revisarán la Activity exportada y los enlaces en P3.3. La comprobación del propietario de cada registro corresponde al backend. |

## 2. Datos en reposo

### Sesión y clave

`SesionLocal` guarda el token y el usuario juntos en `shared_prefs/sesion.xml`, en el campo `datos`. `Cripto` usa `AES/GCM/NoPadding` con una clave de 256 bits generada mediante Android Keystore bajo el alias `clave_datos`. La aplicación obtiene una referencia a la clave. El `Cipher` genera un nonce nuevo al cifrar. El XML guarda el nonce y el cifrado con su etiqueta de autenticación GCM de 128 bits, ambos codificados en Base64.

Keystore exige autenticación por operación. La clave se configura para invalidarse cuando se registra una nueva huella. `BiometricPrompt` recibe el `Cipher` como `CryptoObject`. El guardado y el desbloqueo usan el cifrador devuelto por el diálogo tras la autenticación. El `VistaModelo` comparte una instancia de sesión entre la puerta y el repositorio.

Tras un login válido, el token queda pendiente en memoria hasta confirmar con la huella. Si se cancela, la puerta permite reintentar y no muestra el listado. Tras terminar el proceso, la sesión persistida vuelve a estar bloqueada. Si la clave se invalida o el paquete no se puede recuperar, la app descarta la sesión y vuelve a pedir el acceso. Esto es aceptable para un token que el servidor puede emitir de nuevo. La contraseña del formulario no se persiste. La prueba en emulador no acredita respaldo de la clave por hardware.

Al abrir la aplicación tras una actualización desde P2.3, se eliminan los campos de sesión antiguos en claro. Una cuenta que solo tenía la sesión antigua vuelve a iniciar sesión y confirma el nuevo guardado con huella. Si ya hay un paquete cifrado, se conserva y sigue bloqueado hasta que se autentique el usuario.

### Caché, archivos y copias de seguridad

Room guarda los registros bajo la cuenta que descargó el listado. Cerrar sesión borra las filas de esa cuenta y vacía las preferencias. Esta operación no garantiza el borrado forense de restos ni revoca el token remoto. Descartar una sesión irrecuperable actúa sobre la sesión, por lo que la caché puede permanecer. Se conserva la política de copia de la plantilla. Su restauración está sin comprobar.

La app solicita permiso de cámara. Todavía no crea fotos de informes, adjuntos ni una agenda independiente. Cuando se añada la captura habrá que decidir dónde se guardan los archivos, a qué cuenta pertenecen y cómo se recuperan. Una foto puede ser la única copia de un informe y requiere una política distinta de la de un token que el servidor puede volver a emitir. Este análisis de fotos y citas parte de la [aportación de Esteban en la PR #7](https://github.com/Sauvageduck24/SEM_GRUPO_D/pull/7).

### Comparación antes y después de P3.1

| Dato o canal | Antes de P3.1 | Después de P3.1 | Exposición que queda |
| --- | --- | --- | --- |
| Token y usuario de sesión | Campos en claro en las preferencias de P2.3. El token también aparecía en logcat. | Un único campo AES-GCM con clave ligada a la huella. | Memoria durante el uso, HTTP y servidor. El usuario también figura como propietario en la caché. |
| Registros de salud | SQLite legible y cuerpos HTTP en logcat. | SQLite sigue legible. Se omiten los cuerpos en logcat. | Caché, WAL, copias potenciales, memoria, HTTP y backend. |
| Contraseña | En el cuerpo del login y en logcat. Sin persistencia en el cliente. | El cliente sigue sin persistirla. El cuerpo se omite de logcat. | Formulario en memoria, HTTP y backend docente. |
| Registros de la aplicación | OkHttp en `BODY` y objeto de respuesta de login en un mensaje del repositorio. | OkHttp en `BASIC` y mensaje de éxito sin el objeto. | Método, URL, código, tiempo y tamaño cuando se conoce. |
| Fotos de informes y citas | Sin archivos ni agenda independiente. | Sin archivos ni agenda independiente. | El texto de prueba visible como registro genérico tiene la exposición de la caché. |

El [inventario](p22/INVENTARIO_DATOS.md) detalla quién puede acceder a cada dato. La [verificación de P3.1](p31/VERIFICACION.md) registra la versión probada y los resultados de preferencias, SQLite y logcat.

## 3. Datos en tránsito · P3.2

Pendiente documentar la configuración de red, el establecimiento de TLS, el anclaje y la gestión de certificados. La protección del canal y la autorización de cada recurso del servidor se comprobarán por separado.

## 4. Superficie y endurecimiento · P3.3

Pendiente revisar los componentes exportados, enlaces profundos, permisos, registros de compilaciones no depurables y medidas de resiliencia. El punto de partida incluye la Activity exportada, `CAMERA`, `FLAG_SECURE` y los registros `BASIC`.

## 5. Riesgos asumidos

| Riesgo | Decisión en P3.1 |
| --- | --- |
| Caché legible y posibles copias | Se mantiene para el listado sin conexión con datos ficticios. Cifrar Room es una ampliación opcional. Queda pendiente acordar la política de copias. |
| Sesión activa en memoria | El cliente necesita el token para llamar a la API. La huella se pide al guardar o abrir la sesión persistida. Volver del segundo plano con el proceso vivo no inicia por sí solo un desbloqueo. |
| Transporte, autorización y revocación | El backend docente conserva HTTP, la autorización incompleta por registro y la falta de revocación en el cierre local. Son riesgos de la versión de laboratorio. |
| Metadatos en logcat | `BASIC` permite comprobar el laboratorio y reduce lo que se registra. La política para las compilaciones de publicación se revisará en P3.3. |
