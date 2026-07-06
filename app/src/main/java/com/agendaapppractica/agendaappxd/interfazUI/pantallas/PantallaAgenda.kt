package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogNuevaTarea
import com.agendaapppractica.agendaappxd.interfazUI.modales.CalendarioChile
import com.agendaapppractica.agendaappxd.model.FeriadoChile
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import com.agendaapppractica.agendaappxd.networkData.ProgramadorRecordatorios
import com.agendaapppractica.agendaappxd.networkData.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgenda() {

    val firestore = remember { FirestoreManager() }
    val context = LocalContext.current
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val miUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val prefs = remember { context.getSharedPreferences("AgendaTutorialPrefs", Context.MODE_PRIVATE) }

    var fechaSeleccionadaCalendar by remember { mutableStateOf(LocalDate.now()) }
    var fechaSeleccionada by remember { mutableStateOf(formatter.format(Date()))}
    var tareasDia by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var tareasMes by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var feriados by remember { mutableStateOf<List<FeriadoChile>>(emptyList()) }

    var misGruposIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var misGruposCompletos by remember { mutableStateOf<List<com.agendaapppractica.agendaappxd.model.Grupo>>(emptyList()) }

    var mostrarDialog by remember { mutableStateOf(false) }
    var mostrarTutorial by remember { mutableStateOf(false) }

    val feriado = feriados.firstOrNull {
        try {
            LocalDate.parse(it.date) == fechaSeleccionadaCalendar
        } catch (_: Exception) {
            false
        }
    }

    LaunchedEffect(fechaSeleccionada, fechaSeleccionadaCalendar.monthValue, fechaSeleccionadaCalendar.year) {
        val yaVisto = prefs.getBoolean("ocultar_tutorial_agenda", false)
        if (!yaVisto) {
            mostrarTutorial = true
        }

        firestore.escucharMisGrupos { grupos ->
            misGruposCompletos = grupos
            misGruposIds = grupos.map { it.id }

            firestore.escucharTareasDelDia(fechaSeleccionada, misGruposIds) { listaTareas ->
                tareasDia = listaTareas.filter { it.grupoId.isNotBlank() || it.usuarioId == miUid }
            }

            val mesFormateado = String.format("%02d/%04d", fechaSeleccionadaCalendar.monthValue, fechaSeleccionadaCalendar.year)
            firestore.escucharTareasDelMes(mesFormateado, misGruposIds) { listaMes ->
                val filtradasPrivacidad = listaMes.filter { it.grupoId.isNotBlank() || it.usuarioId == miUid }
                tareasMes = filtradasPrivacidad.sortedBy { tarea ->
                    try {
                        tarea.fecha.split("/")[0].toInt()
                    } catch (_: Exception) { 0 }
                }
            }
        }

        try {
            val respuesta = RetrofitClient.api.obtenerFeriados()
            if (respuesta.status == "success") {
                feriados = respuesta.data
            }
        } catch (e: Exception) {
            Log.e("API_BOOSTR", "Error al conectar con la API de feriados", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Calendario Corporativo",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { mostrarTutorial = true }) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Ver guía de la agenda",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalendarioChile(
            fechaSeleccionada = fechaSeleccionadaCalendar,
            tareas = tareasDia,
            feriados = feriados,
            onFechaSeleccionada = { fecha ->
                fechaSeleccionadaCalendar = fecha
                fechaSeleccionada = String.format(
                    "%02d/%02d/%04d",
                    fecha.dayOfMonth,
                    fecha.monthValue,
                    fecha.year
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { mostrarDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Crear evento")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (feriado != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = feriado.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = feriado.extra)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Fecha: ${feriado.date}")
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen de Actividades",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Fecha: $fechaSeleccionada", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Eventos Agendados Hoy: ${tareasDia.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cronograma Mensual",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (tareasMes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay eventos ni reuniones programadas este mes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tareasMes.forEach { tarea ->
                    val esPrivado = tarea.grupoId.isBlank()
                    val puedoBorrarlo = tarea.usuarioId == miUid

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (esPrivado) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tarea.fecha.split("/").getOrElse(0) { "01" },
                                    fontWeight = FontWeight.Black,
                                    color = if (esPrivado) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tarea.titulo,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (esPrivado) Icons.Default.Lock else Icons.Default.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = if (esPrivado) "Solo para ti • ${tarea.hora}" else "Grupo Corporativo • ${tarea.hora}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            if (puedoBorrarlo) {
                                IconButton(
                                    onClick = {
                                        firestore.eliminarTarea(tarea.id) { exito ->
                                            if (exito) {
                                                Toast.makeText(context, "Publicación eliminada correctamente", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Borrar publicación",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    if (mostrarDialog) {
        DialogNuevaTarea(
            fecha = fechaSeleccionada,
            grupos = misGruposCompletos,
            feriados = feriados,
            onDismiss = { mostrarDialog = false },
            onGuardar = { tarea ->
                firestore.añadirTarea(tarea.copy(usuarioId = miUid))
                ProgramadorRecordatorios.programarRecordatorios(context, tarea)
                mostrarDialog = false
            }
        )
    }

    if (mostrarTutorial) {
        DialogoTutorialAgenda(
            onDismiss = { mostrarTutorial = false },
            onNoMostrarMas = {
                prefs.edit().putBoolean("ocultar_tutorial_agenda", true).apply()
                mostrarTutorial = false
                Toast.makeText(context, "Asistente desactivado para los próximos arranques", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DialogoTutorialAgenda(
    onDismiss: () -> Unit,
    onNoMostrarMas: () -> Unit
) {
    var pasoActual by remember { mutableStateOf(1) }
    val totalPasos = 3
    val progresoAnimado = pasoActual.toFloat() / totalPasos.toFloat()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (pasoActual) {
                            1 -> Icons.Default.Info
                            2 -> Icons.Default.Lock
                            else -> Icons.Default.EventNote
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pasoActual) {
                        1 -> "Organización del Tiempo"
                        2 -> "Control de Privacidad"
                        else -> "Seguimiento Mensual"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progresoAnimado },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Módulo $pasoActual de $totalPasos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 130.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    when (pasoActual) {
                        1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Navegación del Calendario",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Selecciona cualquier día del recuadro superior para filtrar las actividades agendadas.\n" +
                                        "* La tarjeta informativa inferior te mostrará el resumen del día u observaciones en caso de que coincida con un día festivo oficial.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Eventos Personales y de Grupo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Icono de Candado: Representa actividades estrictamente personales, visibles solo por ti.\n" +
                                        "* Icono de Planeta/Red: Indica que el evento pertenece a un grupo asignado y es compartido con los demás miembros.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Planificación y Alertas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* El Cronograma Mensual lista en orden cronológico todos los compromisos del mes en curso.\n" +
                                        "* Al guardar una nueva tarea mediante el botón principal, el sistema programará automáticamente recordatorios locales en el dispositivo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNoMostrarMas) {
                        Text(
                            text = "No volver a mostrar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = {
                            if (pasoActual < totalPasos) {
                                pasoActual++
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (pasoActual < totalPasos) "Siguiente" else "Entendido",
                            fontWeight = FontWeight.Bold
                        )
                        if (pasoActual < totalPasos) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}