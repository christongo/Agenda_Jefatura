package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val miUid = FirebaseAuth.getInstance().currentUser?.uid

    var personales by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var listaGruposUsuario by remember { mutableStateOf<List<Grupo>>(emptyList()) }

    var mapaEventosGrupos by remember { mutableStateOf<Map<String, List<Tarea>>>(emptyMap()) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }

    var tabSeleccionada by remember { mutableStateOf(0) } // 0: Hoy, 1: En Curso, 2: Próximos, 3: Historial
    var filtroOrigen by remember { mutableStateOf("todos") }

    // Diálogo de Edición
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
            personales = listaPersonales.filter { it.usuarioId == miUid || it.visibilidad == "personal" }
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
        refrescar()
    }

    val formatterCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formatterSoloFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val ahora = Date()
    val hoyTexto = formatterSoloFecha.format(ahora)

    val todosLosEventosDeMisGrupos = mapaEventosGrupos.values.flatten()

    // --- FILTRADO DE PRIVACIDAD ---
    val todosEventos = (personales + todosLosEventosDeMisGrupos)
        .distinctBy { it.id }
        .filter { tarea ->
            if (tarea.visibilidad == "personal") {
                tarea.usuarioId == miUid
            } else {
                tarea.grupoId.isNotBlank() && listaGruposUsuario.any { it.id == tarea.grupoId }
            }
        }
        .sortedBy { tarea ->
            runCatching { formatterCompleto.parse("${tarea.fecha} ${tarea.hora}") }.getOrNull()
        }

    // 🗓️ 1. PESTAÑA: HOY
    val eventosDeHoy = todosEventos.filter { tarea ->
        tarea.fecha == hoyTexto || runCatching {
            val inicio = formatterSoloFecha.parse(tarea.fecha)
            val fin = formatterSoloFecha.parse(tarea.fechaFin)
            val actual = formatterSoloFecha.parse(hoyTexto)
            actual != null && inicio != null && fin != null && !actual.before(inicio) && !actual.after(fin)
        }.getOrDefault(false)
    }

    // 🗓️ 3. PESTAÑA: PRÓXIMOS
    val proximos = todosEventos.filter { tarea ->
        val inicio = runCatching { formatterCompleto.parse("${tarea.fecha} ${tarea.hora}") }.getOrNull()
        inicio != null && inicio.after(ahora) && tarea.fecha != hoyTexto
    }

    // 📂 4. PESTAÑA: HISTORIAL
    val finalizados = todosEventos.filter { tarea ->
        val fin = runCatching { formatterCompleto.parse("${tarea.fechaFin} ${tarea.horaFin}") }.getOrNull()
        fin != null && ahora.after(fin) && tarea.fechaFin != hoyTexto
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
                        text = "Eventos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FilledTonalIconButton(onClick = { refrescar() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refrescar")
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
                        label = { Text("Todos") }
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
                        label = { Text("De Grupos") },
                        leadingIcon = { Icon(Icons.Default.Groups, null, modifier = Modifier.size(16.dp)) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    ) { paddingValues ->

        // 🔥 Si está seleccionada la pestaña "En Curso", mostramos directamente el aviso de Próximamente
        if (tabSeleccionada == 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Próximamente se agregará",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val listaPorTiempo = when (tabSeleccionada) {
                0 -> eventosDeHoy
                2 -> proximos
                else -> finalizados
            }

            val listaFinalFiltrada = when (filtroOrigen) {
                "personales" -> listaPorTiempo.filter { it.visibilidad == "personal" }
                "grupos" -> listaPorTiempo.filter { it.visibilidad == "grupo" || it.grupoId.isNotBlank() }
                else -> listaPorTiempo
            }

            val mensajeVacio = when {
                listaPorTiempo.isEmpty() -> {
                    when (tabSeleccionada) {
                        0 -> "No tienes tareas ni eventos agendados para hoy."
                        2 -> "No hay eventos futuros planificados."
                        else -> "Tu historial de eventos antiguos está vacío."
                    }
                }
                else -> "No hay eventos que coincidan con los filtros seleccionados."
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
                                        text = "Publicado en: $nombreGrupoAsignado",
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
    }
}