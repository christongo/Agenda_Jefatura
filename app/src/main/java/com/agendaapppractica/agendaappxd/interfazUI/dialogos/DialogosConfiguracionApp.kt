package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DialogoSeguridadApp(
    correoUsuario: String,
    esGoogle: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var nuevaContrasenia by remember { mutableStateOf("") }
    var confirmarContrasenia by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var mostrarConfirmacion by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seguridad") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Correo asociado: $correoUsuario", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))

                if (esGoogle) {
                    Text(
                        "Has iniciado sesión con Google. No es necesario cambiar la contraseña desde aquí ya que la seguridad es administrada por tu cuenta de Google.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Escribe tu nueva contraseña a continuación para actualizarla inmediatamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = nuevaContrasenia,
                        onValueChange = { nuevaContrasenia = it },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargando
                    )

                    OutlinedTextField(
                        value = confirmarContrasenia,
                        onValueChange = { confirmarContrasenia = it },
                        label = { Text("Confirmar contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargando
                    )
                }
            }
        },
        confirmButton = {
            if (esGoogle) {
                TextButton(onClick = onDismiss) { Text("Aceptar") }
            } else {
                Button(
                    onClick = {
                        if (nuevaContrasenia.isBlank() || confirmarContrasenia.isBlank()) {
                            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (nuevaContrasenia.length < 6) {
                            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (nuevaContrasenia != confirmarContrasenia) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        mostrarConfirmacion = true
                    },
                    enabled = !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Actualizar Clave")
                    }
                }
            }
        },
        dismissButton = {
            if (!esGoogle && !cargando) {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("¿Estás seguro?") },
            text = {
                Text("¿Estás seguro de que deseas cambiar la contraseña? Esta acción actualizará tus credenciales de acceso de forma inmediata.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        cargando = true

                        auth.currentUser?.updatePassword(nuevaContrasenia)
                            ?.addOnCompleteListener { tarea ->
                                cargando = false
                                if (tarea.isSuccessful) {
                                    Toast.makeText(context, "Contraseña modificada con éxito", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Error: ${tarea.exception?.localizedMessage}\n(Si el error persiste, cierra sesión y vuelve a entrar)", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                ) {
                    Text("Sí, cambiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("No, cancelar")
                }
            }
        )
    }
}

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