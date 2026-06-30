package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.interfazUI.modales.CalendarioChile
import com.agendaapppractica.agendaappxd.model.FeriadoChile
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogCrearPublicacion(
    grupoId: String,
    firestore: FirestoreManager,
    feriados: List<FeriadoChile>,
    eventos: List<Tarea>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tipoPublicacionSeleccionada by remember { mutableStateOf("Evento") }
    var nuevoAnuncioTitulo by remember { mutableStateOf("") }

    var fechaInicioText by remember { mutableStateOf("") }
    var horaInicioText by remember { mutableStateOf("") }
    var fechaFinText by remember { mutableStateOf("") }
    var horaFinText by remember { mutableStateOf("") }

    var esSelectorFin by remember { mutableStateOf(false) }
    var mostrarSelectorCalendarioModal by remember { mutableStateOf(false) }
    var mostrarSelectorHoraModal by remember { mutableStateOf(false) }
    var fechaSeleccionadaCalendar by remember { mutableStateOf(LocalDate.now()) }

    var mostrarConfirmarAnuncioModal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (tipoPublicacionSeleccionada == "Anuncio") "Publicar nuevo anuncio" else "Programar nuevo evento",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nuevoAnuncioTitulo,
                    onValueChange = { nuevoAnuncioTitulo = it },
                    label = { Text(if (tipoPublicacionSeleccionada == "Anuncio") "¿Qué quieres anunciar?" else "Título del evento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Inicio", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fechaInicioText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha Inicio") },
                        trailingIcon = {
                            IconButton(onClick = { esSelectorFin = false; mostrarSelectorCalendarioModal = true }) {
                                Icon(Icons.Default.Event, null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = horaInicioText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora Inicio") },
                        trailingIcon = {
                            IconButton(onClick = { esSelectorFin = false; mostrarSelectorHoraModal = true }) {
                                Icon(Icons.Default.Schedule, null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Fin", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fechaFinText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha Fin") },
                        trailingIcon = {
                            IconButton(onClick = { esSelectorFin = true; mostrarSelectorCalendarioModal = true }) {
                                Icon(Icons.Default.Event, null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = horaFinText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora Fin") },
                        trailingIcon = {
                            IconButton(onClick = { esSelectorFin = true; mostrarSelectorHoraModal = true }) {
                                Icon(Icons.Default.Schedule, null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    mostrarConfirmarAnuncioModal = true
                },
                enabled = nuevoAnuncioTitulo.isNotBlank() && fechaInicioText.isNotBlank() && fechaFinText.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (mostrarConfirmarAnuncioModal) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarAnuncioModal = false },
            icon = { Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text(text = "¿Anunciar a los miembros?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Has programado '$nuevoAnuncioTitulo'. ¿Quieres notificar de forma inmediata a todos los miembros de este grupo para que participen?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarAnuncioModal = false
                        firestore.crearEventoGrupo(
                            grupoId = grupoId,
                            titulo = nuevoAnuncioTitulo,
                            fecha = fechaInicioText,
                            hora = horaInicioText,
                            fechaFin = fechaFinText,
                            horaFin = horaFinText,
                            tipo = tipoPublicacionSeleccionada
                        )
                        Toast.makeText(context, "¡Evento publicado y anunciado al grupo!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sí, anunciar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmarAnuncioModal = false
                        firestore.crearEventoGrupo(
                            grupoId = grupoId,
                            titulo = nuevoAnuncioTitulo,
                            fecha = fechaInicioText,
                            hora = horaInicioText,
                            fechaFin = fechaFinText,
                            horaFin = horaFinText,
                            tipo = tipoPublicacionSeleccionada
                        )
                        Toast.makeText(context, "Guardado en la agenda del grupo", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                ) { Text("Solo guardar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (mostrarSelectorCalendarioModal) {
        val anioActualDelSistema = Calendar.getInstance().get(Calendar.YEAR)
        var clickConfirmacionSeguridad by remember { mutableStateOf(false) }

        val feriadoDetectado = feriados.firstOrNull {
            try { LocalDate.parse(it.date) == fechaSeleccionadaCalendar } catch (_: Exception) { false }
        }
        val esDiaFeriado = feriadoDetectado != null
        val anioSeleccionado = fechaSeleccionadaCalendar.year
        val esAnioMuyLejos = anioSeleccionado > (anioActualDelSistema + 1)
        val esProximoAnio = anioSeleccionado == (anioActualDelSistema + 1)

        LaunchedEffect(fechaSeleccionadaCalendar) { clickConfirmacionSeguridad = false }

        AlertDialog(
            onDismissRequest = { mostrarSelectorCalendarioModal = false },
            title = { Text(if(esSelectorFin) "Fecha de Término" else "Fecha de Inicio", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp)).padding(4.dp)) {
                        CalendarioChile(
                            fechaSeleccionada = fechaSeleccionadaCalendar,
                            tareas = eventos,
                            feriados = feriados,
                            onFechaSeleccionada = { fecha ->
                                fechaSeleccionadaCalendar = fecha
                                val resultadoFormateado = String.format(
                                    "%02d/%02d/%04d",
                                    fecha.dayOfMonth,
                                    fecha.monthValue,
                                    fecha.year
                                )
                                if (esSelectorFin) {
                                    fechaFinText = resultadoFormateado
                                } else {
                                    fechaInicioText = resultadoFormateado
                                }
                            }
                        )
                    }

                    AnimatedVisibility(visible = esDiaFeriado) {
                        if (feriadoDetectado != null) {
                            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = feriadoDetectado.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = feriadoDetectado.extra, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val requiereAlertaDeSeguridad = (esProximoAnio || esDiaFeriado) && !clickConfirmacionSeguridad
                Button(
                    onClick = {
                        if (requiereAlertaDeSeguridad) { clickConfirmacionSeguridad = true }
                        else { mostrarSelectorCalendarioModal = false }
                    },
                    enabled = !esAnioMuyLejos
                ) {
                    Text(if (requiereAlertaDeSeguridad) "Confirmar Advertencia" else "Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorCalendarioModal = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarSelectorHoraModal) {
        val estadoReloj = rememberTimePickerState(
            initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { mostrarSelectorHoraModal = false },
            title = { Text(if(esSelectorFin) "Selecciona Hora Fin" else "Selecciona Hora Inicio") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = estadoReloj)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val horaFormateada = String.format("%02d:%02d", estadoReloj.hour, estadoReloj.minute)
                    if (esSelectorFin) {
                        horaFinText = horaFormateada
                    } else {
                        horaInicioText = horaFormateada
                    }
                    mostrarSelectorHoraModal = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorHoraModal = false }) { Text("Cancelar") }
            }
        )
    }
}