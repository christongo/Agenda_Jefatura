package com.agendaapppractica.agendaappxd.interfazUI.dialogos

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    var nombre by remember { mutableStateOf(nombreActual) }
    var apellido by remember { mutableStateOf(apellidoActual) }
    var telefono by remember { mutableStateOf(telefonoActual) }
    var fecha by remember { mutableStateOf(fechaActual) }

    val esTelefonoValido = telefono.isBlank() || (telefono.all { it.isDigit() } && telefono.length in 7..15)
    val puedeGuardar = nombre.isNotBlank() && apellido.isNotBlank() && esTelefonoValido

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { intentarCambiarFoto() },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfilUri != null) {
                        AsyncImage(
                            model = fotoPerfilUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            tint = Color.White
                        )
                    }
                }

                Text(correoUsuario, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )


                OutlinedTextField(
                    value = telefono,
                    onValueChange = { input ->

                        if (input.all { it.isDigit() }) { telefono = input }
                    },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !esTelefonoValido,
                    supportingText = {
                        if (!esTelefonoValido) {
                            Text("Número no válido (Debe tener entre 7 y 15 dígitos)", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = puedeGuardar,
                onClick = {
                    uid?.let {
                        val datos = mapOf(
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "telefono" to telefono
                        )
                        db.collection("usuarios").document(it).update(datos)
                            .addOnSuccessListener {
                                onGuardarExitoso(nombre, apellido, telefono, fecha)
                            }
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}