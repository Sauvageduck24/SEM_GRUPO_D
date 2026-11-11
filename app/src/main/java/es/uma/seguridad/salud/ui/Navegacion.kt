package es.uma.seguridad.salud.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.uma.seguridad.salud.VistaModelo
import es.uma.seguridad.salud.modelo.EstadoUi

/**
 * Navegación entre pantallas. ESTÁ TERMINADA: no hace falta tocarla en P2.2.
 *
 * En la práctica P2.3 se ampliará con argumentos de ruta y enlaces profundos,
 * y se comprobará que la navegación no es un control de seguridad (sesión 2.5.3).
 */
@Composable
fun Navegacion(vistaModelo: VistaModelo) {
    val navController = rememberNavController()
    val estadoLogin by vistaModelo.estadoLogin.collectAsState()
    val estadoDetalle by vistaModelo.estadoDetalle.collectAsState()
    val estadoLista by vistaModelo.estadoLista.collectAsState()

    // P3.1: la puerta biométrica solo compone esto con la sesión abierta o sin
    // sesión. Si ya está abierta (se acaba de desbloquear), se va al listado
    // en lugar de volver a pedir usuario y contraseña.
    val inicio = if (vistaModelo.haySesion()) "lista" else "login"

    NavHost(navController = navController, startDestination = inicio) {

        composable("login") {
            PantallaLogin(
                estado = estadoLogin,
                onEntrar = { usuario, clave -> vistaModelo.entrar(usuario, clave) }
            )
            LaunchedEffect(estadoLogin) {
              if (estadoLogin is EstadoUi.Exito) {
                navController.navigate("lista") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        }

        composable("lista") {
            PantallaLista(
                estado = estadoLista,
                onRecargar = { vistaModelo.cargarElementos() },
                onAbrir = { id -> navController.navigate("detalle/$id") },
                onSalir = {
                    vistaModelo.salir()
                    navController.navigate("login") {
                        popUpTo("lista") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "detalle/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "appsegura://detalle/{id}" })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            PantallaDetalle(id, estadoDetalle,
                onCargar = vistaModelo::cargarDetalle,
                onVolver = { navController.popBackStack() })
        }
    }
}
