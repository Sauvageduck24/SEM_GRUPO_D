package es.uma.seguridad.salud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uma.seguridad.salud.datos.Repositorio
import es.uma.seguridad.salud.modelo.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modelo de vista de la aplicación.
 *
 * Sostiene el estado de las pantallas y lanza el trabajo en viewModelScope, de
 * modo que se cancelan al destruir el modelo, no al cambiar de ruta.
 * La carga del detalle se cancela explícitamente al salir o pedir otro detalle.
 *
 * Está completo. En P2.2 sólo hay que leerlo.
 */
class VistaModelo(private val repositorio: Repositorio) : ViewModel() {

    private val _estadoLogin = MutableStateFlow<EstadoUi<RespuestaLogin>>(EstadoUi.Reposo)
    val estadoLogin: StateFlow<EstadoUi<RespuestaLogin>> = _estadoLogin.asStateFlow()

    private val _estadoLista = MutableStateFlow<EstadoUi<List<RegistroSalud>>>(EstadoUi.Reposo)
    val estadoLista: StateFlow<EstadoUi<List<RegistroSalud>>> = _estadoLista.asStateFlow()

    private var cargaDetalle: kotlinx.coroutines.Job? = null
    private val _estadoDetalle = MutableStateFlow<EstadoUi<RegistroSalud>>(EstadoUi.Reposo)
    val estadoDetalle: StateFlow<EstadoUi<RegistroSalud>> = _estadoDetalle.asStateFlow()

    fun cargarDetalle(id: Int) {
        cargaDetalle?.cancel()
        cargaDetalle = viewModelScope.launch {
            _estadoDetalle.value = EstadoUi.Cargando
            _estadoDetalle.value = repositorio.obtenerElemento(id)
        }
    }

    fun haySesion(): Boolean = repositorio.haySesion()

    fun entrar(usuario: String, clave: String) {
        viewModelScope.launch {
            _estadoLogin.value = EstadoUi.Cargando
            _estadoLogin.value = repositorio.iniciarSesion(usuario, clave)
        }
    }

    fun cargarElementos() {
        viewModelScope.launch {
            _estadoLista.value = EstadoUi.Cargando
            _estadoLista.value = repositorio.obtenerElementos()
        }
    }

    /**
     * Cierre de sesión.
     *
     * Pide al repositorio que borre la sesión y devuelve las dos pantallas a
     * Reposo. Lo segundo no es decorativo: si el estado de acceso se quedara
     * en Exito, la navegación volvería a entrar sola en la lista.
     *
     * PARA EL CUADERNO: además del estado del modelo de vista y del token,
     * ¿qué otros rastros del usuario quedan en el dispositivo? Haz la lista
     * antes de seguir y compruébala en la sección 5.5.
     */
    fun salir() {
        cargaDetalle?.cancel()
        _estadoDetalle.value = EstadoUi.Reposo
        viewModelScope.launch {
            repositorio.cerrarSesion()
            _estadoLogin.value = EstadoUi.Reposo
            _estadoLista.value = EstadoUi.Reposo
            _estadoDetalle.value = EstadoUi.Reposo
        }
    }
}
