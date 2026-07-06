package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.agendaapppractica.agendaappxd.interfazUI.componentes.*
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoCrearPublicacionGrupo
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoEditarBiografiaGrupo
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.model.MiembroUsuario
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleGrupo(
    grupo: Grupo,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val firestore = remember { FirestoreManager() }
    val miUid = FirebaseAuth.getInstance().currentUser?.uid

    val prefs = remember { context.getSharedPreferences("TutorialPrefs", Context.MODE_PRIVATE) }

    var nombreGrupoActual by remember { mutableStateOf(grupo.nombre) }
    var descripcionGrupoActual by remember { mutableStateOf(grupo.descripcion) }

    var tabSeleccionada by remember { mutableStateOf(0) }
    var subTabPublicaciones by remember { mutableStateOf(0) }

    var listaMiembros by remember { mutableStateOf<List<MiembroUsuario>>(emptyList()) }
    var listaSolicitudes by remember { mutableStateOf<List<MiembroUsuario>>(emptyList()) }
    var eventosActivos by remember { mutableStateOf<List<Tarea>>(emptyList()) }

    var listaAdministradores by remember { mutableStateOf<List<String>>(grupo.administradores) }

    var cargandoMiembros by remember { mutableStateOf(true) }
    var cargandoSolicitudes by remember { mutableStateOf(true) }

    var showDialogCrearPublicacion by remember { mutableStateOf(false) }
    var tipoPublicacionSeleccionada by remember { mutableStateOf("Evento") }

    var mostrarDialogoEdicion by remember { mutableStateOf(false) }
    var mostrarTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(grupo.id) {
        val yaVisto = prefs.getBoolean("ocultar_tutorial_grupo", false)
        if (!yaVisto) {
            mostrarTutorial = true
        }

        firestore.escucharEventosGrupo(grupo.id) { eventos -> eventosActivos = eventos }

        firestore.escucharDatosGrupo(grupo.id) { grupoActualizado ->
            grupoActualizado?.let { g ->
                listaAdministradores = g.administradores

                val miembrosInfo = mutableListOf<MiembroUsuario>()
                var miembrosCargados = 0
                if (g.miembros.isEmpty()) {
                    listaMiembros = emptyList()
                    cargandoMiembros = false
                } else {
                    g.miembros.forEach { uid ->
                        firestore.obtenerDatosUsuario(uid) { info ->
                            miembrosInfo.add(info)
                            miembrosCargados++
                            if (miembrosCargados == g.miembros.size) {
                                listaMiembros = miembrosInfo.toList()
                                cargandoMiembros = false
                            }
                        }
                    }
                }
            }
        }

        firestore.escucharSolicitudesGrupo(grupo.id) { uidsSolicitantes ->
            if (uidsSolicitantes.isEmpty()) {
                listaSolicitudes = emptyList()
                cargandoSolicitudes = false
            } else {
                val solicitantesInfo = mutableListOf<MiembroUsuario>()
                var solicitantesCargados = 0
                uidsSolicitantes.forEach { uid ->
                    firestore.obtenerDatosUsuario(uid) { info ->
                        solicitantesInfo.add(info)
                        solicitantesCargados++
                        if (solicitantesCargados == uidsSolicitantes.size) {
                            listaSolicitudes = solicitantesInfo.toList()
                            cargandoSolicitudes = false
                        }
                    }
                }
            }
        }
    }

    val esCreador = grupo.creadorId == miUid

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TopAppBarDetalleGrupo(
                        nombreGrupo = nombreGrupoActual,
                        pestañaSeleccionada = tabSeleccionada,
                        esAdminOCreador = esCreador || listaAdministradores.contains(miUid),
                        esCreador = esCreador,
                        grupoId = grupo.id,
                        onVolver = onVolver,
                        onRefrescar = { },
                        onEditarInfoClick = { mostrarDialogoEdicion = true },
                        onEliminarGrupoClick = { }
                    )
                }

                IconButton(
                    onClick = { mostrarTutorial = true },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Ver guía de la pantalla",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        floatingActionButton = {
            if (tabSeleccionada == 2 && (esCreador || listaAdministradores.contains(miUid))) {
                FloatingActionButton(
                    onClick = {
                        tipoPublicacionSeleccionada = if (subTabPublicaciones == 0) "Evento" else "Anuncio"
                        showDialogCrearPublicacion = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Publicación")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = tabSeleccionada == 0, onClick = { tabSeleccionada = 0 }, text = { Text("Detalles", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Info, null) })
                Tab(selected = tabSeleccionada == 1, onClick = { tabSeleccionada = 1 }, text = { Text("Miembros", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.People, null) })
                Tab(selected = tabSeleccionada == 2, onClick = { tabSeleccionada = 2 }, text = { Text("Publicaciones", fontWeight = FontWeight.Bold) }, icon = { Icon(Icons.Default.Campaign, null) })
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (tabSeleccionada) {
                    0 -> SeccionDetalles(
                        grupo = grupo, nombreActual = nombreGrupoActual, descripcionActual = descripcionGrupoActual, esCreador = esCreador,
                        listaSolicitudes = listaSolicitudes, cargandoSolicitudes = cargandoSolicitudes,
                        onSolicitudProcesada = { uid, aceptada ->
                            if (aceptada) firestore.aceptarSolicitudDeUnion(grupo.id, uid) { if (it) Toast.makeText(context, "Miembro aceptado", Toast.LENGTH_SHORT).show() }
                            else firestore.rechazarSolicitudDeUnion(grupo.id, uid) { if (it) Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show() }
                        },
                        onFotoCambiada = { _ -> }
                    )
                    1 -> SeccionMiembros(
                        cargandoMiembros = cargandoMiembros,
                        listaMiembros = listaMiembros,
                        grupo = grupo,
                        administradoresLocales = listaAdministradores,
                        onMiembroClick = { uid ->
                        },
                        onGestionarAdmin = { uid, esAdmin ->
                            if (esAdmin) {
                                firestore.quitarAdministradorGrupo(grupo.id, uid) { exitoso ->
                                    if (exitoso) Toast.makeText(context, "Rango removido con éxito", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                firestore.asignarAdministradorGrupo(grupo.id, uid) { exitoso ->
                                    if (exitoso) Toast.makeText(context, "Nuevo administrador asignado", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    2 -> SeccionPublicaciones(eventosActivos = eventosActivos, subPestañaPublicaciones = subTabPublicaciones, onSubPestañaCambiada = { subTabPublicaciones = it })
                }
            }
        }
    }

    if (showDialogCrearPublicacion) { DialogoCrearPublicacionGrupo(grupoId = grupo.id, nombreGrupo = grupo.nombre, tipoPublicacion = tipoPublicacionSeleccionada, onDismiss = { showDialogCrearPublicacion = false }) }
    if (mostrarDialogoEdicion) { DialogoEditarBiografiaGrupo(grupoId = grupo.id, nombreInicial = nombreGrupoActual, descripcionInicial = descripcionGrupoActual, onDismiss = { mostrarDialogoEdicion = false }, onCambiosGuardados = { n, d -> nombreGrupoActual = n; descripcionGrupoActual = d }) }

    if (mostrarTutorial) {
        DialogoTutorialPantalla(
            onDismiss = { mostrarTutorial = false },
            onNoMostrarMas = {
                prefs.edit().putBoolean("ocultar_tutorial_grupo", true).apply()
                mostrarTutorial = false
                Toast.makeText(context, "Tutorial desactivado en los arranques automáticos", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DialogoTutorialPantalla(
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
                            2 -> Icons.Default.People
                            else -> Icons.Default.Campaign
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pasoActual) {
                        1 -> "¡Te damos la bienvenida!"
                        2 -> "Gestiona tu equipo"
                        else -> "Mantente al día"
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
                        text = "Paso $pasoActual de $totalPasos",
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
                                text = "Sección de Detalles",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• Revisa el nombre, descripción y propósitos de esta comunidad.\n" +
                                        "• Si eres el creador, aquí aparecerán las alertas de nuevos usuarios que quieren unirse para que los aceptes o rechaces con un toque.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Panel de Miembros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• Mira la lista completa de las personas que integran el grupo.\n" +
                                        "• Gestiona rangos otorgando o removiendo permisos de administrador de forma rápida y segura.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Tablón de Publicaciones (Publicaciones)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• Eventos: Tareas agendadas con fechas límite visibles.\n" +
                                        "• Anuncios: Noticias o comunicados importantes.\n\n" +
                                        "Tip: ¡Usa el botón flotante de la esquina inferior (+) para crear contenido nuevo!",
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
                            text = if (pasoActual < totalPasos) "Siguiente" else "¡Entendido!",
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