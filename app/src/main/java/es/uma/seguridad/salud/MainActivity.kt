package es.uma.seguridad.salud

import android.content.Context
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

   Sustituye al MainActivity.kt de la práctica P2.2. Tres cosas cambian:

   1. La actividad ya no es ComponentActivity sino FragmentActivity.
      Lo exige el diálogo biométrico, que se apoya en el gestor de fragmentos
      para sobrevivir a un giro de pantalla o a que el sistema recree la
      actividad mientras el dedo está en el sensor. FragmentActivity hereda de
      ComponentActivity, así que todo lo demás sigue valiendo igual.

   2. La sesión la comparten el repositorio y la puerta.
      El repositorio la usa para trabajar y la puerta biométrica para abrirla
      y cerrarla. Tiene que ser LA MISMA instancia en los dos: si hubiera dos,
      cada una tendría su propia copia del token en memoria. Por eso la crea
      FabricaVistaModelo y vive en el VistaModelo, que sobrevive a que la
      actividad se recree al girar la pantalla; si se creara en onCreate,
      cada giro dejaría a la puerta con una sesión distinta.

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

        setContent {
            AppSaludTheme {
                // CAMBIO 2: una sola sesión, la del VistaModelo, para el repositorio y la puerta
                val vistaModelo: VistaModelo = viewModel(factory = FabricaVistaModelo(applicationContext))

                // CAMBIO 3: la navegación sólo se compone si la sesión está abierta
                PuertaBiometrica(actividad = this@MainActivity, sesion = vistaModelo.sesion) {
                    Navegacion(vistaModelo)
                }
            }
        }
    }
}

class FabricaVistaModelo(private val contexto: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val sesion = SesionLocal(contexto)
        val repositorio = Repositorio(
            api = ClienteRed.crear(),
            sesion = sesion,
            dao = BaseDatos.obtener(contexto).elementoDao()
        )
        return VistaModelo(repositorio, sesion) as T
    }
}
