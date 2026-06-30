package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.agendaapppractica.agendaappxd.interfazUI.modales.*
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearPublicacionGrupo(
    grupoId: String,
    tipoPublicacion: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formatoFechaApp = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var tituloPublicacion by remember { mutableStateOf("") }
    var descripcionPublicacion by remember { mutableStateOf("") }

    val opcionesSubtipo = if (tipoPublicacion == "Anuncio") {
        listOf("Aviso", "Alerta", "Recordatorio", "Informativo")
    } else {
        listOf("Examen", "Tarea", "Reunión", "Clase", "Otro")
    }
    var subtipoSeleccionado by remember { mutableStateOf(opcionesSubtipo.first()) }

    var fechaInicio by remember { mutableStateOf(LocalDate.now()) }
    var fechaFin by remember { mutableStateOf(LocalDate.now()) }

    val horaInicioState = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
    val horaFinState = rememberTimePickerState(initialHour = 13, initialMinute = 0, is24Hour = true)

    val horaInicioTexto = String.format("%02d:%02d", horaInicioState.hour, horaInicioState.minute)
    val horaFinTexto = String.format("%02d:%02d", horaFinState.hour, horaFinState.minute)

    var avisosSeleccionados by remember { mutableStateOf(setOf<Long>()) }
    var avisosTemporales by remember { mutableStateOf(avisosSeleccionados) }

    var showModalTexto by remember { mutableStateOf(false) }
    var showModalProgramacion by remember { mutableStateOf(false) }
    var showModalTipoSubcategoria by remember { mutableStateOf(false) }
    var showModalAvisos by remember { mutableStateOf(false) }
    var mostrarConfirmarAnuncioPrioritario by remember { mutableStateOf(false) }

    var showCalendarioInicio by remember { mutableStateOf(false) }
    var showTimePickerInicio by remember { mutableStateOf(false) }
    var showCalendarioFin by remember { mutableStateOf(false) }
    var showTimePickerFin by remember { mutableStateOf(false) }

    val esPeriodoValido = remember(fechaInicio, fechaFin, horaInicioTexto, horaFinTexto) {
        try {
            val inicioDT = fechaInicio.atTime(LocalTime.parse(horaInicioTexto))
            val finDT = fechaFin.atTime(LocalTime.parse(horaFinTexto))
            finDT.isAfter(inicioDT)
        } catch (_: Exception) { false }
    }

    val esFormularioValido = remember(tituloPublicacion, esPeriodoValido) {
        tituloPublicacion.isNotBlank() && esPeriodoValido
    }

    val firestore = remember { FirestoreManager() }

    AlertDialog(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                text = if (tipoPublicacion == "Anuncio") "Publicar Anuncio en Grupo" else "Programar Evento de Grupo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    onClick = { showModalTexto = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.EditNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (tituloPublicacion.isBlank()) {
                                Text("Detalles de la Publicación", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Añade el encabezado e información detallada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text(tituloPublicacion, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (descripcionPublicacion.isNotBlank()) {
                                    Text(descripcionPublicacion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                val colorTiempoCard = if (!esPeriodoValido) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = colorTiempoCard),
                    onClick = { showModalProgramacion = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = if (!esPeriodoValido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vigencia y Horario", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            if (esPeriodoValido) {
                                Text("${fechaInicio.format(formatoFechaApp)} $horaInicioTexto • ${fechaFin.format(formatoFechaApp)} $horaFinTexto", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Error: El término ocurre antes del inicio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    onClick = { showModalTipoSubcategoria = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (tipoPublicacion == "Anuncio") "Tipo de Aviso" else "Categoría del Evento", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(subtipoSeleccionado, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    onClick = {
                        avisosTemporales = avisosSeleccionados
                        showModalAvisos = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Avisos y Alertas", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (avisosSeleccionados.isEmpty()) "Sin alertas pre-programadas" else "${avisosSeleccionados.size} recordatorios activos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (esFormularioValido) {
                        if (tipoPublicacion == "Anuncio" && listOf("Alerta", "Aviso").contains(subtipoSeleccionado)) {
                            mostrarConfirmarAnuncioPrioritario = true
                        } else {
                            firestore.crearEventoGrupo(
                                grupoId = grupoId,
                                titulo = "[$subtipoSeleccionado] $tituloPublicacion",
                                fecha = fechaInicio.format(formatoFechaApp),
                                hora = horaInicioTexto,
                                fechaFin = fechaFin.format(formatoFechaApp),
                                horaFin = horaFinTexto,
                                tipo = tipoPublicacion
                            )
                            Toast.makeText(context, "$tipoPublicacion guardado con éxito", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    }
                },
                enabled = esFormularioValido,
                shape = RoundedCornerShape(10.dp)
            ) { Text("Publicar en Grupo") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showModalTexto) {
        ModalTextoTarea(
            tituloInicial = tituloPublicacion,
            descripcionInicial = descripcionPublicacion,
            onConfirmar = { nuevoT, nuevaD ->
                tituloPublicacion = nuevoT
                descripcionPublicacion = nuevaD
                showModalTexto = false
            },
            onDismiss = { showModalTexto = false }
        )
    }

    if (showModalProgramacion) {
        ModalProgramarHorario(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            horaInicio = horaInicioTexto,
            horaFin = horaFinTexto,
            formatoFechaApp = formatoFechaApp,
            esPeriodoValido = esPeriodoValido,
            onAbrirCalendarioInicio = { showCalendarioInicio = true },
            onAbrirTimePickerInicio = { showTimePickerInicio = true },
            onAbrirCalendarioFin = { showCalendarioFin = true },
            onAbrirTimePickerFin = { showTimePickerFin = true },
            onConfirmar = { showModalProgramacion = false },
            onDismiss = { showModalProgramacion = false }
        )
    }

    if (showModalTipoSubcategoria) {
        ModalSeleccionarTipo(
            opciones = opcionesSubtipo,
            onSeleccion = {
                subtipoSeleccionado = it
                showModalTipoSubcategoria = false
            },
            onDismiss = { showModalTipoSubcategoria = false }
        )
    }

    if (showModalAvisos) {
        ModalProgramarAvisos(
            avisosTemporales = avisosTemporales,
            onCambiarAvisos = { avisosTemporales = it },
            onConfirmar = {
                avisosSeleccionados = avisosTemporales
                showModalAvisos = false
            },
            onDismiss = { showModalAvisos = false }
        )
    }

    if (showCalendarioInicio) {
        Dialog(onDismissRequest = { showCalendarioInicio = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fecha de Inicio", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    CalendarioChile(fechaSeleccionada = fechaInicio, tareas = emptyList(), feriados = emptyList(), onFechaSeleccionada = { fechaInicio = it; if(fechaFin.isBefore(it)) fechaFin = it; showCalendarioInicio = false })
                    TextButton(onClick = { showCalendarioInicio = false }, modifier = Modifier.align(Alignment.End)) { Text("Cerrar") }
                }
            }
        }
    }

    if (showCalendarioFin) {
        Dialog(onDismissRequest = { showCalendarioFin = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fecha de Término", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    CalendarioChile(fechaSeleccionada = fechaFin, tareas = emptyList(), feriados = emptyList(), onFechaSeleccionada = { if (it.isBefore(fechaInicio)) { Toast.makeText(context, "No puede terminar antes del inicio", Toast.LENGTH_SHORT).show() } else { fechaFin = it; showCalendarioFin = false } })
                    TextButton(onClick = { showCalendarioFin = false }, modifier = Modifier.align(Alignment.End)) { Text("Cerrar") }
                }
            }
        }
    }


    if (showTimePickerInicio) {
        Dialog(onDismissRequest = { showTimePickerInicio = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hora de Inicio", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    TimePicker(state = horaInicioState)
                    TextButton(onClick = { showTimePickerInicio = false }, modifier = Modifier.align(Alignment.End)) { Text("Aceptar") }
                }
            }
        }
    }

    if (showTimePickerFin) {
        Dialog(onDismissRequest = { showTimePickerFin = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hora de Término", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    TimePicker(state = horaFinState)
                    TextButton(onClick = { showTimePickerFin = false }, modifier = Modifier.align(Alignment.End)) { Text("Aceptar") }
                }
            }
        }
    }

    if (mostrarConfirmarAnuncioPrioritario) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarAnuncioPrioritario = false },
            icon = { Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("¿Notificar de forma urgente?", fontWeight = FontWeight.Bold) },
            text = { Text("Estás publicando un aviso de tipo [$subtipoSeleccionado]. ¿Deseas emitir notificaciones directas inmediatas a todos los integrantes de este grupo?") },
            confirmButton = {
                Button(onClick = {
                    mostrarConfirmarAnuncioPrioritario = false
                    firestore.crearEventoGrupo(grupoId, "[$subtipoSeleccionado] $tituloPublicacion", fechaInicio.format(formatoFechaApp), horaInicioTexto, fechaFin.format(formatoFechaApp), horaFinTexto, tipoPublicacion)
                    Toast.makeText(context, "¡Publicado con notificación forzada!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }) {
                    Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sí, notificar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarConfirmarAnuncioPrioritario = false
                    firestore.crearEventoGrupo(grupoId, "[$subtipoSeleccionado] $tituloPublicacion", fechaInicio.format(formatoFechaApp), horaInicioTexto, fechaFin.format(formatoFechaApp), horaFinTexto, tipoPublicacion)
                    Toast.makeText(context, "Guardado en el feed silenciosamente", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }) { Text("Solo publicar silenciosamente", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }
}