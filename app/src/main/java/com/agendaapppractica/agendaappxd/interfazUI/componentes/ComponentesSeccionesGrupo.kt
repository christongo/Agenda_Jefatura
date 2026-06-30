package com.agendaapppractica.agendaappxd.interfazUI.componentes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.model.MiembroUsuario
import com.agendaapppractica.agendaappxd.model.Tarea

@Composable
fun SeccionDetalles(
    grupo: Grupo,
    nombreActual: String,
    descripcionActual: String,
    esCreador: Boolean,
    listaSolicitudes: List<MiembroUsuario>,
    cargandoSolicitudes: Boolean,
    onSolicitudProcesada: (String, Boolean) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var codigoVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.padding(vertical = 16.dp), contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = nombreActual.take(1).uppercase(), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            IconButton(
                onClick = { Toast.makeText(context, "Próximamente: Cambiar foto", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar Foto", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            }
        }
        Text(text = nombreActual, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(text = "${grupo.miembros.size} Miembros activos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = "Código", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Código de acceso", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        Text(text = if (codigoVisible) grupo.id else "••••••••••••", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Row {
                    IconButton(onClick = { codigoVisible = !codigoVisible }) {
                        Icon(imageVector = if (codigoVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Mostrar")
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(grupo.id))
                        Toast.makeText(context, "¡Código copiado!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (esCreador) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Solicitudes de ingreso (${listaSolicitudes.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    if (cargandoSolicitudes) {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (listaSolicitudes.isEmpty()) {
                        Text(
                            text = "No tienes invitaciones o peticiones pendientes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        listaSolicitudes.forEach { solicitante ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = solicitante.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(text = solicitante.correo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                                    IconButton(
                                        onClick = { onSolicitudProcesada(solicitante.uid, true) },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onSolicitudProcesada(solicitante.uid, false) },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer, CircleShape).size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Rechazar", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Biografía / Sobre nosotros", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(text = if (descripcionActual.isNotBlank()) descripcionActual else "Este grupo no tiene una descripción configurada todavía.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SeccionMiembros(cargandoMiembros: Boolean, listaMiembros: List<MiembroUsuario>, grupo: Grupo) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (cargandoMiembros) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(listaMiembros) { miembro ->
                val esElCreadorDelGrupo = miembro.uid == grupo.creadorId

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = miembro.nombre.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = miembro.nombre, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                Text(text = miembro.correo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(miembro.correo))
                                    Toast.makeText(context, "Correo copiado: ${miembro.correo}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar Correo",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        val (textoRol, colorContenedor, colorTexto) = when {
                            esElCreadorDelGrupo -> Triple("Creador", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                            else -> Triple("Miembro", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            color = colorContenedor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = textoRol,
                                color = colorTexto,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = if (esElCreadorDelGrupo) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionPublicaciones(eventosActivos: List<Tarea>, subPestañaPublicaciones: Int, onSubPestañaCambiada: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = subPestañaPublicaciones,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Tab(
                selected = subPestañaPublicaciones == 0,
                onClick = { onSubPestañaCambiada(0) },
                text = { Text("Eventos", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = subPestañaPublicaciones == 1,
                onClick = { onSubPestañaCambiada(1) },
                text = { Text("Anuncios", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Campaign, null, modifier = Modifier.size(16.dp)) }
            )
        }

        val listaFiltradaInterna = remember(eventosActivos, subPestañaPublicaciones) {
            if (subPestañaPublicaciones == 0) {
                eventosActivos.filter { it.tipoEvento == "Eventos" || it.tipoEvento == "Evento" }
            } else {
                eventosActivos.filter { it.tipoEvento == "Anuncios" || it.tipoEvento == "Anuncio" }
            }
        }

        if (listaFiltradaInterna.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = if (subPestañaPublicaciones == 0) "No hay eventos programados en este grupo." else "No hay anuncios publicados todavía.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(listaFiltradaInterna) { tarea ->
                    val esEventoIcon = tarea.tipoEvento == "Eventos" || tarea.tipoEvento == "Evento"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esEventoIcon) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (esEventoIcon) Icons.Default.Event else Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = if (esEventoIcon) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = tarea.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Inicio: ${tarea.fecha} - ${tarea.hora}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!tarea.fechaFin.isNullOrBlank()) {
                                Text("Término: ${tarea.fechaFin} - ${tarea.horaFin}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}