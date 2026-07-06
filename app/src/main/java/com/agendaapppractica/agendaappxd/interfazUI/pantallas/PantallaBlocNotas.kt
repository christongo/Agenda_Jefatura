package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class NotaModel(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val usuarioId: String = "" ,
    val esChecklist: Boolean = false,
    val completada: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBlocNotas(
    navController: NavController,
    onAbrirAjustes: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val usuarioActual = auth.currentUser

    val listaNotas = remember { mutableStateListOf<NotaModel>() }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(usuarioActual?.uid) {
        if (usuarioActual != null) {
            db.collection("notas")
                .whereEqualTo("usuarioId", usuarioActual.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        cargando = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        listaNotas.clear()
                        for (doc in snapshot.documents) {
                            val nota = doc.toObject(NotaModel::class.java)?.copy(id = doc.id)
                            if (nota != null) {
                                listaNotas.add(nota)
                            }
                        }
                    }
                    cargando = false
                }
        } else {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bloc de Notas", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver al inicio")
                    }
                },
                actions = {
                    IconButton(onClick = { onAbrirAjustes() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("crear_nota/nueva") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Nota")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tus Notas en la Nube",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (listaNotas.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No hay notas guardadas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tus notas se sincronizarán con tu correo.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(listaNotas) { nota ->
                        var menuExpandido by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("crear_nota/${nota.id}") },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (nota.esChecklist) {
                                    Checkbox(
                                        checked = nota.completada,
                                        onCheckedChange = { nuevoEstado ->
                                            db.collection("notas")
                                                .document(nota.id)
                                                .update("completada", nuevoEstado)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nota.titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        textDecoration = if (nota.esChecklist && nota.completada) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (nota.esChecklist && nota.completada) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (nota.contenido.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = nota.contenido,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (nota.esChecklist && nota.completada) MaterialTheme.colorScheme.outline.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            textDecoration = if (nota.esChecklist && nota.completada) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }
                                }

                                Box {
                                    IconButton(onClick = { menuExpandido = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                    }
                                    DropdownMenu(
                                        expanded = menuExpandido,
                                        onDismissRequest = { menuExpandido = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Compartir") },
                                            onClick = {
                                                menuExpandido = false
                                                compartirNota(context, nota.titulo, nota.contenido)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                menuExpandido = false
                                                db.collection("notas").document(nota.id).delete()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun compartirNota(context: Context, titulo: String, contenido: String) {
    val texto = if (titulo.isNotBlank()) "*$titulo*\n\n$contenido" else contenido
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir vía:"))
}