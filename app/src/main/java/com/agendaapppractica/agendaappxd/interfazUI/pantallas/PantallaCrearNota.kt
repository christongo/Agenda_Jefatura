package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearNota(
    navController: NavController,
    notaId: String?,
    tipoTextura: String = "Ninguno",
    grosorLinea: Float = 1.5f
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val usuarioActual = auth.currentUser

    var tituloNota by remember { mutableStateOf("") }
    var contenidoNota by remember { mutableStateOf("") }

    var esChecklist by remember { mutableStateOf(false) }
    var completada by remember { mutableStateOf(false) }

    var guardandoNota by remember { mutableStateOf(false) }
    var cargandoNota by remember { mutableStateOf(notaId != null && notaId != "nueva") }
    var yaGuardadoManualmente by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(notaId) {
        if (notaId != null && notaId != "nueva") {
            db.collection("notas").document(notaId).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        tituloNota = doc.getString("titulo") ?: ""
                        contenidoNota = doc.getString("contenido") ?: ""
                        esChecklist = doc.getBoolean("esChecklist") ?: false
                        completada = doc.getBoolean("completada") ?: false
                    }
                    cargandoNota = false
                }
                .addOnFailureListener {
                    cargandoNota = false
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!yaGuardadoManualmente && (tituloNota.isNotBlank() || contenidoNota.isNotBlank()) && usuarioActual != null) {
                val datosNota = hashMapOf(
                    "titulo" to tituloNota.ifBlank { "Nota sin título" },
                    "contenido" to contenidoNota,
                    "usuarioId" to usuarioActual.uid,
                    "esChecklist" to esChecklist,
                    "completada" to completada
                )

                if (notaId != null && notaId != "nueva") {
                    db.collection("notas").document(notaId).set(datosNota)
                } else {
                    db.collection("notas").add(datosNota)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (notaId != null && notaId != "nueva") "Editar Nota" else "Nueva Nota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !guardandoNota) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if ((tituloNota.isNotBlank() || contenidoNota.isNotBlank()) && !guardandoNota && usuarioActual != null) {
                                guardandoNota = true
                                yaGuardadoManualmente = true

                                val datosNota = hashMapOf(
                                    "titulo" to tituloNota.ifBlank { "Sin título" },
                                    "contenido" to contenidoNota,
                                    "usuarioId" to usuarioActual.uid,
                                    "esChecklist" to esChecklist,
                                    "completada" to completada
                                )

                                val tareaFirestore = if (notaId != null && notaId != "nueva") {
                                    db.collection("notas").document(notaId).set(datosNota)
                                } else {
                                    db.collection("notas").add(datosNota)
                                }

                                tareaFirestore
                                    .addOnSuccessListener {
                                        scope.launch {
                                            Toast.makeText(context, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                                            delay(10)
                                            navController.popBackStack()
                                        }
                                    }
                                    .addOnFailureListener {
                                        guardandoNota = false
                                        yaGuardadoManualmente = false
                                        Toast.makeText(context, "Error al guardar en la nube", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        enabled = !guardandoNota && !cargandoNota
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        if (cargandoNota) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    FilterChip(
                        selected = esChecklist,
                        onClick = { esChecklist = !esChecklist },
                        label = { Text(if (esChecklist) "Modo: Lista Checklist" else "Modo: Nota Normal") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    // CORRECCIÓN: Quitamos el Canvas problemático y dejamos un flujo de diseño limpio
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        TextField(
                            value = tituloNota,
                            onValueChange = { if (!guardandoNota) tituloNota = it },
                            placeholder = {
                                Text(text = if (esChecklist) "Título de la lista..." else "Título de tu nota...", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            },
                            textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TextField(
                            value = contenidoNota,
                            onValueChange = { if (!guardandoNota) contenidoNota = it },
                            placeholder = { Text(if (esChecklist) "Escribe el elemento o tarea aquí..." else "Escribe algo aquí...") },
                            // CORRECCIÓN: Agregamos un lineHeight cómodo y limpio para la lectura y escritura
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}