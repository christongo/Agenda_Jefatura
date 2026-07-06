package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.agendaapppractica.agendaappxd.model.Tarea
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardProductividad(
    tareasPorGrupo: Map<String, List<Tarea>>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rendimiento de mis Grupos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Productividad según tareas completadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ver Análisis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (tareasPorGrupo.isEmpty()) {
                    Text(
                        text = "No hay tareas registradas en tus grupos activos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    tareasPorGrupo.forEach { (grupoIdentificador, tareas) ->
                        val tareasSemana = filtrarTareasPorTiempo(tareas, 0)
                        val completadasSemana = tareasSemana.filter { it.completada == true }.size
                        val totalSemana = tareasSemana.size

                        val progreso = if (totalSemana > 0) completadasSemana.toFloat() / totalSemana.toFloat() else 0f

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = grupoIdentificador,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$completadasSemana/$totalSemana completadas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progreso },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoEstadisticasCompleto(
    tareasPorGrupo: Map<String, List<Tarea>>,
    onDismiss: () -> Unit
) {
    var tabSeleccionada by remember { mutableStateOf(0) }
    val titulosTabs = listOf("Semana", "Mes", "Año")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Productividad grupal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    titulosTabs.forEachIndexed { indice, titulo ->
                        Tab(
                            selected = tabSeleccionada == indice,
                            onClick = { tabSeleccionada = indice },
                            text = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tareasPorGrupo.isEmpty()) {
                        item {
                            Text(
                                text = "Aún no tienes tareas asignadas a tus grupos corporativos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(tareasPorGrupo.entries.toList()) { entry ->
                            val nombreOIdGrupo = entry.key
                            val listaTareasDelGrupo = entry.value

                            val tareasFiltradas = filtrarTareasPorTiempo(listaTareasDelGrupo, tabSeleccionada)
                            val completadas = tareasFiltradas.filter { it.completada == true }.size

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = nombreOIdGrupo,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "• Eventos programados: ${tareasFiltradas.size}", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "• Logrados con éxito: $completadas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (completadas == tareasFiltradas.size && tareasFiltradas.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


fun filtrarTareasPorTiempo(tareas: List<Tarea>, tabIndex: Int): List<Tarea> {
    val ahora = Calendar.getInstance()
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return tareas.filter { tarea ->
        try {
            if (tarea.fecha.isEmpty()) return@filter false

            val fechaTareaDate = formato.parse(tarea.fecha) ?: return@filter false
            val calTarea = Calendar.getInstance().apply { time = fechaTareaDate }

            when (tabIndex) {
                0 -> { // Semana del año actual
                    calTarea.get(Calendar.WEEK_OF_YEAR) == ahora.get(Calendar.WEEK_OF_YEAR) &&
                            calTarea.get(Calendar.YEAR) == ahora.get(Calendar.YEAR)
                }
                1 -> { // Mes del año actual
                    calTarea.get(Calendar.MONTH) == ahora.get(Calendar.MONTH) &&
                            calTarea.get(Calendar.YEAR) == ahora.get(Calendar.YEAR)
                }
                2 -> { // Año actual
                    calTarea.get(Calendar.YEAR) == ahora.get(Calendar.YEAR)
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}