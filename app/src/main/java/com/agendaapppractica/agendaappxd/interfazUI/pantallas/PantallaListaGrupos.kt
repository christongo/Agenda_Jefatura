package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGruposLista(
    onVolver: () -> Unit
) {
    val firestore = remember { FirestoreManager() }
    var grupos by remember { mutableStateOf<List<Grupo>>(emptyList()) }
    var grupoSeleccionado by remember { mutableStateOf<Grupo?>(null) }

    var grupoAEliminar by remember { mutableStateOf<Grupo?>(null) }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firestore.escucharMisGrupos { grupos = it }
    }

    if (grupoSeleccionado != null) {
        PantallaDetalleGrupo(
            grupo = grupoSeleccionado!!,
            onVolver = { grupoSeleccionado = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mis Comunidades",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (grupos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Espacio vacío.\nAún no perteneces a ningún grupo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    // 🌟 CAMBIO CLAVE: Usamos una cuadrícula moderna de 2 columnas en lugar de una lista vertical rígida
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        items(grupos) { grupo ->
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            val esCreador = grupo.creadorId == uid
                            var menuAbierto by remember { mutableStateOf(false) }

                            // Tarjeta minimalista tipo "Card-Widget"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { grupoSeleccionado = grupo }
                                    .padding(14.dp)
                            ) {
                                // Tres puntos superiores integrados sutilmente
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    IconButton(
                                        onClick = { menuAbierto = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Opciones",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuAbierto,
                                        onDismissRequest = { menuAbierto = false },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Ver Detalles") },
                                            onClick = { menuAbierto = false; grupoSeleccionado = grupo }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Salir del Grupo") },
                                            onClick = { menuAbierto = false; firestore.salirDelGrupo(grupo.id) }
                                        )
                                        if (esCreador) {
                                            DropdownMenuItem(
                                                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                                onClick = { menuAbierto = false; grupoAEliminar = grupo; mostrarConfirmarEliminar = true }
                                            )
                                        }
                                    }
                                }

                                // Contenido centralizado de la Tarjeta
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    // Inicial destacada
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = grupo.nombre.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    // Nombre del grupo restringido a max 2 líneas para evitar deformaciones
                                    Text(
                                        text = grupo.nombre,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    // Subtexto fino de miembros
                                    Text(
                                        text = if (grupo.miembros.size == 1) "1 miembro" else "${grupo.miembros.size} miembros",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarConfirmarEliminar && grupoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "¿Eliminar comunidad?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Esta acción borrará definitivamente \"${grupoAEliminar!!.nombre}\" junto con todos sus eventos programados.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { firestore.eliminarGrupo(grupoAEliminar!!.id); mostrarConfirmarEliminar = false }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = false }) {
                    Text("Cancelar", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}