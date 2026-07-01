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
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    var mostrarDialogoEventos by remember { mutableStateOf(false) }
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

        firestoreManager.escucharMisGrupos { grupos -> misGruposIds = grupos.map { it.id } }
        usuario?.uid?.let { uid -> firestoreManager.obtenerUsuario(uid) { nombre -> nombreUsuario = nombre } }
    }

    LaunchedEffect(misGruposIds) {
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        firestoreManager.escucharTareasDelDia(hoy, misGruposIds) { tareas -> listaEventosHoy = tareas }
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

            TarjetaResumenAgenda(
                cantidadEventos = listaEventosHoy.size,
                onClick = { mostrarDialogoEventos = true }
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

                    BotonPanelHerramientas(
                        titulo = "Documentos",
                        subtitulo = "Escanear PDF",
                        icono = Icons.Default.DocumentScanner,
                        colorBase = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate("documentos")
                    }
                }
            }
        }
    }

    if (mostrarDialogoEventos) {
        DialogoEventosHoy(listaEventosHoy = listaEventosHoy, onDismiss = { mostrarDialogoEventos = false })
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
                            1 -> Icons.Default.Dashboard
                            2 -> Icons.Default.Group
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pasoActual) {
                        1 -> "Panel Principal"
                        2 -> "Gestión de Comunidades"
                        else -> "Módulos Adicionales"
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 130.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    when (pasoActual) {
                        1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Resumen Diario de Actividades",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Encabezado Informativo: Muestra la fecha actual y cuenta las responsabilidades pendientes asignadas para hoy.\n" +
                                        "* Tarjeta Central: Funciona como un acceso rápido directo; púlsa el botón informativo para desplegar el cuadro detallado de tus eventos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Conexión Institucional",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Unirse a Grupo: Permite ingresar una credencial alfanumérica compartida para integrarte a equipos existentes.\n" +
                                        "* Mis Grupos: Te redirige al listado completo de espacios y canales corporativos en los que participas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Utilidades Incorporadas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Bloc de Notas: Espacio seguro para redactar y almacenar apuntes temporales o minutas rápidamente.\n" +
                                        "* Escáner de Documentos: Herramienta inteligente para digitalizar archivos físicos y estructurarlos en formato PDF.",
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
                            text = if (pasoActual < totalPasos) "Siguiente" else "Comenzar",
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