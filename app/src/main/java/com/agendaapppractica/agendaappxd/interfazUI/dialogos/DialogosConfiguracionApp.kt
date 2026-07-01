package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DialogoSeguridadApp(email: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seguridad") },
        text = { Text("Opciones de seguridad para $email") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } }
    )
}

// === DIÁLOGO DE PERMISOS COMPLETAMENTE RESUELTO ===
@Composable
fun DialogoPermisosApp(
    permisoAlmacenamiento: Boolean,
    permisoNotificaciones: Boolean,
    permisoCamara: Boolean,
    onCambiarAlmacenamiento: () -> Unit,
    onCambiarNotificaciones: () -> Unit,
    onCambiarCamara: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(text = "Permisos de la Aplicación", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Concede los accesos necesarios para habilitar todas las funciones de tu agenda de forma manual.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Fila: Notificaciones
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (permisoNotificaciones) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Notificaciones", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            Text("Recordatorios de eventos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = permisoNotificaciones, onCheckedChange = { onCambiarNotificaciones() })
                }

                // Fila: Almacenamiento
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = if (permisoAlmacenamiento) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Almacenamiento", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            Text("Guardar y cargar archivos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = permisoAlmacenamiento, onCheckedChange = { onCambiarAlmacenamiento() })
                }

                // Fila: Cámara
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (permisoCamara) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Cámara", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            Text("Tomar fotos de perfil o notas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = permisoCamara, onCheckedChange = { onCambiarCamara() })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Aceptar", fontWeight = FontWeight.Bold) }
        }
    )
}