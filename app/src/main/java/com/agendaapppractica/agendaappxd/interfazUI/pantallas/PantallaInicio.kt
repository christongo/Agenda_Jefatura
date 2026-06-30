package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var nombreUsuario by remember { mutableStateOf("Usuario") }
    var listaEventosHoy by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var misGruposIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var mostrarDialogoEventos by remember { mutableStateOf(false) }

    val fechaHoyTexto = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CL")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    // 🛠️ FIJADO: Cambiado de "groups" a "grupos" para hacer match con MainActivity
    val navegarAPestanaGrupos = {
        navController.navigate("grupos") {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(Unit) {
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
            SeccionEncabezado(
                nombreUsuario = nombreUsuario,
                fechaTexto = fechaHoyTexto,
                cantidadEventos = listaEventosHoy.size,
                onBadgeClick = { mostrarDialogoEventos = true }
            )

            TarjetaResumenAgenda(
                cantidadEventos = listaEventosHoy.size,
                onClick = { mostrarDialogoEventos = true }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Herramientas y Grupos",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonPanelHerramientas("Unirse a Grupo", "Usar código", Icons.Default.GroupAdd, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { navegarAPestanaGrupos() }
                    BotonPanelHerramientas("Mis Grupos", "Ver compartidos", Icons.Default.Group, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { navegarAPestanaGrupos() }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonPanelHerramientas("Notas", "Apuntes rápidos", Icons.Default.Description, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { navController.navigate("bloc_notas") }

                    // 📸 🛠️ FIJADO: Se cambió 'colorIcono' por 'colorBase' para que coincida con tu componente
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
}