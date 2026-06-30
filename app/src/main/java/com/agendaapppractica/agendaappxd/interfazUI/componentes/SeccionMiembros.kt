package com.agendaapppractica.agendaappxd.interfazUI.componentes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.model.MiembroUsuario
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SeccionMiembros(
    cargandoMiembros: Boolean,
    listaMiembros: List<MiembroUsuario>,
    grupo: Grupo,
    administradoresLocales: List<String>,
    onMiembroClick: (String) -> Unit,
    onGestionarAdmin: (String, Boolean) -> Unit
) {
    val miUid = FirebaseAuth.getInstance().currentUser?.uid
    val esCreador = groupCreadorId(grupo, miUid)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (cargandoMiembros) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (listaMiembros.isEmpty()) {
        Text(text = "No hay miembros.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(listaMiembros) { miembro ->
                val esElCreadorDelGrupo = miembro.uid == grupo.creadorId
                val esAdminReal = administradoresLocales.contains(miembro.uid)
                var mostrarMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onMiembroClick(miembro.uid) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!miembro.fotoUrl.isNullOrBlank()) {
                            AsyncImage(model = miembro.fotoUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                Text(text = miembro.nombre.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = miembro.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { clipboardManager.setText(AnnotatedString(miembro.uid)); Toast.makeText(context, "ID copiado", Toast.LENGTH_SHORT).show() }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                }
                            }
                            Text(text = when { esElCreadorDelGrupo -> "Creador"; esAdminReal -> "Administrador"; else -> "Miembro" }, style = MaterialTheme.typography.bodySmall)
                        }

                        if (esCreador && !esElCreadorDelGrupo) {
                            Box {
                                IconButton(onClick = { mostrarMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                                DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text(if (!esAdminReal) "Hacer Administrador" else "Quitar Administrador") },
                                        onClick = { mostrarMenu = false; onGestionarAdmin(miembro.uid, esAdminReal) },
                                        leadingIcon = { Icon(if (!esAdminReal) Icons.Default.Shield else Icons.Default.RemoveModerator, null) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun groupCreadorId(grupo: Grupo, miUid: String?): Boolean = grupo.creadorId == miUid