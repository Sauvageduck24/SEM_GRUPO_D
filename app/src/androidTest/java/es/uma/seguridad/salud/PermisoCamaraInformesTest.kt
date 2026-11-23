package es.uma.seguridad.salud

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.uma.seguridad.salud.modelo.EstadoUi
import es.uma.seguridad.salud.modelo.RegistroSalud
import es.uma.seguridad.salud.ui.PantallaLista
import es.uma.seguridad.salud.ui.PermisoCamaraInformes
import es.uma.seguridad.salud.ui.theme.AppSaludTheme
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/** Ejecutar con laboratorio/probar-camara.ps1: prepara CAMERA denegado sólo para esta app. */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PermisoCamaraInformesTest {
    @get:Rule val regla = createAndroidComposeRule<MainActivity>()
    private val instrumentacion = InstrumentationRegistry.getInstrumentation()
    private val contexto get() = instrumentacion.targetContext
    private val automatizacion get() = instrumentacion.uiAutomation
    private val titulo = "Informe ficticio de cámara"
    private val aperturas = AtomicInteger()

    private fun concedido() = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

    private fun mostrarListado() {
        regla.activityRule.scenario.onActivity { actividad ->
            actividad.setContent {
                AppSaludTheme {
                    PantallaLista(
                        estado = EstadoUi.Exito(listOf(RegistroSalud(99001, titulo,
                            "Contenido ficticio, sin datos médicos reales.", "2026-11-11"))),
                        onRecargar = {}, onSalir = {}, onAbrir = { aperturas.incrementAndGet() }
                    )
                }
            }
        }
        regla.waitForIdle()
    }

    private fun nodos(raiz: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        if (raiz == null) return emptyList()
        return listOf(raiz) + (0 until raiz.childCount).flatMap { nodos(raiz.getChild(it)) }
    }

    private fun pulsarSistema(id: String) {
        val limite = SystemClock.uptimeMillis() + 10000
        while (SystemClock.uptimeMillis() < limite) {
            val boton = nodos(automatizacion.rootInActiveWindow).firstOrNull {
                it.viewIdResourceName?.endsWith(":id/$id") == true ||
                    (id == "permission_deny_button" &&
                        it.viewIdResourceName?.endsWith(":id/permission_deny_and_dont_ask_again_button") == true)
            }
            if (boton != null) {
                assertTrue(boton.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                return
            }
            SystemClock.sleep(100)
        }
        evidencia("diagnostico-boton-ausente")
        throw AssertionError("No apareció $id: " + nodos(automatizacion.rootInActiveWindow)
            .joinToString { "${it.viewIdResourceName}=${it.text}" })
    }

    private fun evidencia(nombre: String) {
        var raiz = automatizacion.rootInActiveWindow
        val limite = SystemClock.uptimeMillis() + 5000
        while (SystemClock.uptimeMillis() < limite) {
            val visibles = nodos(raiz).mapNotNull { it.text?.toString() }.filter { it.isNotBlank() }
            val listo = when {
                nombre.startsWith("01-") || nombre.startsWith("03-") || nombre.startsWith("05-") || nombre.startsWith("07-") -> titulo in visibles
                nombre.startsWith("08-") -> visibles.any { "Este dispositivo no tiene cámara" in it }
                else -> visibles.size > 2
            }
            if (listo) break
            SystemClock.sleep(100)
            raiz = automatizacion.rootInActiveWindow
        }
        val carpeta = File(contexto.getExternalFilesDir(null), "pruebas-camara").apply { mkdirs() }
        val textos = nodos(raiz).filterNot { it.isPassword }.mapNotNull { n ->
            n.text?.toString()?.takeIf { it.isNotBlank() }
        }
        File(carpeta, "$nombre.txt").writeText(
            "Prueba real en MainActivity; listado con fixture local, sin backend ni autenticación.\n" +
                "CAMERA concedido: ${concedido()}\nVentana: ${raiz?.packageName}\n" + textos.joinToString("\n"))
        // Sólo capturar los diálogos del sistema. La ventana de la app conserva FLAG_SECURE.
        val paqueteVentana = raiz?.packageName?.toString().orEmpty()
        if (paqueteVentana.contains("permissioncontroller") || paqueteVentana == "com.android.settings") {
            // Accesibilidad puede publicar el diálogo antes de terminar su animación de entrada.
            automatizacion.waitForIdle(250, 3000)
            automatizacion.takeScreenshot()?.let { bitmap ->
                File(carpeta, "$nombre.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
        Log.i("EstebanCamara", "$nombre CAMERA=${concedido()}")
    }

    @Test fun a_permisoDenegadoPermiteListado() {
        assertFalse("Preparar el permiso con laboratorio/probar-camara.ps1", concedido())
        mostrarListado()
        regla.onNodeWithText("Permitir cámara para informes").assertExists()
        regla.onNodeWithText(titulo).performClick()
        assertEquals(1, aperturas.get())
        evidencia("01-sin-permiso-listado-utilizable")
    }

    @Test fun b_dobleDenegacionYAjustesActualizanEstado() {
        assertFalse(concedido())
        mostrarListado()
        regla.onNodeWithText("Permitir cámara para informes").performClick()
        regla.waitUntil(10000) { automatizacion.rootInActiveWindow?.packageName?.toString()?.contains("permissioncontroller") == true }
        evidencia("02-primer-dialogo")
        pulsarSistema("permission_deny_button")
        regla.onNodeWithText("El permiso no se ha concedido. El resto de la aplicación sigue funcionando.").assertExists()
        regla.onNodeWithText(titulo).assertExists()
        evidencia("03-primera-denegacion")

        regla.onNodeWithText("Permitir cámara para informes").performClick()
        regla.waitUntil(10000) { automatizacion.rootInActiveWindow?.packageName?.toString()?.contains("permissioncontroller") == true }
        evidencia("04-segundo-dialogo")
        pulsarSistema("permission_deny_button")
        regla.onNodeWithText("Abrir ajustes de cámara").assertExists()
        regla.onNodeWithText(titulo).performClick()
        assertEquals(1, aperturas.get())
        evidencia("05-denegacion-repetida")

        regla.onNodeWithText("Abrir ajustes de cámara").performClick()
        regla.waitUntil(10000) { automatizacion.rootInActiveWindow?.packageName?.toString() == "com.android.settings" }
        evidencia("06-ajustes-de-la-aplicacion")
        // Simula conceder desde Ajustes con privilegios del runner, no desde código de la app.
        automatizacion.grantRuntimePermission(contexto.packageName, Manifest.permission.CAMERA)
        ParcelFileDescriptor.AutoCloseInputStream(automatizacion.executeShellCommand("input keyevent KEYCODE_BACK"))
            .bufferedReader().use { it.readText() }
        regla.waitUntil(10000) { automatizacion.rootInActiveWindow?.packageName?.toString() == contexto.packageName }
        regla.onNodeWithText("Permiso de cámara concedido").assertExists()
        regla.onNodeWithText("Abrir ajustes de cámara").assertDoesNotExist()
        regla.onNodeWithText(titulo).assertExists()
        evidencia("07-regreso-de-ajustes-con-permiso")
    }

    @Test fun c_dispositivoSinCamaraNoSolicitaPermiso() {
        regla.activityRule.scenario.onActivity { actividad ->
            actividad.setContent { AppSaludTheme { PermisoCamaraInformes(tieneCamara = false) } }
        }
        regla.onNodeWithText("Este dispositivo no tiene cámara. Puedes consultar tus registros.").assertExists()
        regla.onNodeWithText("Permitir cámara para informes").assertDoesNotExist()
        evidencia("08-sin-camara-simulado-en-el-componente")
    }
}
