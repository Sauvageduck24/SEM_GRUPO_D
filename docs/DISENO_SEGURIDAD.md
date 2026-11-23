# Diseño de seguridad · JADE Salud

Grupo D del Supergrupo 1 · Seguridad en Entornos Móviles · Bloque III

## 1. La aplicación y su modelo de amenazas

AppSalud permite iniciar sesión, consultar un listado de registros de salud y abrir su detalle. Usa una caché de Room para el listado cuando falla la red. El detalle se consulta al backend y puede abrirse mediante un enlace profundo. El permiso de cámara prepara una futura función de informes. El laboratorio usa datos ficticios. El punto de partida es el [alcance de P2.3](ALCANCE.md).

| Adversario y condición | Activo en riesgo | Control aplicado o pendiente |
| --- | --- | --- |
| Persona con acceso al móvil desbloqueado | Sesión y registros en pantalla | La huella protege la apertura de la sesión persistida. Si el proceso conserva una sesión abierta, volver desde segundo plano puede mostrar los datos. |
| Lector con acceso a archivos de la app, como ADB autorizado con un APK debug o un proceso privilegiado | Preferencias y caché | El XML copiado requiere la clave y una operación autorizada para abrirse. La caché SQLite sigue legible. Un proceso comprometido puede leer datos en memoria mientras se usan. |
| Observador del tráfico entre el dispositivo y el backend | Contraseña, token y registros | HTTPS valida la cadena y el nombre del servidor. La política de publicación usa pines para el host del backend. Un observador puede seguir viendo metadatos de conexión. |
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

| Dato o canal | Antes de P3.1 | Después de P3.1 | Exposición al cerrar P3.1 |
| --- | --- | --- | --- |
| Token y usuario de sesión | Campos en claro en las preferencias de P2.3. El token también aparecía en logcat. | Un único campo AES-GCM con clave ligada a la huella. | Memoria durante el uso, HTTP y servidor. El usuario también figura como propietario en la caché. |
| Registros de salud | SQLite legible y cuerpos HTTP en logcat. | SQLite sigue legible. Se omiten los cuerpos en logcat. | Caché, WAL, copias potenciales, memoria, HTTP y backend. |
| Contraseña | En el cuerpo del login y en logcat. Sin persistencia en el cliente. | El cliente sigue sin persistirla. El cuerpo se omite de logcat. | Formulario en memoria, HTTP y backend docente. |
| Registros de la aplicación | OkHttp en `BODY` y objeto de respuesta de login en un mensaje del repositorio. | OkHttp en `BASIC` y mensaje de éxito sin el objeto. | Método, URL, código, tiempo y tamaño cuando se conoce. |
| Fotos de informes y citas | Sin archivos ni agenda independiente. | Sin archivos ni agenda independiente. | El texto de prueba visible como registro genérico tiene la exposición de la caché. |

El [inventario](p22/INVENTARIO_DATOS.md) detalla quién puede acceder a cada dato. La [verificación de P3.1](p31/VERIFICACION.md) registra la versión probada y los resultados de preferencias, SQLite y logcat.

## 3. Datos en tránsito · P3.2

### Política de red

El manifiesto enlaza `network_security_config.xml`. La política generada para cada variante prohíbe el tráfico en claro y parte de las CA del sistema. Se aplica una regla explícita a loopback porque Android 17 admite HTTP local si falta esa regla. Retrofit usa una URL HTTPS y el cliente desactiva las redirecciones automáticas para mantener las peticiones en el origen configurado. Se conserva `BASIC` en logcat. [Configuración de seguridad de red de Android](https://developer.android.com/privacy-and-security/security-config#localhost-configuration)

| Variante | Anclas y anclaje |
| --- | --- |
| `debug` | `debug-overrides` añade la CA docente cuando se indica `p32LabCa` y el almacén de CA del usuario. Esas anclas permiten depurar el laboratorio. Android omite el anclaje de cadenas que terminan en esas anclas. |
| `verificacion` | APK no depurable, con identidad propia para pruebas locales. Acepta la CA de prueba solo para el host de laboratorio. Tiene un `pin-set` activo y carece de `debug-overrides`. |
| `release` | Solo CA del sistema, sin CA docente ni almacén del usuario. Con un backend configurado, valida la cadena, el host exacto y dos pines SPKI SHA-256 del servidor, actual y reserva, con fecha de caducidad. La compilación sin un servidor configurado apunta a un dominio reservado. |

### Decisión propuesta para producción

Aplicaríamos anclaje a un único backend propio porque por el canal viajan credenciales y datos de salud, y una CA de confianza que emita un certificado indebido podría exponerlos. Lo activaríamos cuando el equipo responsable del servidor controle las claves y pueda desplegar la clave de reserva en una renovación o ante un incidente. El anclaje puede dejar sin acceso a usuarios que actualizan despacio. Antes de un despliegue real hacen falta métricas de adopción, un plan de compatibilidad con versiones antiguas y capacidad para publicar una actualización de emergencia. Una CA pública válida y TLS siguen siendo requisitos.

### Rotación del certificado o la clave

1. El responsable del backend avisa a quien publica AppSalud al menos 60 días antes de una renovación que cambie la clave o del vencimiento de los pines, lo que ocurra primero. Comparte el host, la fecha prevista y los hashes SPKI por el canal del equipo. Ambos confirman en la tarea de despliegue qué claves aceptan las versiones soportadas.
2. Si se usa la reserva ya anclada por esas versiones, se prueba en preproducción y se sustituye el certificado. Si hace falta una tercera clave, se publica antes una versión que acepte la clave actual y la siguiente. Se mantiene operativa una clave compartida durante el despliegue salvo que esté comprometida.
3. Objetivo propuesto de despliegue: 95 % de versiones activas compatibles con el nuevo pin y comprobaciones sintéticas correctas durante 48 horas. Las versiones restantes deben poder actualizarse. Si ya no admiten una clave válida, fallarán al conectar y necesitarán una versión nueva. El equipo informará por su canal de soporte.
4. Después del cambio, el responsable comprueba acceso, listado y detalle con la versión nueva y la versión anterior soportada. Vigila los fallos de TLS y de conexión. Si hay un fallo, vuelve a un certificado y a una clave aún válidos y anclados en ambos grupos, siempre que esa clave siga siendo segura.
5. Superada la fecha de `expiration`, Android deja de exigir los pines del conjunto. La conexión vuelve a depender de la cadena de confianza del sistema y del nombre del servidor. El responsable renueva los pines antes de la fecha para evitar perder esa restricción.

Los plazos y umbrales son objetivos propuestos. El proyecto docente no tiene una base de usuarios ni las métricas de un servicio publicado.

### Comparación antes y después

| Aspecto | P3.1 | P3.2 | Exposición que queda |
| --- | --- | --- | --- |
| Contraseña, token y registros en red | Cuerpos y cabeceras por HTTP. | HTTPS exige cadena y host válidos. La política de publicación configurada añade el anclaje. | Datos en memoria en ambos extremos, autorización por registro, revocación y caducidad del token. |
| Confianza en el servidor | Sin comprobación de certificado por HTTP. | Anclas del sistema en publicación. Las anclas añadidas se limitan a las variantes de laboratorio y prueba. | La confianza de depuración es más amplia. Un proceso o dispositivo comprometido puede alterar la app u observar los datos antes de TLS. |
| Metadatos | Un observador podía leer método, URL y contenido. | TLS oculta las rutas, las cabeceras y los cuerpos al observador de red. | IP, tiempos, tamaños aproximados y, según la conexión, el nombre del destino. El cliente sigue registrando las rutas en `BASIC`. |
| Caché y copias | SQLite legible y política de backup de plantilla. | Continúa la política de almacenamiento de P3.1. | Acceso privilegiado al dispositivo y copias potenciales. |

La [verificación de P3.2](p32/VERIFICACION.md) documenta los certificados de prueba, la aceptación de la reserva y el rechazo de una clave distinta firmada por la misma CA.

## 4. Superficie y endurecimiento · P3.3

Pendiente revisar los componentes exportados, enlaces profundos, permisos, registros de compilaciones no depurables y medidas de resiliencia. El punto de partida incluye la Activity exportada, `CAMERA`, `FLAG_SECURE` y los registros `BASIC`.

## 5. Riesgos asumidos

| Riesgo | Decisión al cerrar P3.2 |
| --- | --- |
| Caché legible y posibles copias | Se mantiene para el listado sin conexión con datos ficticios. Cifrar Room es una ampliación opcional. Queda pendiente acordar la política de copias. |
| Sesión activa en memoria | El cliente necesita el token para llamar a la API. La huella se pide al guardar o abrir la sesión persistida. Volver del segundo plano con el proceso vivo no inicia por sí solo un desbloqueo. |
| Autorización y revocación | TLS protege el canal. El backend docente conserva la autorización incompleta por registro y la falta de revocación en el cierre local. |
| Caducidad o rotación del anclaje | La fecha evita un bloqueo permanente por pines obsoletos y puede reducir la protección a la confianza normal de Android. La rotación requiere una clave de respaldo válida y la coordinación descrita en el apartado 3. |
| Metadatos en logcat | `BASIC` permite comprobar el laboratorio y reduce lo que se registra. La política para las compilaciones de publicación se revisará en P3.3. |
