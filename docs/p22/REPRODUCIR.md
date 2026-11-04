# Ejecutar la propuesta P2.2

Requiere JDK 25, Android SDK 37.0 y Build Tools 36.0.0. El Wrapper fija Gradle 9.6.0. Crear local.properties con sdk.dir apuntando al SDK propio. La app declara minSdk 26 y se ha probado en Android 14/API 34.

1. Descomprimir Fuentes_P2_2.zip. El backend docente se incluye en Backend_docente. No requiere bibliotecas externas.
2. Desde JADE_Salud ejecutar `python3 -u laboratorio/backend_salud.py ../Backend_docente/servidor.py`. Se limita a 127.0.0.1:8080 y utiliza datos de salud ficticios. No arrancar otro proceso si el puerto ya está ocupado.
3. Compilar con `sh gradlew :app:assembleDebug`. Si el JDK está en otra ruta, ajustar org.gradle.java.installations.paths de gradle.properties. JAVA_HOME debe apuntar a un JDK 25.
4. Instalar app/build/outputs/apk/debug/app-debug.apk en un emulador Android (probado con Android 14, API 34).
5. Entrar con una cuenta de prueba documentada en Backend_docente/LEEME.md, pulsar Recargar y comprobar los registros ficticios. La URL es http://10.0.2.2:8080/ para el emulador Android local.
6. Pulsar Salir y extraer preferencias y SQLite según el enunciado. appbase.db, appbase.db-wal y appbase.db-shm forman una extracción. Conservar los originales y consultar una copia. No mezclar archivos de momentos distintos.

La entrega individual usa AppBase y el backend docente sin adaptación. Puede iniciarse `python3 Herramientas/backend_local.py` desde la estructura de Desarrollo original; en el ZIP se incluye un lanzador equivalente al lado del backend. Solo uno de los dos backends puede ocupar 8080 a la vez.

El cambio funcional individual es el cierre de la sección 5.6. Los logs BODY y HTTP permanecen de forma deliberada. No es una aplicación lista para uso real. Los APK incluidos son debug y están firmados con una clave de laboratorio no distribuida. Una reconstrucción local tendrá normalmente otra firma y hash.
