# Seguimiento de la aportación de Esteban

Esteban Sánchez Gámez · JADE Salud · 11/11/2026.

El reparto se toma del README, tabla «Reparto del trabajo», y de docs/ALCANCE.md, apartado 5. Mi ámbito es cámara, informes y agenda de citas, con sus permisos, denegación y protección de fotografías/archivos. Esta revisión no reasigna tareas ni certifica el trabajo pendiente de otros integrantes.

## Revisión de prácticas anteriores

| Práctica | Exigencia grupal revisada | Estado de mi ámbito |
| --- | --- | --- |
| P2.1, apartado 5 | Base compilable, dominio, requisito peligroso justificado y reparto | El README ya identifica a Esteban y CAMERA, y documenta la compilación inicial. No se ha identificado otra implementación obligatoria pendiente de mi parte en ese apartado |
| P2.2, sección 6 | Versión común, modelo adaptado y primeras columnas del inventario | main ya contiene RegistroSalud, backend adaptado, inventario y responsables. La fila de fotografías debe reflejar que aún no se guardan; no se exige aquí una agenda o captura completas |
| P2.3, sección 6 y paso 6 | Permiso coherente, denegación sin bloquear la aplicación, documento de alcance y base etiquetada | CAMERA y hardware opcional ya están integrados, al igual que FLAG_SECURE y el tag bloque-II. Se completa ahora la evidencia propia y el tratamiento de revocación/denegación repetida |
| P3.1, sección 6 | Protección en reposo, inventario actualizado y diseño iniciado | Se entrega el apartado de cámara/informes con controles reales, límites y decisiones pendientes. La sesión biométrica está en la rama de Ángel; almacenamiento y red mantienen sus responsables |

Las funciones de fotografiar informes, agenda, recordatorios e IA del README son planificación del producto. No aparecen como una tarea de implementación obligatoria de Esteban en las partes grupales de P2.1–P2.3; no se marcan como realizadas ni se añaden para aparentar cierre.

## Aportación en esta rama

Rama: feature/esteban-p31-camara-informes. Base: main, eab801c (tag bloque-II).

- Permiso de cámara aislado en un componente reutilizable; actualización del estado al volver de Ajustes.
- Explicación de uso, denegación sin bloquear el listado y acción a Ajustes tras rechazo reiterado.
- Pruebas instrumentadas propias, con capturas de diálogos y estados de accesibilidad, sin backend ni datos médicos reales.
- Actualización limitada de las filas propias del inventario y [apartado de diseño de cámara/informes](../p31/ESTEBAN_CAMARA_INFORMES.md).

## Resultados

Ejecución final: 11/11/2026, emulador Android 17/API 37. APK y APK de test compilados mediante Gradle. [Metadatos, alcance y SHA-256](../p31/evidencias-esteban/20261111-145327/resultado.json).

| Comprobación | Resultado y evidencia |
| --- | --- |
| Listado sin CAMERA; no petición automática | Correcto: se presenta el botón voluntario y se invoca el callback de detalle. [Estado real](../p31/evidencias-esteban/20261111-145327/estados/01-sin-permiso-listado-utilizable.txt) |
| Primera denegación | El listado permanece utilizable y se permite volver a solicitar. [Diálogo del sistema](../p31/evidencias-esteban/20261111-145327/estados/02-primer-dialogo.png) y [estado posterior](../p31/evidencias-esteban/20261111-145327/estados/03-primera-denegacion.txt) |
| Segunda denegación | Se ofrece Ajustes, no se lanza un bucle de solicitudes. [Segundo diálogo](../p31/evidencias-esteban/20261111-145327/estados/04-segundo-dialogo.png) y [estado posterior](../p31/evidencias-esteban/20261111-145327/estados/05-denegacion-repetida.txt) |
| Cambio desde Ajustes | Se abre la ficha de SaludApp; el runner concede CAMERA allí y al regresar se lee el permiso real. [Ajustes](../p31/evidencias-esteban/20261111-145327/estados/06-ajustes-de-la-aplicacion.png) y [estado concedido](../p31/evidencias-esteban/20261111-145327/estados/07-regreso-de-ajustes-con-permiso.txt) |
| Sin cámara | Correcto en el componente con hardware simulado ausente; no se solicita permiso. [Estado](../p31/evidencias-esteban/20261111-145327/estados/08-sin-camara-simulado-en-el-componente.txt) |
| Revocación real de CAMERA | Se revoca fuera del proceso y se repite la prueba del listado: permiso denegado y listado utilizable. [Resultado del runner](../p31/evidencias-esteban/20261111-145327/revocacion.txt) y [estado](../p31/evidencias-esteban/20261111-145327/estado-tras-revocar.txt) |

Resultado: [OK (3 tests)](../p31/evidencias-esteban/20261111-145327/instrumentacion.txt), más [OK (1 test)](../p31/evidencias-esteban/20261111-145327/revocacion.txt) después de revocar. Se restaura CAMERA al estado previo de esta ejecución, denegado. No se borran datos de sesión ni se usa pm reset-permissions global.

Compatibilidad: la aportación se aplicó sin conflictos sobre una copia aislada de feature/angel-p31-cifrado, commit 56428ae54bae7142aefcc3bdacedd1f9e5458d82. Se compilaron el APK y el APK de test juntos: [registro Gradle](../p31/evidencias-esteban/20261111-145327/compilacion-integracion-angel.txt). Esto acredita compatibilidad de compilación, no una prueba de las operaciones biométricas de esa rama ni una fusión en main.

Las ejecuciones preliminares y los diagnósticos del entorno se conservan en laboratorio/.local; la carpeta enlazada contiene la ejecución final seleccionada. Las fotografías de informes siguen sin generarse: las PNG son capturas de los diálogos del sistema tomadas por el runner.

## Lo que no acredita esta aportación

No acredita una captura de informes ni una agenda implementadas, un cifrado de SQLite/imágenes, TLS, una autorización correcta del servidor ni una revisión colectiva. El permiso y FLAG_SECURE protegen canales concretos; no bastan para asegurar un historial médico. La integración de ramas y revisión por otro compañero siguen el procedimiento del equipo.
