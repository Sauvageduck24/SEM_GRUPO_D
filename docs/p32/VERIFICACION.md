# P3.2 · Verificación grupal de AppSalud

Antes de cambiar el código se comprobó de nuevo P3.1 en Android 17 (API 37), sobre [fc641da de la PR #8](https://github.com/Sauvageduck24/SEM_GRUPO_D/pull/8/commits/fc641dab75efb7bee70fcac17e8e3d65575ad96f). El APK coincidía con el SHA-256 de la [evidencia de P3.1](../p31/evidencias/resultado.json). Pasaron el acceso, la huella, el listado, el detalle, la reapertura, el cierre y las tres comprobaciones de almacenamiento y registros.

Las pruebas finales de P3.2 parten de [631d55e en `main`](https://github.com/Sauvageduck24/SEM_GRUPO_D/commit/631d55e74d3146be20e89970c6222f18be7cc7c4), que integra las PR #7 y #8. La base incluye la mejora de cámara de Esteban.

## Resultados en Android 17

Una sonda de instrumentación ejecutó las peticiones a `/api/salud` desde el proceso de cada APK. El mismo puerto del emulador se conectó al servidor que presentaba cada hoja. Antes de comparar, las tres hojas devolvieron HTTP 200 por TLS 1.3 con un cliente que validaba la CA y el host del laboratorio.

| APK | Hoja presentada | Resultado HTTPS | Resultado HTTP en loopback |
| --- | --- | --- | --- |
| `debug` | Actual | HTTP 200 | Bloqueado |
| `verificacion` | Actual | HTTP 200 | Bloqueado |
| `verificacion` | Reserva | HTTP 200 | Bloqueado |
| `verificacion` | Tercera clave, con la misma CA y SAN | `Pin verification failed` | Bloqueado |
| `release` | Actual, firmada por la CA docente | `Trust anchor for certification path not found` | Bloqueado |

Las variantes `verificacion` y `release` se ejecutaron con `debuggable=false`. La primera valida el anclaje con una CA de prueba limitada al host del backend. La segunda se instaló firmando una copia de laboratorio. Usa la política de publicación, con anclas del sistema y sin la CA docente ni las CA de usuario. La prueba negativa sustituye de forma controlada la identidad TLS del servidor.

Con la hoja actual, los APK `debug` y `verificacion` completaron desde la interfaz el acceso, la confirmación con huella, el listado, el detalle y el cierre de sesión. Las tres llamadas devolvieron HTTP 200. También se ejecutó el cliente Retrofit de la propia app con la hoja de reserva. Devolvió el acceso, los dos registros ficticios y el detalle 2.

En el mismo APK de depuración pasaron las tres pruebas instrumentadas de cámara de Esteban y la repetición después de revocar `CAMERA`. Estas pruebas usan un listado ficticio local.

El ensayo inicial detectó que Android 17 permitía HTTP en `127.0.0.1` aun con el bloqueo general. Se añadió la política explícita de loopback y se repitieron las peticiones en los APK reconstruidos. La compilación también rechazó una URL HTTP y un backend de publicación configurado sin pines. Pasaron las tres compilaciones, la prueba unitaria existente y lint, con 0 errores y 26 avisos.

La inspección del XML y de la tabla de recursos de `release` confirmó dos pines para el host de prueba, bloqueo de HTTP, CA del sistema y ausencia de la CA de laboratorio, de `debug-overrides` y del almacén del usuario. Los hashes y resultados están en [las evidencias](evidencias/resultado.json).

## Preparar las pruebas de P3.2

1. Crear la CA y las dos hojas de prueba con `generar_certificado.sh` del material docente. Usar el SHA-256 de la clave pública (SPKI) que imprime el script para cada hoja. La CA debe corresponder al servidor de pruebas y el SAN de las hojas debe incluir `127.0.0.1`.
2. Arrancar el [backend por HTTPS](../../README.md#compilar-y-ejecutar) con la hoja actual. Para probar una rotación, sustituir `CERT` y `CLAVE` por la pareja de reserva y reiniciar el backend en el mismo puerto. Para la prueba negativa, usar una tercera hoja válida para el mismo host y firmada por la misma CA, con una clave distinta de las dos ancladas.
3. Compilar la variante de verificación. Hay que adaptar las rutas, huellas y fecha del ejemplo. `verificacion` usa el identificador `es.uma.seguridad.salud.verificacion` y exige los tres parámetros del anclaje.

   ~~~powershell
   $ca = 'C:\ruta\ca.pem'
   $pinActual = '<SPKI de la hoja actual en Base64>'
   $pinReserva = '<SPKI de la hoja de reserva en Base64>'
   $caducidad = '<AAAA-MM-DD>'
   .\gradlew.bat :app:assembleVerificacion -Pp32Verificacion=true "-Pp32LabCa=$ca" "-Pp32LabPinActual=$pinActual" "-Pp32LabPinReserva=$pinReserva" "-Pp32LabPinCaducidad=$caducidad"
   adb reverse tcp:8443 tcp:8443
   adb install -r app/build/outputs/apk/verificacion/app-verificacion.apk
   adb shell am start -n es.uma.seguridad.salud.verificacion/es.uma.seguridad.salud.MainActivity
   ~~~

4. Comprobar acceso, huella, listado y detalle con la hoja activa. Cerrar sesión antes de repetir con la reserva. Volver a probar con la tercera hoja. Debe fallar la conexión TLS, sin que se registre una petición HTTP de acceso en el backend. Usar una hoja válida y firmada por la misma CA permite distinguir el rechazo por pin de un error de confianza o de nombre.

## Inspeccionar la variante de publicación

Compilar `release` con la URL HTTPS de prueba y los valores SPKI y de caducidad preparados en el paso 3. Inspeccionar la APK con `aapt2` del SDK. Usar la ruta de XML que figure en `dump resources`. La optimización de recursos puede abreviar su nombre. En la compilación comprobada fue `res/8G.xml`.

~~~powershell
.\gradlew.bat :app:assembleRelease '-Pp32ReleaseUrl=https://127.0.0.1:8443/' "-Pp32ReleasePinActual=$pinActual" "-Pp32ReleasePinReserva=$pinReserva" "-Pp32ReleasePinCaducidad=$caducidad"
$apk = 'app/build/outputs/apk/release/app-release-unsigned.apk'
aapt2 dump xmltree --file AndroidManifest.xml $apk
aapt2 dump resources $apk
aapt2 dump xmltree --file res/8G.xml $apk
~~~

La política de publicación debe contener el bloqueo de tráfico en claro, las CA del sistema y el conjunto de pines para el host exacto. El manifiesto debe apuntar a esa política y la tabla de recursos debe carecer de la CA de laboratorio. La política de la APK debe carecer de `debug-overrides` y de referencias al almacén de CA del usuario.

La [decisión y el procedimiento de rotación](../DISENO_SEGURIDAD.md#3-datos-en-tránsito--p32) se documentan en el apartado 3. Los ensayos de red usan el backend ficticio y no acreditan el despliegue de un servidor de producción.
