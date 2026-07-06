package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.agendaapppractica.agendaappxd.model.MiembroUsuario
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager

@Composable
fun DialogoDetalleMiembro(
    uidMiembro: String,
    onDismiss: () -> Unit
) {
    val firestore = remember { FirestoreManager() }
    var usuario by remember { mutableStateOf<MiembroUsuario?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(uidMiembro) {
        firestore.obtenerDatosUsuario(uidMiembro) { usuarioReal ->
            usuario = usuarioReal
            cargando = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        title = { Text("Perfil del Miembro", fontWeight = FontWeight.Bold) },
        text = {
            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                usuario?.let { u ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!u.fotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = u.fotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                Text(u.nombre.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                        Text(text = u.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = u.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)


                        if (!u.telefono.isNullOrBlank()) {
                            Text(
                                text = "Teléfono: ${u.telefono}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Sin teléfono registrado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } ?: Text("No se pudo cargar la información del usuario.")
            }
        }
    )
}