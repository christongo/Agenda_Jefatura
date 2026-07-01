package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.agendaapppractica.agendaappxd.BuildConfig
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoEditarPerfil
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoPermisosApp
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.DialogoSeguridadApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

fun tienePermisoMultimedia(context: Context): Boolean {
    val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
}

fun tienePermisoNotificaciones(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun tienePermisoCamara(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(
    modoOscuroActivo: Boolean,
    onModoOscuroCambiado: (Boolean) -> Unit,
    colorTemaActual: String,
    onColorTemaCambiChanged: (String) -> Unit,
    tipoTexturaActual: String,
    onTipoTexturaCambiado: (String) -> Unit,
    grosorLineaActual: Float = 1f,
    onGrosorLineaCambiado: (Float) -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val usuarioActual = auth.currentUser

    var nombreUsuario by remember { mutableStateOf(usuarioActual?.displayName ?: "Usuario") }
    var apellidoUsuario by remember { mutableStateOf("") }
    var telefonoUsuario by remember { mutableStateOf("") }
    val correoUsuario = usuarioActual?.email ?: "sin_correo@test.com"
    var fechaNacimiento by remember { mutableStateOf("") }
    var fotoPerfilUri by remember { mutableStateOf<Uri?>(null) }

    var mostrarDialogoPerfil by remember { mutableStateOf(false) }
    var mostrarDialogoSeguridad by remember { mutableStateOf(false) }
    var mostrarDialogoPersonalizacion by remember { mutableStateOf(false) }
    var mostrarDialogoPermisos by remember { mutableStateOf(false) }
    var mostrarDialogoAcercaDe by remember { mutableStateOf(false) }

    var permisoConcedidoEstado by remember { mutableStateOf(tienePermisoMultimedia(context)) }
    var permisoNotifConcedidoEstado by remember { mutableStateOf(tienePermisoNotificaciones(context)) }
    var permisoCamaraConcedidoEstado by remember { mutableStateOf(tienePermisoCamara(context)) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoPerfilUri = uri
            Toast.makeText(context, "Foto de perfil seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        permisoConcedidoEstado = esConcedido
        if (esConcedido) {
            Toast.makeText(context, "¡Permiso multimedia concedido!", Toast.LENGTH_SHORT).show()
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permiso denegado.", Toast.LENGTH_LONG).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        permisoNotifConcedidoEstado = esConcedido
        if (esConcedido) {
            Toast.makeText(context, "¡Notificaciones activadas!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Alertas desactivadas.", Toast.LENGTH_LONG).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        permisoCamaraConcedidoEstado = esConcedido
        if (esConcedido) {
            Toast.makeText(context, "¡Permiso de cámara concedido!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de cámara denegado.", Toast.LENGTH_LONG).show()
        }
    }

    val intentarCambiarFoto = {
        val permisoAAsignar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (tienePermisoMultimedia(context)) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Necesito permisos de multimedia", Toast.LENGTH_LONG).show()
            permissionLauncher.launch(permisoAAsignar)
        }
    }

    LaunchedEffect(usuarioActual?.uid) {
        usuarioActual?.uid?.let { uid ->
            db.collection("usuarios").document(uid)
                .addSnapshotListener { document, error ->
                    if (error != null) return@addSnapshotListener
                    if (document != null && document.exists()) {
                        nombreUsuario = document.getString("nombre") ?: (usuarioActual.displayName ?: "Usuario")
                        apellidoUsuario = document.getString("apellido") ?: ""
                        telefonoUsuario = document.getString("telefono") ?: ""
                        fechaNacimiento = document.getString("fechaNacimiento") ?: ""
                        val urlFoto = document.getString("fotoPerfilUrl")
                        if (!urlFoto.isNullOrEmpty()) {
                            fotoPerfilUri = Uri.parse(urlFoto)
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Ajustes", style = MaterialTheme.typography.headlineLarge)
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        if (fotoPerfilUri != null) {
                            AsyncImage(model = fotoPerfilUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = if (apellidoUsuario.isEmpty()) nombreUsuario else "$nombreUsuario $apellidoUsuario", style = MaterialTheme.typography.titleLarge)
                        Text(text = if (telefonoUsuario.isNotEmpty()) telefonoUsuario else correoUsuario, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().clickable { intentarCambiarFoto() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Cambiar foto de perfil", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoPerfil = true }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Ver y editar perfil completo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoSeguridad = true }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Security", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Cambiar contraseña", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoPersonalizacion = true }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Personalización", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Tema oscuro, colores y papel texturizado", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoPermisos = true }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Permisos de la app", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoAcercaDe = true }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color(0xFF3DDC84))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Información de la app", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Versión del sistema, soporte y detalles", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { auth.signOut(); onCerrarSesion() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar sesión")
        }
    }

    if (mostrarDialogoPerfil) {
        DialogoEditarPerfil(
            nombreActual = nombreUsuario, apellidoActual = apellidoUsuario, telefonoActual = telefonoUsuario, fechaActual = fechaNacimiento, correoUsuario = correoUsuario, fotoPerfilUri = fotoPerfilUri, intentarCambiarFoto = { intentarCambiarFoto() }, onDismiss = { mostrarDialogoPerfil = false },
            onGuardarExitoso = { n, a, t, f -> nombreUsuario = n; apellidoUsuario = a; telefonoUsuario = t; fechaNacimiento = f; mostrarDialogoPerfil = false }
        )
    }

    if (mostrarDialogoPermisos) {
        DialogoPermisosApp(
            permisoConcedidoEstado = permisoConcedidoEstado,
            permisoNotifConcedidoEstado = permisoNotifConcedidoEstado,
            permisoCamaraConcedidoEstado = permisoCamaraConcedidoEstado,
            solicitarMultimedia = { val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE; permissionLauncher.launch(p) },
            solicitarNotificaciones = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            solicitarCamara = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
            onDismiss = { mostrarDialogoPermisos = false }
        )
    }

    if (mostrarDialogoSeguridad) {
        DialogoSeguridadApp(correoUsuario = correoUsuario, onDismiss = { mostrarDialogoSeguridad = false })
    }

    if (mostrarDialogoAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAcercaDe = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Acerca de esta App", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Agenda Jefatura", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Versión instalada: v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Código de compilación: ${BuildConfig.VERSION_CODE}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = "Desarrollado para la optimización y gestión de tareas de jefatura de forma práctica.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoAcercaDe = false }) { Text("Cerrar") }
            }
        )
    }

    if (mostrarDialogoPersonalizacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPersonalizacion = false },
            title = { Text("Personalización", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tema Oscuro", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = modoOscuroActivo, onCheckedChange = onModoOscuroCambiado)
                    }

                    HorizontalDivider()

                    Text("Color del Sistema", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    val coloresLocales = listOf(
                        "Morado" to Color(0xFF9C27B0),
                        "Azul" to Color(0xFF2196F3),
                        "Verde" to Color(0xFF4CAF50),
                        "Rojo" to Color(0xFFE53935),
                        "Naranja" to Color(0xFFFF9800),
                        "Rosa" to Color(0xFFE91E63),
                        "Cian" to Color(0xFF00BCD4)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        coloresLocales.take(4).forEach { (nombre, colorReal) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorReal)
                                    .clickable { onColorTemaCambiChanged(nombre) }
                            ) {
                                if (colorTemaActual == nombre) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(18.dp))
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Textura del Bloc de Notas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    val opcionesTextura = listOf("Ninguno", "Líneas")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        opcionesTextura.forEach { textura ->
                            FilterChip(
                                selected = tipoTexturaActual == textura,
                                onClick = { onTipoTexturaCambiado(textura) },
                                label = { Text(textura, fontSize = 12.sp) }
                            )
                        }
                    }

                    if (tipoTexturaActual == "Líneas") {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grosor de línea", style = MaterialTheme.typography.bodyMedium)
                                Text("${grosorLineaActual.roundToInt()} px", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = grosorLineaActual,
                                onValueChange = { onGrosorLineaCambiado(it) },
                                valueRange = 1f..6f,
                                steps = 4
                            )
                        }
                    }

                    Text("Vista previa del papel:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    val lineaColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    Card(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (tipoTexturaActual == "Líneas") {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    var y = 35.dp.toPx()
                                    while (y < size.height) {
                                        drawLine(
                                            color = lineaColor,
                                            start = Offset(12.dp.toPx(), y),
                                            end = Offset(size.width - 12.dp.toPx(), y),
                                            strokeWidth = grosorLineaActual
                                        )
                                        y += 24.dp.toPx()
                                    }
                                }
                            }
                            Text(
                                text = "Comienza a escribir aquí...",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 14.dp).padding(top = 45.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoPersonalizacion = false }) { Text("Aceptar") }
            }
        )
    }
}