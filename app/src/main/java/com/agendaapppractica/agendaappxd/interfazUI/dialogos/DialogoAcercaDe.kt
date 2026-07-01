package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agendaapppractica.agendaappxd.BuildConfig

@Composable
fun DialogoAcercaDe(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Acerca de esta App", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Agenda Jefatura", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "Versión instalada: v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Código de compilación: ${BuildConfig.VERSION_CODE}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(text = "Desarrollado para la optimización y gestión de tareas de jefatura de forma práctica.", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}