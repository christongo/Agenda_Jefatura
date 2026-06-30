package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.interfazUI.componentes.SeccionDetalles
import com.agendaapppractica.agendaappxd.interfazUI.componentes.SeccionPublicaciones
import com.agendaapppractica.agendaappxd.interfazUI.componentes.SeccionMiembros
import com.agendaapppractica.agendaappxd.interfazUI.componentes.TopAppBarDetalleGrupo
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoEditarBiografiaGrupo
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoDetalleMiembro
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoConfirmarEliminarGrupo
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoCrearPublicacionGrupo
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.model.MiembroUsuario
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoConfirmarEliminarGrupo
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoCrearPublicacionGrupo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleGrupo(
    grupo: Grupo,
    onVolver: () -> Unit
) {
    val firestore = remember { FirestoreManager() }
    val miUid = FirebaseAuth.getInstance().currentUser?.uid

    var nombreActual by remember { mutableStateOf(grupo.nombre) }
    var descripcionActual by remember { mutableStateOf(grupo.descripcion) }

    val grupoAdaptado = remember(grupo, nombreActual, descripcionActual) {
        grupo.copy(id = grupo.codigo, nombre = nombreActual, descripcion = descripcionActual)
    }

    var eventos by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var listaMiembros by remember { mutableStateOf<List<MiembroUsuario>>(emptyList()) }
    var pestañaSeleccionada by remember { mutableStateOf(0) }
    var cargandoMiembros by remember { mutableStateOf(false) }
    var listaSolicitudes by remember { mutableStateOf<List<MiembroUsuario>>(emptyList()) }
    var cargandoSolicitudes by remember { mutableStateOf(false) }
    var estaRefrescando by remember { mutableStateOf(false) }
    var subPestañaPublicaciones by remember { mutableStateOf(0) }

    var mostrarEditarDetalles by remember { mutableStateOf(false) }
    var mostrarCrearPublicacionDialog by remember { mutableStateOf(false) }
    var tipoPublicacionSeleccionada by remember { mutableStateOf("Evento") }
    var mostrarDialogoConfirmacionEliminar by remember { mutableStateOf(false) }
    var usuarioSeleccionadoUid by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoPerfil by remember { mutableStateOf(false) }

    val esCreador = grupo.creadorId == miUid
    val esAdminOCreador = esCreador || grupo.administradores.contains(miUid)
    var administradoresLocales by remember(grupo.administradores) { mutableStateOf(grupo.administradores) }

    val recargarDatosManual = {
        if (esCreador && grupo.solicitudes.isNotEmpty()) {
            cargandoSolicitudes = true
            val solicitudesTemporales = mutableListOf<MiembroUsuario>()
            var peticionesFinalizadas = 0
            grupo.solicitudes.forEach { uid ->
                firestore.obtenerDatosUsuario(uid) { usuarioReal ->
                    solicitudesTemporales.add(usuarioReal)
                    peticionesFinalizadas++
                    if (peticionesFinalizadas == grupo.solicitudes.size) {
                        listaSolicitudes = solicitudesTemporales
                        cargandoSolicitudes = false
                        estaRefrescando = false
                    }
                }
            }
        } else {
            listaSolicitudes = emptyList()
            cargandoSolicitudes = false
            estaRefrescando = false
        }
    }

    LaunchedEffect(grupo.id) { firestore.escucharEventosGrupo(grupo.id) { eventos = it } }
    LaunchedEffect(grupo.solicitudes) { recargarDatosManual() }

    LaunchedEffect(pestañaSeleccionada) {
        if (pestañaSeleccionada == 1 && listaMiembros.isEmpty()) {
            cargandoMiembros = true
            val miembrosTemporales = mutableListOf<MiembroUsuario>()
            var peticionesFinalizadas = 0
            if (grupo.miembros.isEmpty()) {
                cargandoMiembros = false
            } else {
                grupo.miembros.forEach { uid ->
                    firestore.obtenerDatosUsuario(uid) { usuarioReal ->
                        miembrosTemporales.add(usuarioReal)
                        peticionesFinalizadas++
                        if (peticionesFinalizadas == grupo.miembros.size) {
                            listaMiembros = miembrosTemporales.sortedByDescending { it.uid == groupCreadorId(grupo) }
                            cargandoMiembros = false
                        }
                    }
                }
            }
        }
    }

    val eventosActivos = remember(eventos) {
        val ahora = LocalDateTime.now()
        val formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formateadorHora = DateTimeFormatter.ofPattern("H:mm")
        eventos.filter { tarea ->
            try {
                val campoFechaLimite = if (!tarea.fechaFin.isNullOrBlank()) tarea.fechaFin else tarea.fecha
                val campoHoraLimite = if (!tarea.horaFin.isNullOrBlank()) tarea.horaFin else tarea.hora
                LocalDateTime.of(LocalDate.parse(campoFechaLimite, formateadorFecha), LocalTime.parse(campoHoraLimite, formateadorHora)).isAfter(ahora)
            } catch (_: Exception) { true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBarDetalleGrupo(
                nombreGrupo = nombreActual,
                pestañaSeleccionada = pestañaSeleccionada,
                esAdminOCreador = esAdminOCreador,
                esCreador = esCreador,
                grupoId = grupo.id,
                onVolver = onVolver,
                onRefrescar = { estaRefrescando = true; recargarDatosManual() },
                onEditarInfoClick = { mostrarEditarDetalles = true },
                onEliminarGrupoClick = { mostrarDialogoConfirmacionEliminar = true }
            )
        },
        floatingActionButton = {
            if (pestañaSeleccionada == 2 && esAdminOCreador) {
                FloatingActionButton(
                    onClick = {
                        tipoPublicacionSeleccionada = if (subPestañaPublicaciones == 0) "Evento" else "Anuncio"
                        mostrarCrearPublicacionDialog = true
                    },
                    containerColor = if (subPestañaPublicaciones == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = if (subPestañaPublicaciones == 0) Icons.Default.Event else Icons.Default.Campaign, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = pestañaSeleccionada) {
                Tab(selected = pestañaSeleccionada == 0, onClick = { pestañaSeleccionada = 0 }, text = { Text("Detalles") })
                Tab(selected = pestañaSeleccionada == 1, onClick = { pestañaSeleccionada = 1 }, text = { Text("Miembros") })
                Tab(selected = pestañaSeleccionada == 2, onClick = { pestañaSeleccionada = 2 }, text = { Text("Publicaciones") })
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (pestañaSeleccionada) {
                    0 -> PullToRefreshBox(isRefreshing = estaRefrescando, onRefresh = { estaRefrescando = true; recargarDatosManual() }, modifier = Modifier.fillMaxSize()) {
                        SeccionDetalles(grupo = grupoAdaptado, nombreActual = nombreActual, descripcionActual = descripcionActual, esCreador = esCreador, listaSolicitudes = listaSolicitudes, cargandoSolicitudes = cargandoSolicitudes, onSolicitudProcesada = { uid, aceptar -> if (aceptar) firestore.aceptarSolicitudDeUnion(grupo.id, uid) { if (it) listaSolicitudes = listaSolicitudes.filter { s -> s.uid != uid } } else firestore.rechazarSolicitudDeUnion(grupo.id, uid) { if (it) listaSolicitudes = listaSolicitudes.filter { s -> s.uid != uid } } })
                    }
                    1 -> SeccionMiembros(cargandoMiembros = cargandoMiembros, listaMiembros = listaMiembros, grupo = grupo, administradoresLocales = administradoresLocales, onMiembroClick = { uid -> usuarioSeleccionadoUid = uid; mostrarDialogoPerfil = true }, onGestionarAdmin = { uid, quitar -> if (quitar) firestore.quitarAdministradorGrupo(grupo.id, uid) { if (it) administradoresLocales = administradoresLocales - uid } else firestore.asignarAdministradorGrupo(grupo.id, uid) { if (it) administradoresLocales = administradoresLocales + uid } })
                    2 -> SeccionPublicaciones(eventosActivos = eventosActivos, subPestañaPublicaciones = subPestañaPublicaciones, onSubPestañaCambiada = { subPestañaPublicaciones = it })
                }
            }
        }
    }

    if (mostrarEditarDetalles) {
        DialogoEditarBiografiaGrupo(grupoId = grupo.id, nombreInicial = nombreActual, descripcionInicial = descripcionActual, onDismiss = { mostrarEditarDetalles = false }, onCambiosGuardados = { n, d -> nombreActual = n; descripcionActual = d })
    }
    if (mostrarDialogoPerfil && usuarioSeleccionadoUid != null) {
        DialogoDetalleMiembro(uidMiembro = usuarioSeleccionadoUid!!, onDismiss = { mostrarDialogoPerfil = false; usuarioSeleccionadoUid = null })
    }
    if (mostrarDialogoConfirmacionEliminar) {
        DialogoConfirmarEliminarGrupo(grupoId = grupo.id, onDismiss = { mostrarDialogoConfirmacionEliminar = false }, onEliminado = onVolver)
    }
    if (mostrarCrearPublicacionDialog) {
        DialogoCrearPublicacionGrupo(grupoId = grupo.id, tipoPublicacion = tipoPublicacionSeleccionada, onDismiss = { mostrarCrearPublicacionDialog = false })
    }
}

private fun groupCreadorId(grupo: Grupo): String = grupo.creadorId