package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.model.Tarea

@Composable
fun DialogEditarEvento(
    tarea: Tarea,
    onDismiss: () -> Unit,
    onGuardar: (Tarea) -> Unit
) {
    var titulo by remember { mutableStateOf(tarea.titulo) }
    var descripcion by remember { mutableStateOf(tarea.descripcion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Evento") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tareaActualizada = tarea.copy(
                        titulo = titulo,
                        descripcion = descripcion
                    )
                    onGuardar(tareaActualizada)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}