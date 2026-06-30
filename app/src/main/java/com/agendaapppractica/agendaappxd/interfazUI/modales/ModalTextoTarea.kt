package com.agendaapppractica.agendaappxd.interfazUI.modales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModalTextoTarea(
    tituloInicial: String,
    descripcionInicial: String,
    onConfirmar: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var titulo by remember { mutableStateOf(tituloInicial) }
    var descripcion by remember { mutableStateOf(descripcionInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Evento", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(titulo, descripcion) },
                enabled = titulo.isNotBlank()
            ) { Text("Listo") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}