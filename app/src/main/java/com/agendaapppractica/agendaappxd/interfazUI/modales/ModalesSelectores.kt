package com.agendaapppractica.agendaappxd.interfazUI.modales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModalSeleccionarTipo(
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tipo de Evento", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                opciones.forEach { opcion ->
                    TextButton(
                        onClick = { onSeleccion(opcion); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(opcion, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ModalSeleccionarVisibilidad(
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Visibilidad del Evento", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                opciones.forEach { opcion ->
                    TextButton(
                        onClick = { onSeleccion(opcion); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Muestra "Personal" o "Grupo" con la primera letra en mayúscula
                        Text(opcion.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}