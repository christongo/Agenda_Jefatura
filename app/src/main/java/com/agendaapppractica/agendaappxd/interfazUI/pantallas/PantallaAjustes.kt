package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    val lifecycleOwner = LocalLifecycleOwner.current
    val auth = remember { FirebaseAuth.getInstance() }
    val usuarioActual = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var nombreUsuario by remember { mutableStateOf(usuarioActual?.displayName ?: "Usuario") }
    var apellidoUsuario by remember { mutableStateOf("") }
    var telefonoUsuario by remember { mutableStateOf("") }
    var fechaNacimientoUsuario by remember { mutableStateOf("") } // 🛠️ Añadido estado para la fecha

    // Cambiado a Any? para admitir de manera ultra rápida tanto Uris locales como URLs de la red
    var fotoPerfilUri by remember { mutableStateOf<Any?>(null) }

    var dPerfil by remember { mutableStateOf(false) }
    var dSeguridad by remember { mutableStateOf(false) }
    var dPersonalizacion by remember { mutableStateOf(false) }
    var dPermisos by remember { mutableStateOf(false) }
    var dAcerca by remember { mutableStateOf(false) }

    // === ESTADOS DE PERMISOS REACTIVOS ===
    var tienePermisoAlmacenamiento by remember { mutableStateOf(false) }
    var tienePermisoNotificaciones by remember { mutableStateOf(false) }
    var tienePermisoCamara by remember { mutableStateOf(false) }

    val actualizarEstadoPermisosSilencioso = {
        tienePermisoCamara = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        tienePermisoAlmacenamiento = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        tienePermisoNotificaciones = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                actualizarEstadoPermisosSilencioso()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val abrirConfiguracionSistema = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    val requestCamaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) tienePermisoCamara = true else abrirConfiguracionSistema()
    }

    val requestNotificacionesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) tienePermisoNotificaciones = true else abrirConfiguracionSistema()
    }

    val requestAlmacenamientoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) tienePermisoAlmacenamiento = true else abrirConfiguracionSistema()
    }

    // 🛠️ Al elegir la foto, se asigna al estado local al instante (Pinta la UI sin esperar a Firebase)
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            fotoPerfilUri = uri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) galleryLauncher.launch("image/*") }

    LaunchedEffect(usuarioActual?.uid) {
        usuarioActual?.uid?.let { uid ->
            db.collection("usuarios").document(uid).addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    nombreUsuario = doc.getString("nombre") ?: "Usuario"
                    apellidoUsuario = doc.getString("apellido") ?: ""
                    telefonoUsuario = doc.getString("telefono") ?: ""
                    fechaNacimientoUsuario = doc.getString("fechaNacimiento") ?: "" // 🛠️ Sincronizar fecha de la nube

                    doc.getString("fotoPerfilUrl")?.let {
                        if (it.isNotEmpty() && fotoPerfilUri == null) {
                            fotoPerfilUri = Uri.parse(it)
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 24.dp))

        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    if (fotoPerfilUri != null) AsyncImage(model = fotoPerfilUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("$nombreUsuario $apellidoUsuario", style = MaterialTheme.typography.titleLarge)
                    Text(usuarioActual?.email ?: "", color = Color.Gray)
                }
            }
        }

        Text("Cuenta y Configuración", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        ItemAjuste(Icons.Default.AccountBox, "Ver y editar perfil", containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) { dPerfil = true }
        ItemAjuste(Icons.Default.Lock, "Seguridad", "Cambiar contraseña") { dSeguridad = true }
        ItemAjuste(Icons.Default.Palette, "Personalización", "Tema, colores y texturas") { dPersonalizacion = true }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Ayuda y Soporte", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)

        ItemAjuste(Icons.Default.Info, "Permisos de la app", "Gestionar accesos") {
            actualizarEstadoPermisosSilencioso()
            dPermisos = true
        }

        ItemAjuste(Icons.Default.Android, "Información de la app", "Versión y sistema") { dAcerca = true }
        TarjetaActualizacion()

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { auth.signOut(); onCerrarSesion() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }

    if (dPerfil) {
        DialogoEditarPerfil(
            nombreActual = nombreUsuario,
            apellidoActual = apellidoUsuario,
            telefonoActual = telefonoUsuario,
            fechaActual = fechaNacimientoUsuario, // 🛠️ Pasando la fecha correcta al diálogo
            correoUsuario = usuarioActual?.email ?: "",
            fotoPerfilUri = if (fotoPerfilUri is Uri) fotoPerfilUri as Uri else null,
            intentarCambiarFoto = {
                permissionLauncher.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
                    else Manifest.permission.READ_EXTERNAL_STORAGE
                )
            },
            onDismiss = { dPerfil = false },
            onGuardarExitoso = { n: String, a: String, t: String, f: String ->
                nombreUsuario = n
                apellidoUsuario = a
                telefonoUsuario = t
                fechaNacimientoUsuario = f // 🛠️ Guardar la nueva fecha en el estado local
                dPerfil = false
            }
        )
    }

    if (dSeguridad) DialogoSeguridadApp(usuarioActual?.email ?: "", { dSeguridad = false })
    if (dAcerca) DialogoAcercaDe { dAcerca = false }

    if (dPermisos) {
        DialogoPermisosApp(
            permisoAlmacenamiento = tienePermisoAlmacenamiento,
            permisoNotificaciones = tienePermisoNotificaciones,
            permisoCamara = tienePermisoCamara,
            onCambiarAlmacenamiento = {
                if (!tienePermisoAlmacenamiento) {
                    val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
                    requestAlmacenamientoLauncher.launch(permiso)
                } else {
                    abrirConfiguracionSistema()
                }
            },
            onCambiarNotificaciones = {
                if (!tienePermisoNotificaciones && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificacionesLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    abrirConfiguracionSistema()
                }
            },
            onCambiarCamara = {
                if (!tienePermisoCamara) {
                    requestCamaraLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    abrirConfiguracionSistema()
                }
            },
            onDismiss = { dPermisos = false }
        )
    }

    if (dPersonalizacion) DialogoPersonalizacion(modoOscuroActivo, onModoOscuroCambiado, colorTemaActual, onColorTemaCambiChanged, tipoTexturaActual, onTipoTexturaCambiado, grosorLineaActual, onGrosorLineaCambiado) { dPersonalizacion = false }
}