package es.uma.seguridad.salud.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.uma.seguridad.salud.modelo.*

/**
 * Listado de elementos. ESTÁ TERMINADA: no hace falta tocarla en P2.2.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    estado: EstadoUi<List<RegistroSalud>>,
    onRecargar: () -> Unit,
    onSalir: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis registros de salud") },
                actions = {
                    TextButton(onClick = onRecargar) { Text("Recargar") }
                    TextButton(onClick = onSalir) { Text("Salir") }
                }
            )
        }
    ) { relleno ->
        Column(Modifier.fillMaxSize().padding(relleno)) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                PermisoCamaraInformes()
            }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (estado) {
                is EstadoUi.Reposo -> Text("Pulsa Recargar para cargar los datos")
                is EstadoUi.Cargando -> CircularProgressIndicator()
                is EstadoUi.Error -> Text("No se han podido cargar los elementos")
                is EstadoUi.Exito -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    items(estado.datos) { elemento ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onAbrir(elemento.id) }) {
                            Column(Modifier.padding(16.dp)) {
                                Text(elemento.titulo, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(elemento.contenido, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(elemento.fecha, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
