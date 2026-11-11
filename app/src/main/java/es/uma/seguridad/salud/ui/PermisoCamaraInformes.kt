package es.uma.seguridad.salud.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/** Permiso de P2.3: no captura ni persiste informes, ni condiciona el acceso al listado. */
@Composable
fun PermisoCamaraInformes(
    tieneCamara: Boolean = LocalContext.current.packageManager
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
) {
    val contexto = LocalContext.current
    val actividad = remember(contexto) { contexto.actividad() }
    val ciclo = LocalLifecycleOwner.current.lifecycle
    fun permitido() = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

    var concedido by remember { mutableStateOf(permitido()) }
    var denegado by rememberSaveable { mutableStateOf(false) }
    var necesitaAjustes by rememberSaveable { mutableStateOf(false) }
    fun sinNuevaSolicitud(): Boolean = actividad != null &&
        !ActivityCompat.shouldShowRequestPermissionRationale(actividad, Manifest.permission.CAMERA)

    val solicitar = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { resultado ->
        concedido = resultado
        denegado = !resultado
        // Se evalúa después de una denegación: false también significa «no solicitado» al arrancar.
        necesitaAjustes = !resultado && sinNuevaSolicitud()
    }

    DisposableEffect(ciclo, actividad) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                concedido = permitido()
                necesitaAjustes = !concedido && denegado && sinNuevaSolicitud()
            }
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    Column {
        when {
            !tieneCamara -> Text("Este dispositivo no tiene cámara. Puedes consultar tus registros.")
            concedido -> {
                Text("Permiso de cámara concedido")
                Text("La captura de informes aún no está disponible.", style = MaterialTheme.typography.bodySmall)
            }
            else -> {
                Text("La cámara se usará para fotografiar informes médicos. Puedes consultar tus registros sin este permiso.",
                    style = MaterialTheme.typography.bodySmall)
                if (necesitaAjustes) {
                    Text("El permiso está desactivado. Puedes habilitarlo en Ajustes.")
                    Button(onClick = {
                        contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${contexto.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }) { Text("Abrir ajustes de cámara") }
                } else {
                    if (denegado) Text("El permiso no se ha concedido. El resto de la aplicación sigue funcionando.")
                    Button(onClick = {
                        // Comprobar de nuevo antes de solicitar: Ajustes puede haber cambiado el permiso.
                        concedido = permitido()
                        if (!concedido) solicitar.launch(Manifest.permission.CAMERA)
                    }) { Text("Permitir cámara para informes") }
                }
            }
        }
    }
}

private tailrec fun Context.actividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.actividad()
    else -> null
}
