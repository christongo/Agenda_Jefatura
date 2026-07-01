package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager

@Composable
fun DialogoEditarBiografiaGrupo(
    grupoId: String,
    nombreInicial: String,
    descripcionInicial: String,
    onDismiss: () -> Unit,
    onCambiosGuardados: (String, String) -> Unit
) {
    var editNombre by remember { mutableStateOf(nombreInicial) }
    var editDescripcion by remember { mutableStateOf(descripcionInicial) }
    val firestore = remember { FirestoreManager() }
    val context = LocalContext.current

    // 🛠️ CORRECCIÓN CLAVE: Sincroniza los estados editables si los parámetros iniciales cambian o tardan en cargar de Firebase
    LaunchedEffect(nombreInicial, descripcionInicial) {
        editNombre = nombreInicial
        editDescripcion = descripcionInicial
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Información del Grupo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editNombre,
                    onValueChange = { editNombre = it },
                    label = { Text("Nombre del Grupo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editDescripcion,
                    onValueChange = { editDescripcion = it },
                    label = { Text("Biografía / Sobre nosotros") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    firestore.actualizarDetallesGrupo(grupoId, editNombre, editDescripcion) { exitoso ->
                        if (exitoso) {
                            onCambiosGuardados(editNombre, editDescripcion)
                            Toast.makeText(context, "Grupo actualizado correctamente", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Error al actualizar la base de datos", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = editNombre.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}