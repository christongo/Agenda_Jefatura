package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.agendaapppractica.agendaappxd.interfazUI.modales.CalendarioChile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ItemColorTema(val nombre: String, val colorVisual: Color)

@Composable
fun DialogoEditarPerfil(
    nombreActual: String,
    apellidoActual: String,
    telefonoActual: String,
    fechaActual: String, // Esperado en formato "DD/MM/AAAA" o vacío
    correoUsuario: String,
    fotoPerfilUri: Uri?,
    intentarCambiarFoto: () -> Unit,
    onDismiss: () -> Unit,
    onGuardarExitoso: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    var tempNombre by remember { mutableStateOf(nombreActual) }
    var tempApellido by remember { mutableStateOf(apellidoActual) }
    var tempTelefono by remember { mutableStateOf(telefonoActual) }
    var tempFecha by remember { mutableStateOf(fechaActual) }

    // Control del estado del sub-diálogo del calendario
    var mostrarCalendario by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = "Editar perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoPerfilUri != null) {
                            AsyncImage(model = fotoPerfilUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp))
                        }
                    }
                    IconButton(
                        onClick = intentarCambiarFoto,
                        modifier = Modifier.size(30.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Elegir foto", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = tempApellido, onValueChange = { tempApellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tempTelefono,
                    onValueChange = { entrada ->
                        val filtrado = entrada.filter { it.isDigit() || it == '+' }
                        if (filtrado.length <= 15) tempTelefono = filtrado
                    },
                    label = { Text("Número de teléfono") },
                    placeholder = { Text("+56912345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Campo de fecha transformado a interactivo/solo lectura para evitar bugs de escritura manual
                OutlinedTextField(
                    value = tempFecha,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de nacimiento") },
                    placeholder = { Text("Selecciona tu fecha") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Abrir calendario",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { mostrarCalendario = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarCalendario = true },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = correoUsuario, onValueChange = {}, label = { Text("Correo electrónico") }, enabled = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                auth.currentUser?.uid?.let { uid ->
                    val data = mapOf(
                        "nombre" to tempNombre,
                        "apellido" to tempApellido,
                        "telefono" to tempTelefono,
                        "fechaNacimiento" to tempFecha,
                        "email" to correoUsuario,
                        "fotoPerfilUrl" to (fotoPerfilUri?.toString() ?: "")
                    )
                    db.collection("usuarios").document(uid).set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            onGuardarExitoso(tempNombre, tempApellido, tempTelefono, tempFecha)
                            Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                        }
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    // Diálogo flotante personalizado que contiene tu CalendarioChile adaptado
    if (mostrarCalendario) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaInicial = try {
            if (tempFecha.isNotBlank()) LocalDate.parse(tempFecha, formatter) else LocalDate.now().minusYears(20)
        } catch (_: Exception) {
            LocalDate.now().minusYears(20)
        }

        AlertDialog(
            onDismissRequest = { mostrarCalendario = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Selecciona tu Fecha", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                    // Invocamos tu componente pasándole estados vacíos de tareas/feriados ya que es para un perfil
                    CalendarioChileAdaptado(
                        fechaSeleccionada = fechaInicial,
                        onFechaSeleccionada = { fechaElegida ->
                            tempFecha = fechaElegida.format(formatter)
                            mostrarCalendario = false
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }
}

// Versión optimizada de tu componente para permitir navegar al pasado sin romper las restricciones de tareas futuras
@Composable
fun CalendarioChileAdaptado(
    fechaSeleccionada: LocalDate,
    onFechaSeleccionada: (LocalDate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Permitimos expandir el calendario 100 años al pasado para cubrir cualquier rango de nacimiento lógico
    val startMonth = remember { java.time.YearMonth.now().minusMonths(1200) }
    val endMonth = remember { java.time.YearMonth.now() }
    val currentMonth = remember(fechaSeleccionada) { java.time.YearMonth.from(fechaSeleccionada) }

    val calendarState = com.kizitonwose.calendar.compose.rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = java.time.DayOfWeek.MONDAY
    )

    LaunchedEffect(fechaSeleccionada) {
        calendarState.scrollToMonth(java.time.YearMonth.from(fechaSeleccionada))
    }

    val mesVisible = calendarState.firstVisibleMonth.yearMonth
    val nombreMes = mesVisible.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "CL"))
        .replaceFirstChar { it.uppercase() }
    val anio = mesVisible.year

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val mesAnterior = mesVisible.minusMonths(1)
                    if (mesAnterior >= startMonth) {
                        coroutineScope.launch { calendarState.animateScrollToMonth(mesAnterior) }
                    }
                },
                enabled = mesVisible > startMonth
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            Text(text = "$nombreMes $anio", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp))

            IconButton(
                onClick = {
                    val mesSiguiente = mesVisible.plusMonths(1)
                    if (mesSiguiente <= endMonth) {
                        coroutineScope.launch { calendarState.animateScrollToMonth(mesSiguiente) }
                    }
                },
                enabled = mesVisible < endMonth
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        com.kizitonwose.calendar.compose.HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                val fecha = day.date
                val hoy = fecha == LocalDate.now()
                // Corrección del bug: Bloqueamos días del futuro, permitimos días del pasado
                val esDiaFuturo = fecha.isAfter(LocalDate.now())
                val seleccionado = fecha == fechaSeleccionada

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(40.dp)
                        .background(
                            if (seleccionado) MaterialTheme.colorScheme.primary else if (hoy) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            CircleShape
                        )
                        .clickable(enabled = !esDiaFuturo) { onFechaSeleccionada(fecha) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fecha.dayOfMonth.toString(),
                        color = when {
                            seleccionado -> MaterialTheme.colorScheme.onPrimary
                            esDiaFuturo -> Color.Gray.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }
}

@Composable
fun DialogoSeguridadApp(email: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seguridad") },
        text = { Text("Opciones de seguridad para $email") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } }
    )
}

@Composable
fun DialogoPermisosApp(
    p1: Boolean, p2: Boolean, p3: Boolean,
    onP1: () -> Unit, onP2: () -> Unit, onP3: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permisos de la Aplicación") },
        text = { Text("Gestión de permisos y accesos") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } }
    )
}

// Los demás diálogos de tu archivo permanecen exactamente iguales abajo...