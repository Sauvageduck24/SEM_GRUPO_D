package es.uma.seguridad.salud.datos

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/* ==========================================================================
   LA SESIÓN DEL USUARIO · versión cifrada
   Teoría: sesión 3.3, diapositivas 6 y 12.

   SUSTITUYE al fichero del mismo nombre de la práctica P2.2.

   Lo importante de esta clase es lo que NO ha cambiado: la interfaz pública
   es exactamente la misma que antes. guardarSesion, obtenerToken,
   obtenerUsuario, cerrarSesion y haySesion tienen la misma firma y hacen lo
   mismo desde fuera.

   Por eso ni Repositorio ni VistaModelo ni ninguna pantalla se enteran de que
   ahora hay criptografía por medio. Es el reparto en capas de la sesión 2.5.4
   dando su primer dividendo: una decisión de seguridad que se toma en un solo
   fichero.
   ========================================================================== */

class SesionLocal(contexto: Context) {

    private val preferencias: SharedPreferences =
        contexto.getSharedPreferences(NOMBRE_FICHERO, Context.MODE_PRIVATE)

    /**
     * Guarda la sesión, cifrando cada valor antes de escribirlo.
     *
     * Fíjate en que el NOMBRE de cada campo sigue estando en claro en el XML.
     * Eso es deliberado y vale la pena observarlo cuando leas el fichero con
     * adb: el cifrado protege el contenido, no la estructura. Un adversario
     * seguirá sabiendo que esta aplicación guarda algo llamado "token".
     */
    fun guardarSesion(token: String, usuario: String) {
        preferencias.edit()
            .putString(CLAVE_TOKEN, Cripto.cifrar(token))
            .putString(CLAVE_USUARIO, Cripto.cifrar(usuario))
            .apply()
    }

    fun obtenerToken(): String? = leerCifrado(CLAVE_TOKEN)

    fun obtenerUsuario(): String? = leerCifrado(CLAVE_USUARIO)

    /**
     * Lee un campo y lo descifra.
     *
     * EL CAMINO QUE NO ES FELIZ, y es la mitad del trabajo de esta práctica.
     *
     * Descifrar puede fallar, y no por un error de programación:
     *
     *   - KeyPermanentlyInvalidatedException: alguien ha registrado una huella
     *     nueva en el terminal, o ha quitado el bloqueo de pantalla. El
     *     sistema ha destruido la clave a propósito, que es justo lo que le
     *     pedimos al crearla.
     *   - AEADBadTagException: el texto cifrado no es el que se guardó.
     *
     * En los dos casos los datos son IRRECUPERABLES. No hay nada que
     * reintentar y no hay nada que reparar: lo único honesto es descartarlos y
     * obligar a iniciar sesión otra vez.
     *
     * Lo que NO se debe hacer aquí es capturar la excepción y devolver el
     * valor sin cifrar, ni dejar el catch vacío. Es la tercera prohibición de
     * la diapositiva 21: un try/catch silencioso convierte un fallo de
     * seguridad en una aplicación que parece funcionar.
     */
    private fun leerCifrado(clave: String): String? {
        val paquete = preferencias.getString(clave, null) ?: return null
        return try {
            Cripto.descifrar(paquete)
        } catch (e: Exception) {
            // Se registra QUE ha fallado y de qué tipo. Nunca el contenido.
            Log.w("SesionLocal", "La sesión guardada ya no se puede descifrar: ${e.javaClass.simpleName}")
            descartarSesionIrrecuperable()
            null
        }
    }

    /**
     * Tira la sesión cifrada y la clave que la protegía.
     *
     * Hay que borrar las dos cosas. Si se borran las preferencias y se deja la
     * clave vieja, el siguiente inicio de sesión cifrará con una clave que
     * quizá ya esté invalidada y el problema vuelve en el arranque siguiente.
     */
    private fun descartarSesionIrrecuperable() {
        preferencias.edit().clear().apply()
        try {
            Cripto.borrarClave()
        } catch (e: Exception) {
            Log.w("SesionLocal", "No se ha podido borrar la clave: ${e.javaClass.simpleName}")
        }
    }

    /**
     * Cierra la sesión.
     *
     * La clave se queda: no se ha invalidado nada, simplemente este usuario se
     * ha ido. La siguiente sesión se cifrará con la misma.
     */
    fun cerrarSesion() {
        preferencias.edit().clear().apply()
    }

    /**
     * Si hay sesión.
     *
     * Ojo a una diferencia con P2.2: esto ya no es una consulta gratis. Mirar
     * si hay token obliga a descifrarlo, y por tanto puede disparar el camino
     * de error de arriba. Es el primer sitio donde se nota que cifrar tiene un
     * precio que hay que pagar en algún momento.
     */
    fun haySesion(): Boolean = obtenerToken() != null

    companion object {
        const val NOMBRE_FICHERO = "sesion"
        const val CLAVE_TOKEN = "token"
        const val CLAVE_USUARIO = "usuario"
    }
}
