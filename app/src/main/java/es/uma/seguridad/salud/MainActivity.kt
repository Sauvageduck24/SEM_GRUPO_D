package es.uma.seguridad.salud

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import es.uma.seguridad.salud.datos.*
import es.uma.seguridad.salud.ui.Navegacion
import es.uma.seguridad.salud.ui.PuertaBiometrica
import es.uma.seguridad.salud.ui.theme.AppSaludTheme

/* ==========================================================================
   PUNTO DE ENTRADA · versión del paso 4

   SUSTITUYE al MainActivity.kt de la práctica P2.2.
   No lo copies antes del paso 4: necesita ficheros que hasta entonces no
   existen y no compilaría.

   TRES COSAS CAMBIAN, Y CONVIENE SABER CUÁLES ANTES DE PEGAR

   1. La actividad ya no es ComponentActivity sino FragmentActivity.
      Lo exige el diálogo biométrico, que se apoya en el gestor de fragmentos
      para sobrevivir a un giro de pantalla o a que el sistema recree la
      actividad mientras el dedo está en el sensor. FragmentActivity hereda de
      ComponentActivity, así que todo lo demás sigue valiendo igual.

   2. La sesión se saca a una variable propia.
      Antes se construía dentro de la llamada a Repositorio y nadie más la
      veía. Ahora la necesitan dos sitios: el repositorio, para trabajar, y la
      puerta biométrica, para abrirla y cerrarla. Tiene que ser LA MISMA
      instancia en los dos: si se construyeran dos, cada una tendría su propia
      copia del token en memoria y la aplicación se comportaría de forma
      incomprensible.

   3. La navegación va envuelta en la puerta.
      Mientras la sesión guardada esté bloqueada, PuertaBiometrica no compone
      su contenido. No es que lo tape: es que no existe en pantalla.
      ========================================================================== */

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se conserva de P2.3: miniatura de recientes y capturas en negro.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // CAMBIO 2: una sola instancia, compartida por el repositorio y la puerta
        val sesion = SesionLocal(applicationContext)

        val repositorio = Repositorio(
            api = ClienteRed.crear(),
            sesion = sesion,
            dao = BaseDatos.obtener(applicationContext).elementoDao()
        )

        setContent {
            AppSaludTheme {
                val vistaModelo: VistaModelo = viewModel(factory = FabricaVistaModelo(repositorio))

                // CAMBIO 3: la navegación sólo se compone si la sesión está abierta
                PuertaBiometrica(actividad = this@MainActivity, sesion = sesion) {
                    Navegacion(vistaModelo)
                }
            }
        }
    }
}

class FabricaVistaModelo(private val repositorio: Repositorio) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        VistaModelo(repositorio) as T
}
