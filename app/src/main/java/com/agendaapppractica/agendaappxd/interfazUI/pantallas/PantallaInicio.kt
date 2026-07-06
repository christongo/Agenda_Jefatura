package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.agendaapppractica.agendaappxd.interfazUI.pantallas.componentes.*
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import com.agendaapppractica.agendaappxd.networkData.LectorQRManager
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    navController: NavController
) {
    val usuario = FirebaseAuth.getInstance().currentUser
    val firestoreManager = remember { FirestoreManager() }
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("InicioTutorialPrefs", Context.MODE_PRIVATE) }

    var nombreUsuario by remember { mutableStateOf("Usuario") }
    var listaEventosHoy by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var misGruposIds by remember { mutableStateOf<List<String>>(emptyList()) }

    var mapaNombresGrupos by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var mapaProductividadReal by remember { mutableStateOf<Map<String, List<Tarea>>>(emptyMap()) }

    var mostrarDialogoEventos by remember { mutableStateOf(false) }
    var mostrarDialogoEstadisticas by remember { mutableStateOf(false) }
    var mostrarTutorial by remember { mutableStateOf(false) }

    val fechaHoyTexto = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CL")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    val navegarAPestanaGrupos = {
        navController.navigate("grupos") {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(Unit) {
        val yaVisto = prefs.getBoolean("ocultar_tutorial_inicio", false)
        if (!yaVisto) {
            mostrarTutorial = true
        }

        firestoreManager.escucharMisGrupos { listaGrupos ->
            misGruposIds = listaGrupos.map { it.id }
            mapaNombresGrupos = listaGrupos.associate { it.id to (it.nombre ?: "Grupo de Trabajo") }
        }
        usuario?.uid?.let { uid -> firestoreManager.obtenerUsuario(uid) { nombre -> nombreUsuario = nombre } }
    }

    LaunchedEffect(misGruposIds) {
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        firestoreManager.escucharTareasDelDia(hoy, misGruposIds) { tareas -> listaEventosHoy = tareas }
    }

    LaunchedEffect(misGruposIds, mapaNombresGrupos) {
        if (misGruposIds.isNotEmpty()) {
            firestoreManager.escucharTareasMisGrupos(misGruposIds) { listaTareasObtenidas ->
                mapaProductividadReal = listaTareasObtenidas.groupBy { tarea ->
                    mapaNombresGrupos[tarea.grupoId] ?: "Grupo de Trabajo"
                }
            }
        } else {
            mapaProductividadReal = emptyMap()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SeccionEncabezado(
                        nombreUsuario = nombreUsuario,
                        fechaTexto = fechaHoyTexto,
                        cantidadEventos = listaEventosHoy.size,
                        onBadgeClick = { mostrarDialogoEventos = true }
                    )
                }
                IconButton(onClick = { mostrarTutorial = true }) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Ver guía de inicio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            DashboardProductividad(
                tareasPorGrupo = mapaProductividadReal,
                onClick = { mostrarDialogoEstadisticas = true }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Herramientas y Grupos",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonPanelHerramientas("Unirse a Grupo", "Usar código", Icons.Default.GroupAdd, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { navegarAPestanaGrupos() }
                    BotonPanelHerramientas("Mis Grupos", "Ver compartidos", Icons.Default.Group, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { navegarAPestanaGrupos() }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonPanelHerramientas("Notas", "Apuntes rápidos", Icons.Default.Description, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { navController.navigate("bloc_notas") }
                    BotonPanelHerramientas("Documentos", "Escanear PDF", Icons.Default.DocumentScanner, MaterialTheme.colorScheme.error, Modifier.weight(1f)) { navController.navigate("documentos") }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    BotonPanelHerramientas(
                        titulo = "Escáner QR Corporativo",
                        subtitulo = "Importar contactos, credenciales o eventos de inmediato",
                        icono = Icons.Default.QrCodeScanner,
                        colorBase = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LectorQRManager.iniciarEscaneoFuncional(context) { resultadoContenido ->
                            Toast.makeText(context, "Contenido QR Detectado: $resultadoContenido", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoEventos) {
        DialogoEventosHoy(listaEventosHoy = listaEventosHoy, onDismiss = { mostrarDialogoEventos = false })
    }

    if (mostrarDialogoEstadisticas) {
        DialogoEstadisticasCompleto(tareasPorGrupo = mapaProductividadReal, onDismiss = { mostrarDialogoEstadisticas = false })
    }

    if (mostrarTutorial) {
        DialogoTutorialInicio(
            onDismiss = { mostrarTutorial = false },
            onNoMostrarMas = {
                prefs.edit().putBoolean("ocultar_tutorial_inicio", true).apply()
                mostrarTutorial = false
                Toast.makeText(context, "Asistente de inicio desactivado", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DialogoTutorialInicio(
    onDismiss: () -> Unit,
    onNoMostrarMas: () -> Unit
) {
    var pasoActual by remember { mutableStateOf(1) }
    val totalPasos = 3
    val progresoAnimado = pasoActual.toFloat() / totalPasos.toFloat()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(56.dp).background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (pasoActual) {
                            1 -> Icons.Default.Dashboard
                            2 -> Icons.Default.Group
                            else -> Icons.Default.QrCodeScanner
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pasoActual) {
                        1 -> "Métricas de Rendimiento"
                        2 -> "Gestión de Comunidades"
                        else -> "Módulos de Productividad"
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
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sección $pasoActual de $totalPasos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    when (pasoActual) {
                        1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Dashboard Analítico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("* Gráfico Semanal: Analiza de forma visual qué días registraste mayor actividad o completaste más tareas.\n* Acceso Rápido: Puedes pulsar directamente sobre el gráfico para abrir el panel con el desglose exacto de tus eventos agendados.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Conexión Institucional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("* Unirse a Grupo: Permite ingresar una credencial alfanumérica compartida para integrarte a equipos existentes.\n* Mis Grupos: Te redirige al listado completo de espacios y canales corporativos en los que participas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Utilidades de Conectividad Rápida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("* Bloc de Notas: Espacio seguro para guardar apuntes rápidos y minutas temporales.\n* Escáner PDF: Herramienta inteligente para digitalizar archivos físicos.\n* Escáner QR: Lector instantáneo para importar eventos corporativos o tarjetas de contacto de colegas sin digitar nada.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text(text = "No volver a mostrar", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    }

                    Button(
                        onClick = { if (pasoActual < totalPasos) pasoActual++ else onDismiss() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = if (pasoActual < totalPasos) "Siguiente" else "Comenzar", fontWeight = FontWeight.Bold)
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