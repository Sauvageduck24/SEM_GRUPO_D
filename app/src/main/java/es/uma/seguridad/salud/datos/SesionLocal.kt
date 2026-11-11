package es.uma.seguridad.salud.datos

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.crypto.Cipher

/* ==========================================================================
   LA SESIÓN DEL USUARIO · versión con autenticación
   Teoría: sesión 3.3, diapositivas 19 y 20.

   Sustituye a la versión del paso 1 desde el paso 4 de la práctica.

   QUÉ CAMBIA Y POR QUÉ

   En el paso 1 la clave no exigía autenticación, así que cifrar y descifrar
   se podían hacer en el acto, en cualquier momento y desde cualquier hilo.
   Al añadirle la condición de autenticación, eso deja de ser posible: una
   operación con esa clave sólo existe si ha pasado antes por el diálogo del
   sistema, y el diálogo es asíncrono y necesita una pantalla.

   La consecuencia de diseño es ésta: la sesión ya no se lee del disco cada
   vez que hace falta. Se ABRE una vez, con la huella, y a partir de ahí vive
   en memoria mientras la aplicación está en marcha.

   Fíjate en el precio: el token descifrado está ahora en la memoria del
   proceso. Eso es inevitable —hay que usarlo para hablar con el servidor— y
   es exactamente la cuarta fila de la tabla de la sección 5: lo que sigue sin
   proteger. El cifrado protege el disco, no el proceso.

   UNA DECISIÓN QUE NOTARÁS: los dos valores se guardan en UN SOLO campo.
   Cada operación criptográfica exige una autenticación, así que dos campos
   serían dos huellas. Se empaquetan juntos y se cifran de una vez.
   ========================================================================== */

class SesionLocal(contexto: Context) {

    private val preferencias: SharedPreferences =
        contexto.getSharedPreferences(NOMBRE_FICHERO, Context.MODE_PRIVATE)

    /** En qué situación está la sesión. La pantalla de desbloqueo la observa. */
    enum class Estado {
        /** No hay nada guardado: hay que iniciar sesión. */
        SIN_SESION,

        /** Se acaba de iniciar sesión y falta confirmar para guardarla cifrada. */
        PENDIENTE_DE_GUARDAR,

        /** Hay sesión guardada pero todavía no se ha desbloqueado. */
        BLOQUEADA,

        /** Desbloqueada: el token está en memoria y la aplicación puede trabajar. */
        ABIERTA
    }

    private val _estado = MutableStateFlow(estadoInicial())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private var tokenEnMemoria: String? = null
    private var usuarioEnMemoria: String? = null

    private fun estadoInicial(): Estado =
        if (preferencias.contains(CLAVE_DATOS)) Estado.BLOQUEADA else Estado.SIN_SESION

    /* ----------------------------------------------------------------------
       LO QUE VE EL RESTO DE LA APLICACIÓN

       Estas cuatro funciones tienen la misma firma que en P2.2 y en el paso 1.
       Repositorio y VistaModelo siguen sin enterarse de nada.
       ---------------------------------------------------------------------- */

    /**
     * Guarda la sesión... a medias, y a propósito.
     *
     * No puede escribir en disco todavía, porque cifrar exige autenticación y
     * esta función se llama desde una corrutina del repositorio, sin pantalla
     * a la vista. Lo que hace es dejar los valores en memoria —con lo cual la
     * aplicación ya funciona— y marcar que falta persistirlos.
     *
     * La pantalla de desbloqueo verá ese estado y pedirá la huella.
     */
    fun guardarSesion(token: String, usuario: String) {
        tokenEnMemoria = token
        usuarioEnMemoria = usuario
        _estado.value = Estado.PENDIENTE_DE_GUARDAR
    }

    fun obtenerToken(): String? = tokenEnMemoria

    fun obtenerUsuario(): String? = usuarioEnMemoria

    fun haySesion(): Boolean = tokenEnMemoria != null

    fun cerrarSesion() {
        tokenEnMemoria = null
        usuarioEnMemoria = null
        preferencias.edit().clear().apply()
        _estado.value = Estado.SIN_SESION
    }

    /* ----------------------------------------------------------------------
       LO QUE USA LA PANTALLA DE DESBLOQUEO

       Aquí está la idea de la diapositiva 20. El cifrador NO se usa en estas
       funciones: se crea, se le entrega al diálogo del sistema, y vuelve
       utilizable sólo si la autenticación salió bien. Quien impone la
       condición es el hardware, no este código.
       ---------------------------------------------------------------------- */

    /** Cifrador para persistir la sesión recién iniciada. Va al diálogo. */
    fun cifradorParaGuardar(): Cipher = Cripto.cifradorParaCifrar()

    /** Cifrador para abrir la sesión guardada. Va al diálogo. */
    fun cifradorParaAbrir(): Cipher =
        Cripto.cifradorParaDescifrar(preferencias.getString(CLAVE_DATOS, null)!!)

    /**
     * Termina de guardar, con el cifrador que devuelve el diálogo.
     *
     * Es importante que sea ESE cifrador y no uno nuevo: el que sale del
     * diálogo es el único que el sistema ha habilitado.
     */
    fun completarGuardado(cifrador: Cipher) {
        val token = tokenEnMemoria ?: return
        val usuario = usuarioEnMemoria ?: ""
        val paquete = Cripto.empaquetar(cifrador, "$token$SEPARADOR_CAMPOS$usuario")
        preferencias.edit().putString(CLAVE_DATOS, paquete).apply()
        _estado.value = Estado.ABIERTA
    }

    /**
     * Termina de abrir, con el cifrador que devuelve el diálogo.
     *
     * Devuelve false si los datos ya no se pueden recuperar, y en ese caso los
     * descarta: no hay nada que reintentar. Es el mismo camino no feliz del
     * paso 1, ahora con una causa más, que es que la clave se haya invalidado
     * porque alguien registró una huella nueva.
     */
    fun completarApertura(cifrador: Cipher): Boolean {
        val paquete = preferencias.getString(CLAVE_DATOS, null) ?: return false
        return try {
            val claro = Cripto.desempaquetar(cifrador, paquete)
            tokenEnMemoria = claro.substringBefore(SEPARADOR_CAMPOS)
            usuarioEnMemoria = claro.substringAfter(SEPARADOR_CAMPOS)
            _estado.value = Estado.ABIERTA
            true
        } catch (e: Exception) {
            Log.w("SesionLocal", "Sesión irrecuperable: ${e.javaClass.simpleName}")
            descartarSesionIrrecuperable()
            false
        }
    }

    /**
     * Cuando la clave ya no sirve.
     *
     * Se borran las dos cosas: los datos cifrados, que son ilegibles para
     * siempre, y la clave, que puede estar invalidada. Si se dejara la clave,
     * el siguiente inicio de sesión fallaría igual en el arranque siguiente.
     */
    fun descartarSesionIrrecuperable() {
        tokenEnMemoria = null
        usuarioEnMemoria = null
        preferencias.edit().clear().apply()
        try {
            Cripto.borrarClave()
        } catch (e: Exception) {
            Log.w("SesionLocal", "No se ha podido borrar la clave: ${e.javaClass.simpleName}")
        }
        _estado.value = Estado.SIN_SESION
    }

    companion object {
        const val NOMBRE_FICHERO = "sesion"

        /** Un único campo: una operación criptográfica, una autenticación. */
        const val CLAVE_DATOS = "datos"

        /** Separa el token del usuario dentro del paquete cifrado. */
        private const val SEPARADOR_CAMPOS = "\u0000"
    }
}
