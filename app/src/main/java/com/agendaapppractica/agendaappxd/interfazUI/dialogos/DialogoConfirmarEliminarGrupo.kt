package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager

@Composable
fun DialogoConfirmarEliminarGrupo(
    grupoId: String,
    onDismiss: () -> Unit,
    onEliminado: () -> Unit
) {
    val firestore = remember { FirestoreManager() }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar este grupo?", fontWeight = FontWeight.Bold) },
        text = { Text("Esta acción borrará el grupo por completo de manera permanente. No se puede deshacer.") },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    firestore.eliminarGrupo(grupoId)
                    Toast.makeText(context, "Grupo eliminado exitosamente", Toast.LENGTH_LONG).show()
                    onDismiss()
                    onEliminado()
                }
            ) { Text("Eliminar", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}