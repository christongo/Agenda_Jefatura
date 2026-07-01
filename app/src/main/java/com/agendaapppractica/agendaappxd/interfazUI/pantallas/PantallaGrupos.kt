package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGrupos(
    onUnirseGrupoClick: () -> Unit,
    onMisGruposClick: () -> Unit,
    onGrupoClick: (Grupo) -> Unit = {}
) {
    val context = LocalContext.current
    val firestoreManager = remember { FirestoreManager() }

    val prefs = remember { context.getSharedPreferences("comunidades_prefs", Context.MODE_PRIVATE) }
    val prefsTutorial = remember { context.getSharedPreferences("comunidades_principal_tutorial_prefs", Context.MODE_PRIVATE) }

    var idsFijados by remember {
        mutableStateOf(prefs.getStringSet("ids_fijados", emptySet()) ?: emptySet())
    }

    var todosLosGrupos by remember { mutableStateOf<List<Grupo>>(emptyList()) }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var nombreNuevoGrupo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var mostrarTutorial by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val yaVisto = prefsTutorial.getBoolean("ocultar_tutorial_principal", false)
        if (!yaVisto) {
            mostrarTutorial = true
        }

        firestoreManager.escucharMisGrupos { todosLosGrupos = it }
    }

    val gruposFrecuentes = remember(todosLosGrupos, idsFijados) {
        todosLosGrupos.filter { it.id in idsFijados }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grupos de Trabajo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(onClick = { mostrarTutorial = true }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Ver guía de inicio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { mostrarDialogoCrear = true }
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupWork,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Crear grupo",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Nueva comunidad",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onUnirseGrupoClick() }
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Unirse a grupo",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ingresar un código",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                if (gruposFrecuentes.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Accesos Directos Fijados",
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(gruposFrecuentes) { grupo ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable { onGrupoClick(grupo) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (grupo.fotoGrupo.isNotBlank()) {
                                                AsyncImage(
                                                    model = grupo.fotoGrupo,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = grupo.nombre.take(1).uppercase(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = grupo.nombre,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = if (grupo.miembros.size == 1) "1 miembro" else "${grupo.miembros.size} miemb.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Tus Comunidades",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onMisGruposClick() }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mis grupos activos",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ver, editar y administrar tus comunidades",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) {
                    mostrarDialogoCrear = false
                    nombreNuevoGrupo = ""
                }
            },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Crear nuevo grupo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Asigna un nombre a tu comunidad de trabajo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = nombreNuevoGrupo,
                        onValueChange = { nombreNuevoGrupo = it },
                        label = { Text("Nombre del grupo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !cargando
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCrear = false
                        nombreNuevoGrupo = ""
                    },
                    enabled = !cargando
                ) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreNuevoGrupo.isNotBlank()) {
                            cargando = true

                            firestoreManager.crearGrupo(
                                nombre = nombreNuevoGrupo,
                                descripcion = "Grupo de trabajo creado desde la app"
                            )

                            coroutineScope.launch {
                                mostrarDialogoCrear = false
                                val nombreGuardado = nombreNuevoGrupo
                                nombreNuevoGrupo = ""
                                cargando = false

                                snackbarHostState.showSnackbar(
                                    message = "¡Grupo \"$nombreGuardado\" creado exitosamente!",
                                    duration = SnackbarDuration.Short
                                )
                                delay(500)
                                onMisGruposClick()
                            }
                        }
                    },
                    enabled = nombreNuevoGrupo.isNotBlank() && !cargando,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Crear")
                    }
                }
            }
        )
    }

    if (mostrarTutorial) {
        DialogoTutorialPantallaGrupos(
            onDismiss = { mostrarTutorial = false },
            onNoMostrarMas = {
                prefsTutorial.edit().putBoolean("ocultar_tutorial_principal", true).apply()
                mostrarTutorial = false
                Toast.makeText(context, "Asistente de inicio rápido desactivado", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DialogoTutorialPantallaGrupos(
    onDismiss: () -> Unit,
    onNoMostrarMas: () -> Unit
) {
    var pasoActual by remember { mutableStateOf(1) }
    val totalPasos = 3
    val progresoAnimado = pasoActual.toFloat() / totalPasos.toFloat()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (pasoActual) {
                            1 -> Icons.Default.GroupWork
                            2 -> Icons.Default.PushPin
                            else -> Icons.Default.Group
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pasoActual) {
                        1 -> "Gestión Inicial"
                        2 -> "Accesos Fijados"
                        else -> "Panel de Control"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progresoAnimado },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fase $pasoActual de $totalPasos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 130.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    when (pasoActual) {
                        1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Alta e Incorporación",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Crear Grupo: Permite abrir una nueva comunidad asignándole un título descriptivo único de manera inmediata.\n" +
                                        "* Unirse a Grupo: Permite ingresar a entornos compartidos mediante la inserción de claves de invitación generadas por otros coordinadores.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Monitoreo Frecuente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Visualización Ágil: Despliega en un carrusel de flujo horizontal superior aquellas comunidades que has fijado previamente como prioritarias.\n" +
                                        "* Accesibilidad Directa: Entra a los canales recurrentes con un solo toque desde este concentrador principal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Listado Centralizado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Mis Grupos Activos: Accede a la base general de comunidades para auditar roles, administrar fijados rápidos o desvincularte de canales obsoletos.\n" +
                                        "* Soporte Permanente: El ícono superior derecho de ayuda reactivará este manual cada vez que requieras resolver alguna duda de flujo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNoMostrarMas) {
                        Text(
                            text = "No volver a mostrar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = {
                            if (pasoActual < totalPasos) {
                                pasoActual++
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (pasoActual < totalPasos) "Siguiente" else "Completar",
                            fontWeight = FontWeight.Bold
                        )
                        if (pasoActual < totalPasos) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}