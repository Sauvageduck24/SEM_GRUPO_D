# Inventario de datos sensibles · P2.2

Propuesta local para incorporar al alcance de P2.3, pendiente de revisión del equipo. Los tres primeros campos describen la base actual o indican expresamente que una función todavía no está implementada. El 11/11/2026 Esteban actualiza las filas de fotografías y citas para P3.1; las demás medidas del Bloque III siguen pendientes de sus respectivas aportaciones y de la integración del equipo.

| Dato | Dónde se guarda hoy | Quién puede leerlo hoy | Cómo se protegerá |
| --- | --- | --- | --- |
| Token de sesión | shared_prefs/sesion.xml, memoria, logcat y sesiones del backend | App por su UID; ADB autorizado y run-as en debug; root/procesos privilegiados; servidor; observador de HTTP | A completar en Bloque III |
| Usuario y asociación con el historial | sesion.xml, columna propietario de elementos, logs y backend | Mismas vías de acceso; otras apps ordinarias no pueden abrir el directorio privado | A completar en Bloque III |
| Contraseña de acceso | Campo de acceso en memoria, cuerpo HTTP, BODY en logcat y diccionario docente del backend. No se guarda en sesion.xml | Proceso cliente, lector autorizado de logs, servidor y observador de HTTP | A completar en Bloque III |
| Título, contenido y fecha de un registro de salud ficticio | Tabla elementos en appbase.db y WAL, respuesta HTTP, logcat, memoria y datos de prueba del backend | App, run-as de debug, root/procesos privilegiados, servidor y observador de HTTP | A completar en Bloque III |
| Nombre, correo, DNI y rol del perfil docente | Diccionario del backend y respuesta de /api/perfil. La pantalla actual no pide ni persiste ese perfil | Servidor; si se llama al endpoint, cliente, observador de HTTP y lector autorizado de BODY | A completar en Bloque III |
| Horarios de acceso y consultas | Registros del backend y logcat | Operador del backend y lector autorizado del dispositivo; el observador de red también ve tiempos de conexión | A completar en Bloque III |
| Fotografías de informes (Esteban) | No se capturan ni persisten imágenes en esta versión; sólo se solicita CAMERA | No existen fotografías de informes guardadas por la app que se puedan extraer | P3.1: permiso voluntario, denegación/revocación tratadas y FLAG_SECURE heredado. No se afirma cifrado de imágenes inexistentes. Diseño y límites en [la aportación de Esteban](../p31/ESTEBAN_CAMARA_INFORMES.md) |
| Citas de agenda (Esteban) | Sin modelo ni almacenamiento específico; RegistroSalud puede mostrar texto ficticio de una cita | No existe una agenda persistida independiente en esta versión | Pendiente de la implementación futura y de definir protección/recuperación; no se atribuye cifrado a un dato que aún no se guarda |
| Antecedentes y tratamientos estructurados | Aún no existe almacenamiento específico. Solo se representa contenido genérico de salud mediante RegistroSalud | No hay evidencias de estas funciones estructuradas en esta versión | A completar cuando se implementen y en Bloque III |

MODE_PRIVATE no cifra. Para run-as no basta tener cualquier conexión ADB: el host debe estar autorizado y el paquete debe ser depurable. No se afirma que cualquier app pueda leer estos datos. El APK conserva allowBackup y las reglas de la plantilla; se debe revisar su alcance en Bloque III, sin dar por realizada una extracción de copia de seguridad.

La corrección 5.6 borra filas del usuario y luego preferencias. No revoca tokens remotos, no limpia logs ni garantiza borrado forense. No se ha demostrado ausencia de carreras con una recarga simultánea al cierre.
