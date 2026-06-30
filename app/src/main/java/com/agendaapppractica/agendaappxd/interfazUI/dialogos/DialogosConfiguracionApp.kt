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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// Estructura para la selección de colores
data class ItemColorTema(val nombre: String, val colorVisual: Color)

// --- DIÁLOGO: EDITAR PERFIL COMPLETO ---
@Composable
fun DialogoEditarPerfil(
    nombreActual: String,
    apellidoActual: String,
    telefonoActual: String,
    fechaActual: String,
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

    // 🛠️ Guardamos internamente solo los números limpios de la fecha
    var tempFechaNumeros by remember { mutableStateOf(fechaActual.filter { it.isDigit() }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Editar perfil completo") },
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

                OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = tempApellido, onValueChange = { tempApellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 🎭 Campo de fecha corregido con máscara automatizada y bloqueo estricto a 8 dígitos
                OutlinedTextField(
                    value = tempFechaNumeros,
                    onValueChange = { entrada ->
                        val soloNumeros = entrada.filter { it.isDigit() }
                        if (soloNumeros.length <= 8) {
                            tempFechaNumeros = soloNumeros
                        }
                    },
                    label = { Text("Fecha de nacimiento") },
                    placeholder = { Text("DD/MM/AAAA") },
                    visualTransformation = MascaraFechaTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = correoUsuario, onValueChange = {}, label = { Text("Correo electrónico") }, enabled = false, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                auth.currentUser?.uid?.let { uid ->
                    // Reconstruimos la fecha con sus barras correspondientes antes de guardarla en Firestore
                    val fechaFormateada = buildString {
                        if (tempFechaNumeros.length >= 2) append(tempFechaNumeros.substring(0, 2)).append("/")
                        if (tempFechaNumeros.length >= 4) append(tempFechaNumeros.substring(2, 4)).append("/")
                        if (tempFechaNumeros.length > 4) append(tempFechaNumeros.substring(4))
                    }.removeSuffix("/") // Remueve barras sobrantes si está incompleta

                    val data = mapOf(
                        "nombre" to tempNombre,
                        "apellido" to tempApellido,
                        "telefono" to tempTelefono,
                        "fechaNacimiento" to fechaFormateada,
                        "email" to correoUsuario,
                        "fotoPerfilUrl" to (fotoPerfilUri?.toString() ?: "")
                    )
                    db.collection("usuarios").document(uid).set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            onGuardarExitoso(tempNombre, tempApellido, tempTelefono, fechaFormateada)
                            Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                        }
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// --- DIÁLOGO: PERMISOS DE LA APP ---
@Composable
fun DialogoPermisosApp(
    permisoConcedidoEstado: Boolean,
    permisoNotifConcedidoEstado: Boolean,
    permisoCamaraConcedidoEstado: Boolean, // 🆕 Estado dinámico de la cámara
    solicitarMultimedia: () -> Unit,
    solicitarNotificaciones: () -> Unit,
    solicitarCamara: () -> Unit,           // 🆕 Callback para pedir permiso de cámara
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Permisos Requeridos", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Para asegurar la total funcionalidad de la Agenda, gestiona los accesos del sistema desde aquí:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))

                // Tarjeta 1: Acceso Multimedia
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Acceso Multimedia", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = if (permisoConcedidoEstado) "Permiso Concedido" else "Acceso Requerido", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (!permisoConcedidoEstado) {
                            TextButton(onClick = solicitarMultimedia) { Text("Conceder", fontWeight = FontWeight.Bold) }
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        }
                    }
                }

                // Tarjeta 2: Notificaciones
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Notificaciones", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = if (permisoNotifConcedidoEstado) "Alertas activas" else "Alertas desactivadas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (!permisoNotifConcedidoEstado && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            TextButton(onClick = solicitarNotificaciones) { Text("Permitir", fontWeight = FontWeight.Bold) }
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        }
                    }
                }

                // Tarjeta 3: Cámara (Añadida para el nuevo lector de documentos)
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Cámara e Impresión", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = if (permisoCamaraConcedidoEstado) "Escáner listo" else "Requerido para escáner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (!permisoCamaraConcedidoEstado) {
                            TextButton(onClick = solicitarCamara) { Text("Habilitar", fontWeight = FontWeight.Bold) }
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Aceptar") } }
    )
}

// --- DIÁLOGO: SEGURIDAD ---
@Composable
fun DialogoSeguridadApp(
    correoUsuario: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var correoConfirmacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Restablecer Contraseña") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Por motivos de seguridad, te enviaremos un correo con un enlace seguro para definir tu nueva contraseña.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(value = correoConfirmacion, onValueChange = { correoConfirmacion = it }, label = { Text("Confirma tu correo electrónico") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (correoConfirmacion.trim() == correoUsuario) {
                    auth.sendPasswordResetEmail(correoConfirmacion.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Correo enviado con éxito", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "El correo no coincide con tu sesión", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Enviar Enlace") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// --- DIÁLOGO: PERSONALIZACIÓN ---
@Composable
fun DialogoPersonalizacionApp(
    modoOscuroActivo: Boolean,
    onModoOscuroCambiado: (Boolean) -> Unit,
    colorTemaActual: String,
    onColorTemaCambiado: (String) -> Unit,
    listaTemas: List<ItemColorTema>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Aspecto de la aplicación") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = if (modoOscuroActivo) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Modo Oscuro")
                    }
                    Switch(checked = modoOscuroActivo, onCheckedChange = onModoOscuroCambiado)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Color de acento", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(listaTemas) { tema ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(tema.colorVisual)
                                .border(
                                    width = if (colorTemaActual == tema.nombre) 3.dp else 0.dp,
                                    color = if (colorTemaActual == tema.nombre) MaterialTheme.colorScheme.outline else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onColorTemaCambiado(tema.nombre) }
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Aceptar") } }
    )
}

// =========================================================
// 🎭 MÁSCARA AUTOMÁTICA PARA ENTRADA DE FECHAS (DD/MM/AAAA)
// =========================================================
class MascaraFechaTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val inputOriginal = text.text
        val out = StringBuilder()

        for (i in inputOriginal.indices) {
            out.append(inputOriginal[i])
            if (i == 1 || i == 3) {
                out.append("/")
            }
        }

        val traductorDeOffsets = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }

        return TransformedText(AnnotatedString(out.toString()), traductorDeOffsets)
    }
}