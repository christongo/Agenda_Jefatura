package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.agendaapppractica.agendaappxd.interfazUI.componentes.EventoCard
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogEditarEvento
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEventos() {

    val firestore = remember { FirestoreManager() }
    val context = LocalContext.current
    val miUid = FirebaseAuth.getInstance().currentUser?.uid

    val prefs = remember { context.getSharedPreferences("EventosTutorialPrefs", Context.MODE_PRIVATE) }

    var personales by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var listaGruposUsuario by remember { mutableStateOf<List<Grupo>>(emptyList()) }

    var mapaEventosGrupos by remember { mutableStateOf<Map<String, List<Tarea>>>(emptyMap()) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }

    var tabSeleccionada by remember { mutableStateOf(0) }
    var filtroOrigen by remember { mutableStateOf("todos") }

    var mostrarTutorial by remember { mutableStateOf(false) }

    tareaEditando?.let { tarea ->
        DialogEditarEvento(
            tarea = tarea,
            onDismiss = { tareaEditando = null },
            onGuardar = { actualizada ->
                firestore.actualizarTarea(actualizada)
                tareaEditando = null
            }
        )
    }

    fun refrescar() {
        firestore.escucharEventosPersonales { listaPersonales ->
            personales = listaPersonales.filter { it.usuarioId == miUid }
        }

        firestore.escucharMisGrupos { listaGrupos ->
            listaGruposUsuario = listaGrupos

            if (listaGrupos.isEmpty()) {
                mapaEventosGrupos = emptyMap()
            } else {
                val mapaActualizado = mapaEventosGrupos.toMutableMap()
                listaGrupos.forEach { grupo ->
                    firestore.escucharEventosGrupo(grupo.id) { eventosDelGrupo ->
                        mapaActualizado[grupo.id] = eventosDelGrupo
                        mapaEventosGrupos = mapaActualizado.toMap()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val yaVisto = prefs.getBoolean("ocultar_tutorial_eventos", false)
        if (!yaVisto) {
            mostrarTutorial = true
        }
        refrescar()
    }

    val formatterCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formatterSoloFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val ahora = Date()
    val hoyTexto = formatterSoloFecha.format(ahora)

    val todosLosEventosDeMisGrupos = mapaEventosGrupos.values.flatten()

    val todosEventos = (personales + todosLosEventosDeMisGrupos)
        .distinctBy { it.id }
        .filter { tarea ->
            if (tarea.grupoId.isBlank()) {
                tarea.usuarioId == miUid
            } else {
                listaGruposUsuario.any { it.id == tarea.grupoId }
            }
        }
        .sortedBy { tarea ->
            runCatching { formatterCompleto.parse("${tarea.fecha} ${tarea.hora}") }.getOrNull()
        }

    val eventosDeHoy = todosEventos.filter { tarea ->
        tarea.fecha == hoyTexto || runCatching {
            val inicio = formatterSoloFecha.parse(tarea.fecha)
            val fin = formatterSoloFecha.parse(tarea.fechaFin)
            val actual = formatterSoloFecha.parse(hoyTexto)
            actual != null && inicio != null && fin != null && !actual.before(inicio) && !actual.after(fin)
        }.getOrDefault(false)
    }

    val eventosEnCurso = todosEventos.filter { tarea ->
        runCatching {
            val inicioCompleto = formatterCompleto.parse("${tarea.fecha} ${tarea.hora}")
            val finCompleto = formatterCompleto.parse("${tarea.fechaFin} ${tarea.horaFin}")
            inicioCompleto != null && finCompleto != null && !ahora.before(inicioCompleto) && !ahora.after(finCompleto)
        }.getOrDefault(false)
    }

    val proximos = todosEventos.filter { tarea ->
        val inicio = runCatching { formatterCompleto.parse("${tarea.fecha} ${tarea.hora}") }.getOrNull()
        inicio != null && inicio.after(ahora) && tarea.fecha != hoyTexto
    }

    val finalizados = todosEventos.filter { tarea ->
        val fin = runCatching { formatterCompleto.parse("${tarea.fechaFin} ${tarea.horaFin}") }.getOrNull()
        fin != null && ahora.after(fin) && tarea.fechaFin != hoyTexto && !eventosEnCurso.contains(tarea)
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Panel de Eventos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { mostrarTutorial = true }) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Ver guía de eventos",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        FilledTonalIconButton(onClick = { refrescar() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sincronizar feed")
                        }
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp
                ) {
                    Tab(
                        selected = tabSeleccionada == 0,
                        onClick = { tabSeleccionada = 0 },
                        text = { Text("Hoy", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.Today, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = tabSeleccionada == 1,
                        onClick = { tabSeleccionada = 1 },
                        text = { Text("En Curso", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = tabSeleccionada == 2,
                        onClick = { tabSeleccionada = 2 },
                        text = { Text("Próximos", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = tabSeleccionada == 3,
                        onClick = { tabSeleccionada = 3 },
                        text = { Text("Historial", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.CheckCircleOutline, null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroOrigen == "todos",
                        onClick = { filtroOrigen = "todos" },
                        label = { Text("Ver todo") }
                    )
                    FilterChip(
                        selected = filtroOrigen == "personales",
                        onClick = { filtroOrigen = "personales" },
                        label = { Text("Personales") },
                        leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = filtroOrigen == "grupos",
                        onClick = { filtroOrigen = "grupos" },
                        label = { Text("Grupos") },
                        leadingIcon = { Icon(Icons.Default.Groups, null, modifier = Modifier.size(16.dp)) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    ) { paddingValues ->

        val listaPorTiempo = when (tabSeleccionada) {
            0 -> eventosDeHoy
            1 -> eventosEnCurso
            2 -> proximos
            else -> finalizados
        }

        val listaFinalFiltrada = when (filtroOrigen) {
            "personales" -> listaPorTiempo.filter { it.grupoId.isBlank() }
            "grupos" -> listaPorTiempo.filter { it.grupoId.isNotBlank() }
            else -> listaPorTiempo
        }

        val mensajeVacio = when {
            listaPorTiempo.isEmpty() -> {
                when (tabSeleccionada) {
                    0 -> "No tienes actividades planificadas para la jornada de hoy."
                    1 -> "No hay eventos corporativos ejecutándose en este momento."
                    2 -> "No hay compromisos u objetivos futuros agendados."
                    else -> "El historial de actividades concluidas está despejado."
                }
            }
            else -> "Ningún evento coincide con el criterio del filtro seleccionado."
        }

        if (listaFinalFiltrada.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mensajeVacio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(listaFinalFiltrada) { tarea ->
                    val nombreGrupoAsignado = remember(tarea.grupoId, listaGruposUsuario) {
                        listaGruposUsuario.find { it.id == tarea.grupoId }?.nombre
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (!nombreGrupoAsignado.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Área corporativa: $nombreGrupoAsignado",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        EventoCard(
                            tarea = tarea,
                            esPropietario = tarea.usuarioId == miUid,
                            onEliminar = { firestore.eliminarTarea(tarea.id) },
                            onEditar = { tareaEditando = tarea }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (mostrarTutorial) {
        DialogoTutorialEventos(
            onDismiss = { mostrarTutorial = false },
            onNoMostrarMas = {
                prefs.edit().putBoolean("ocultar_tutorial_eventos", true).apply()
                mostrarTutorial = false
                Toast.makeText(context, "Asistente del panel desactivado", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DialogoTutorialEventos(
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
                            1 -> Icons.Default.Today
                            2 -> Icons.Default.FilterList
                            else -> Icons.Default.Refresh
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pasoActual) {
                        1 -> "Línea de Tiempo"
                        2 -> "Segmentación Avanzada"
                        else -> "Sincronización Remota"
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
                        text = "Fase $pasoActual de $totalPasos",
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
                                text = "Navegación por Pestañas Temporales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Hoy / En Curso: Monitorea las responsabilidades inmediatas y reuniones activas programadas para el bloque actual.\n" +
                                        "* Próximos / Historial: Consulta los hitos del resto del año o realiza una auditoría de tus compromisos completados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Filtros de Origen (Chips)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Ver Todo: Consolida la totalidad de tus tareas en un listado unificado.\n" +
                                        "* Personales / Grupos: Discrimina rápidamente entre asignaciones privadas individuales y los comunicados oficiales compartidos de tus dependencias corporativas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Acciones y Actualizaciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Botón de Sincronización: Actualiza manualmente el feed consultando la base de datos de Firebase.\n" +
                                        "* Gestión de Tarjetas: Si eres propietario del evento, podrás modificar los parámetros o removerlo directamente desde los controles integrados.",
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
                            text = if (pasoActual < totalPasos) "Siguiente" else "Finalizar",
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