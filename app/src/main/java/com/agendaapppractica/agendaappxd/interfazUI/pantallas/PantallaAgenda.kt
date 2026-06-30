package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogNuevaTarea
import com.agendaapppractica.agendaappxd.interfazUI.modales.CalendarioChile
import com.agendaapppractica.agendaappxd.model.Tarea
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import com.agendaapppractica.agendaappxd.networkData.ProgramadorRecordatorios
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import com.agendaapppractica.agendaappxd.model.FeriadoChile
import com.agendaapppractica.agendaappxd.networkData.RetrofitClient
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgenda() {

    val firestore = remember { FirestoreManager() }
    val context = LocalContext.current
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val miUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var fechaSeleccionadaCalendar by remember { mutableStateOf(LocalDate.now()) }
    var fechaSeleccionada by remember { mutableStateOf(formatter.format(Date()))}
    var tareasDia by remember { mutableStateOf<List<Tarea>>(emptyList()) }
    var feriados by remember { mutableStateOf<List<FeriadoChile>>(emptyList()) }

    var misGruposIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var misGruposCompletos by remember { mutableStateOf<List<com.agendaapppractica.agendaappxd.model.Grupo>>(emptyList()) }

    var mostrarDialog by remember { mutableStateOf(false) }

    val feriado = feriados.firstOrNull {
        try {
            LocalDate.parse(it.date) == fechaSeleccionadaCalendar
        } catch (_: Exception) {
            false
        }
    }

    LaunchedEffect(fechaSeleccionada) {
        firestore.escucharMisGrupos { grupos ->
            misGruposCompletos = grupos
            misGruposIds = grupos.map { it.id }

            firestore.escucharTareasDelDia(fechaSeleccionada, misGruposIds) { listaTareas ->
                tareasDia = listaTareas
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
        Text(
            text = " Calendario Chileno ",
            style = MaterialTheme.typography.headlineMedium
        )

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
            modifier = Modifier.fillMaxWidth()
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
                        text = "🇨🇱 ${feriado.title}",
                        style = MaterialTheme.typography.titleLarge
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
                        text = " Resumen del día",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Fecha: $fechaSeleccionada", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Mis eventos personales: ${tareasDia.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
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
}