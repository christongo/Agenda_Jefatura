package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.agendaapppractica.agendaappxd.interfazUI.modales.*
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.model.FeriadoChile
import com.google.firebase.auth.FirebaseAuth
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogNuevaTarea(
    fecha: String,
    grupos: List<Grupo>,
    feriados: List<FeriadoChile>,
    onDismiss: () -> Unit,
    onGuardar: (Tarea) -> Unit
) {
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    val formatoFechaApp = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val fechaInicial = try { LocalDate.parse(fecha, formatoFechaApp) } catch (_: Exception) { LocalDate.now() }

    var fechaInicio by remember { mutableStateOf(fechaInicial) }
    var fechaFin by remember { mutableStateOf(fechaInicial) }
    var horaInicio by remember { mutableStateOf("12:00") }
    var horaFin by remember { mutableStateOf("13:00") }

    var horarioProgramado by remember { mutableStateOf(true) }
    var tipoEvento by remember { mutableStateOf("Reunion") }
    var visibilidad by remember { mutableStateOf("personal") }
    var grupoSeleccionado by remember { mutableStateOf("") }

    var avisosSeleccionados by remember { mutableStateOf(setOf<Long>()) }
    var avisosTemporales by remember { mutableStateOf(avisosSeleccionados) }

    var nombreUsuario by remember { mutableStateOf("") }
    var correoUsuario by remember { mutableStateOf("") }

    var showModalTexto by remember { mutableStateOf(false) }
    var showModalProgramacion by remember { mutableStateOf(false) }
    var showModalAvisos by remember { mutableStateOf(false) }
    var mostrarConfirmarAnuncio by remember { mutableStateOf(false) }
    var showModalGrupo by remember { mutableStateOf(false) }
    var showModalTipo by remember { mutableStateOf(false) }
    var showModalVisibilidad by remember { mutableStateOf(false) }

    var showCalendarioInicio by remember { mutableStateOf(false) }
    var showTimePickerInicio by remember { mutableStateOf(false) }
    var showCalendarioFin by remember { mutableStateOf(false) }
    var showTimePickerFin by remember { mutableStateOf(false) }

    val esPeriodoValido = remember(fechaInicio, fechaFin, horaInicio, horaFin) {
        try {
            val inicioDT = fechaInicio.atTime(LocalTime.parse(horaInicio))
            val finDT = fechaFin.atTime(LocalTime.parse(horaFin))
            finDT.isAfter(inicioDT)
        } catch (_: Exception) { false }
    }

    val esFormularioValido = remember(titulo, esPeriodoValido, visibilidad, grupoSeleccionado, horarioProgramado) {
        titulo.isNotBlank() && esPeriodoValido && (visibilidad != "grupo" || grupoSeleccionado.isNotBlank()) && horarioProgramado
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirestoreManager().obtenerDatosUsuario(uid) { usuario ->
                nombreUsuario = usuario.nombre
                correoUsuario = usuario.correo
            }
        }
    }

    val generarObjetoTarea = {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Tarea(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            descripcion = descripcion,
            fecha = fechaInicio.format(formatoFechaApp),
            hora = horaInicio,
            fechaFin = fechaFin.format(formatoFechaApp),
            horaFin = horaFin,
            usuarioId = uid,
            nombreUsuario = nombreUsuario,
            correoUsuario = correoUsuario,
            tipoEvento = tipoEvento,
            visibilidad = visibilidad,
            grupoId = grupoSeleccionado,
            esGlobal = false,
            avisosMinutosAntes = avisosSeleccionados.toList().sorted()
        )
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                "Nuevo Evento",
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
                            if (titulo.isBlank()) {
                                Text("Detalles del Evento", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Añade título y notas descriptivas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text(titulo, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (descripcion.isNotBlank()) {
                                    Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                val colorTiempoCard = if (horarioProgramado && !esPeriodoValido) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                        Icon(Icons.Default.CalendarToday, null, tint = if (horarioProgramado && !esPeriodoValido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fecha y Hora", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            if (horarioProgramado && esPeriodoValido) {
                                Text("${fechaInicio.format(formatoFechaApp)} $horaInicio • ${fechaFin.format(formatoFechaApp)} $horaFin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            } else if (horarioProgramado && !esPeriodoValido) {
                                Text("Error: El fin ocurre antes del inicio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Configura el cronograma del evento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        onClick = { showModalTipo = true }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Categoría", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BookmarkBorder, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(6.dp))
                                Text(tipoEvento, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    val tieneErrorGrupo = visibilidad == "grupo" && grupoSeleccionado.isBlank()
                    val colorDestinatario = if (tieneErrorGrupo) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                    ElevatedCard(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = colorDestinatario),
                        onClick = { showModalVisibilidad = true }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Compartir con", style = MaterialTheme.typography.labelSmall, color = if (tieneErrorGrupo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val iconoVisibilidad = if (visibilidad == "grupo") Icons.Default.Group else Icons.Default.Person
                                Icon(imageVector = iconoVisibilidad, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (tieneErrorGrupo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(6.dp))
                                val textoVis = if (visibilidad == "grupo") {
                                    grupos.find { it.id == grupoSeleccionado }?.nombre ?: "Elegir Grupo ⚠️"
                                } else "Solo yo (Privado)"
                                Text(textoVis, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (tieneErrorGrupo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            }
                        }
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
                                text = if (avisosSeleccionados.isEmpty()) "Sin alertas configuradas" else "${avisosSeleccionados.size} alertas activas",
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
                        val tieneAlertaAlEmpezar = avisosSeleccionados.contains(0L)

                        if (visibilidad == "grupo" && grupoSeleccionado.isNotBlank() && tieneAlertaAlEmpezar) {
                            mostrarConfirmarAnuncio = true
                        } else {
                            onGuardar(generarObjetoTarea())
                        }
                    }
                },
                enabled = esFormularioValido,
                shape = RoundedCornerShape(10.dp)
            ) { Text("Guardar Evento") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showModalProgramacion) {
        ModalProgramarHorario(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            horaInicio = horaInicio,
            horaFin = horaFin,
            formatoFechaApp = formatoFechaApp,
            esPeriodoValido = esPeriodoValido,
            onAbrirCalendarioInicio = { showCalendarioInicio = true },
            onAbrirTimePickerInicio = { showTimePickerInicio = true },
            onAbrirCalendarioFin = { showCalendarioFin = true },
            onAbrirTimePickerFin = { showTimePickerFin = true },
            onConfirmar = { horarioProgramado = true; showModalProgramacion = false },
            onDismiss = { showModalProgramacion = false }
        )
    }

    if (showModalAvisos) {
        ModalProgramarAvisos(
            avisosTemporales = avisosTemporales,
            onCambiarAvisos = { avisosTemporales = it },
            onConfirmar = { avisosSeleccionados = avisosTemporales; showModalAvisos = false },
            onDismiss = { showModalAvisos = false }
        )
    }

    if (showCalendarioInicio) {
        ModalCalendarioInicio(fechaInicio = fechaInicio, feriados = feriados, onFechaSeleccionada = { fechaInicio = it; showCalendarioInicio = false }, onDismiss = { showCalendarioInicio = false })
    }
    if (showTimePickerInicio) {
        ModalTimePickerInicio(onHoraSeleccionada = { horaInicio = it; showTimePickerInicio = false }, onDismiss = { showTimePickerInicio = false })
    }
    if (showCalendarioFin) {
        ModalCalendarioFin(fechaFin = fechaFin, feriados = feriados, onFechaSeleccionada = { fechaFin = it; showCalendarioFin = false }, onDismiss = { showCalendarioFin = false })
    }
    if (showTimePickerFin) {
        ModalTimePickerFin(onHoraSeleccionada = { horaFin = it; showTimePickerFin = false }, onDismiss = { showTimePickerFin = false })
    }

    if (showModalVisibilidad) {
        ModalSeleccionarVisibilidad(opciones = listOf("personal", "grupo"), onSeleccion = { seleccion -> visibilidad = seleccion; showModalVisibilidad = false; if (seleccion == "grupo") showModalGrupo = true else grupoSeleccionado = "" }, onDismiss = { showModalVisibilidad = false })
    }
    if (showModalGrupo) {
        ModalSeleccionarGrupo(grupos = grupos, grupoSeleccionadoId = grupoSeleccionado, onGrupoElegido = { idGrupo -> grupoSeleccionado = idGrupo; showModalGrupo = false }, onDismiss = { showModalGrupo = false })
    }
    if (showModalTexto) {
        ModalTextoTarea(tituloInicial = titulo, descripcionInicial = descripcion, onConfirmar = { nuevoTitulo, nuevaDescripcion -> titulo = nuevoTitulo; descripcion = nuevaDescripcion; showModalTexto = false }, onDismiss = { showModalTexto = false })
    }
    if (showModalTipo) {
        ModalSeleccionarTipo(opciones = listOf("Ocupado", "Reunion", "Vacaciones", "Recordatorio"), onSeleccion = { tipoEvento = it; showModalTipo = false }, onDismiss = { showModalTipo = false })
    }
    if (mostrarConfirmarAnuncio) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarAnuncio = false },
            icon = { Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("¿Anunciar al Grupo?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Quieres notificar inmediatamente a todos los integrantes sobre este evento para asegurar su participación?") },
            confirmButton = {
                Button(onClick = { mostrarConfirmarAnuncio = false; onGuardar(generarObjetoTarea()); Toast.makeText(context, "¡Evento publicado y notificado!", Toast.LENGTH_SHORT).show() }) {
                    Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sí, anunciar")
                }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmarAnuncio = false; onGuardar(generarObjetoTarea()) }) { Text("Solo guardar silenciosamente", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
    }
}