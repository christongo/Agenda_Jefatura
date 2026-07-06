package com.agendaapppractica.agendaappxd.interfazUI.modales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.model.FeriadoChile
import com.agendaapppractica.agendaappxd.model.Tarea
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarioChile(
    fechaSeleccionada: LocalDate,
    tareas: List<Tarea>,
    feriados: List<FeriadoChile>,
    onFechaSeleccionada: (LocalDate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val startMonth = YearMonth.now().minusMonths(12)
    val endMonth = YearMonth.now().plusMonths(24)
    val currentMonth = remember(fechaSeleccionada) { YearMonth.from(fechaSeleccionada) }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    LaunchedEffect(fechaSeleccionada) {
        calendarState.scrollToMonth(YearMonth.from(fechaSeleccionada))
    }

    val mesVisible = calendarState.firstVisibleMonth.yearMonth
    val nombreMes = mesVisible.month.getDisplayName(TextStyle.FULL, Locale("es", "CL"))
        .replaceFirstChar { it.uppercase() }
    val anio = mesVisible.year

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val mesAnterior = mesVisible.minusMonths(1)
                    if (mesAnterior >= startMonth) {
                        coroutineScope.launch {
                            calendarState.animateScrollToMonth(mesAnterior)
                        }
                    }
                },
                enabled = mesVisible > startMonth
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Mes anterior",
                    tint = if (mesVisible > startMonth) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "$nombreMes $anio",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            IconButton(
                onClick = {
                    val mesSiguiente = mesVisible.plusMonths(1)
                    if (mesSiguiente <= endMonth) {
                        coroutineScope.launch {
                            calendarState.animateScrollToMonth(mesSiguiente)
                        }
                    }
                },
                enabled = mesVisible < endMonth
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Mes siguiente",
                    tint = if (mesVisible < endMonth) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                val fecha = day.date
                val hoy = fecha == LocalDate.now()
                val esDiaPasado = fecha.isBefore(LocalDate.now())

                val tieneEvento = tareas.any {
                    try {
                        val partes = it.fecha.split("/")
                        LocalDate.of(partes[2].toInt(), partes[1].toInt(), partes[0].toInt()) == fecha
                    } catch (_: Exception) {
                        false
                    }
                }

                val esFeriado = feriados.any {
                    try {
                        LocalDate.parse(it.date) == fecha
                    } catch (_: Exception) {
                        false
                    }
                }

                val seleccionado = fecha == fechaSeleccionada

                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(46.dp)
                        .then(
                            if (tieneEvento && !seleccionado) {
                                Modifier.border(2.dp, Color(0xFFD32F2F), CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            when {
                                seleccionado -> MaterialTheme.colorScheme.primary
                                esFeriado -> Color(0xFFFFCDD2)
                                hoy -> Color(0xFFE3F2FD)
                                else -> Color.Transparent
                            },
                            CircleShape
                        )
                        .clickable(enabled = !esDiaPasado) {
                            onFechaSeleccionada(fecha)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = fecha.dayOfMonth.toString(),
                            color = when {
                                seleccionado -> MaterialTheme.colorScheme.onPrimary
                                esDiaPasado -> Color.Gray.copy(alpha = 0.5f)
                                esFeriado -> Color(0xB7B71C1C)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (tieneEvento) FontWeight.Bold else FontWeight.Normal
                        )

                        if (tieneEvento) {
                            Spacer(Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (seleccionado) MaterialTheme.colorScheme.onPrimary else Color(0xFFD32F2F),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        )
    }
}