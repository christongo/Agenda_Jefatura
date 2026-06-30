package com.agendaapppractica.agendaappxd.interfazUI.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agendaapppractica.agendaappxd.model.Tarea
import java.text.SimpleDateFormat
import java.util.*

fun obtenerTiempoTranscurrido(fechaInicioStr: String, horaInicioStr: String): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaEvento = runCatching { formatter.parse("$fechaInicioStr $horaInicioStr") }.getOrNull()

    if (fechaEvento == null) return "Actualizado recientemente"

    val ahora = Date()
    if (fechaEvento.after(ahora)) return "Planificado"

    val diferenciaMs = ahora.time - fechaEvento.time
    val segundos = diferenciaMs / 1000
    val minutos = segundos / 60
    val horas = minutos / 60
    val dias = horas / 24

    return when {
        minutos < 1 -> "Inició ahora mismo"
        minutos < 60 -> "Inició hace $minutos min"
        horas < 24 -> "Inició hace $horas ${if (horas == 1L) "hora" else "horas"}"
        else -> "Inició hace $dias ${if (dias == 1L) "día" else "días"}"
    }
}

@Composable
fun EventoCard(
    tarea: Tarea,
    esPropietario: Boolean,
    onEliminar: () -> Unit,
    onEditar: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }

    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val ahora = Date()

    val fechaInicio = runCatching { formatter.parse("${tarea.fecha} ${tarea.hora}") }.getOrNull()
    val fechaFin = runCatching { formatter.parse("${tarea.fechaFin} ${tarea.horaFin}") }.getOrNull()

    val (textoEstado, colorEstado, iconoEstado) = when {
        fechaInicio == null -> Triple("Estado desconocido", Color.Gray, Icons.Default.HelpOutline)
        fechaFin != null && ahora >= fechaFin -> {
            when (tarea.tipoEvento) {
                "Vacaciones" -> Triple("Vacaciones finalizadas", Color(0xFF757575), Icons.Default.DoneAll)
                "Reunion" -> Triple("Reunión finalizada", Color(0xFF757575), Icons.Default.DoneAll)
                "Recordatorio" -> Triple("Recordatorio realizado", Color(0xFF757575), Icons.Default.DoneAll)
                else -> Triple("Evento finalizado", Color(0xFF757575), Icons.Default.DoneAll)
            }
        }
        fechaFin != null && ahora >= fechaInicio && ahora < fechaFin -> {
            when (tarea.tipoEvento) {
                "Vacaciones" -> Triple("Vacaciones en curso", Color(0xFF00ACC1), Icons.Default.DateRange)
                "Reunion" -> Triple("Reunión en curso", Color(0xFF43A047), Icons.Default.BusinessCenter)
                "Recordatorio" -> Triple("Recordatorio activo", Color(0xFFF4511E), Icons.Default.NotificationsActive)
                else -> Triple("Evento en curso", Color(0xFF43A047), Icons.Default.PlayArrow)
            }
        }
        else -> {
            when (tarea.tipoEvento) {
                "Vacaciones" -> Triple("Vacaciones programadas", Color(0xFF03A9F4), Icons.Default.FlightTakeoff)
                "Reunion" -> Triple("Reunión programada", Color(0xFF673AB7), Icons.Default.Groups)
                "Recordatorio" -> Triple("Recordatorio pendiente", Color(0xFFFFB300), Icons.Default.NotificationAdd)
                else -> Triple("Próximo evento", Color(0xFF2196F3), Icons.Default.Event)
            }
        }
    }

    val tiempoTranscurrido = obtenerTiempoTranscurrido(tarea.fecha, tarea.hora)

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colorEstado.copy(alpha = 0.5f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorEstado.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = iconoEstado, contentDescription = null, tint = colorEstado, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = textoEstado.uppercase(), color = colorEstado, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Text(text = tiempoTranscurrido, color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            }

            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(text = tarea.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Column(modifier = Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (tarea.descripcion.isNotBlank()) {
                            Text(text = tarea.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(Modifier.width(6.dp))
                            Text(text = "Inicio: ${tarea.fecha} - ${tarea.hora}", fontSize = 12.sp)
                        }
                        if (tarea.fechaFin.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                Spacer(Modifier.width(6.dp))
                                Text(text = "Fin: ${tarea.fechaFin} - ${tarea.horaFin}", fontSize = 12.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Label, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(Modifier.width(6.dp))
                            Text(text = "Categoría: ${tarea.tipoEvento}", fontSize = 12.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Organizado por: ${tarea.nombreUsuario.ifBlank { "Usuario" }}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                leadingContent = {
                    Icon(imageVector = if (tarea.visibilidad == "Publico") Icons.Default.Public else Icons.Default.Lock, contentDescription = null, tint = colorEstado.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                },
                trailingContent = {
                    if (esPropietario) {
                        Box {
                            IconButton(onClick = { menuAbierto = true }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opciones")
                            }
                            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                                    onClick = { menuAbierto = false; onEditar() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { menuAbierto = false; onEliminar() }
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}