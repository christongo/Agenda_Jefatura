package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.Manifest
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.agendaapppractica.agendaappxd.interfazUI.dialogos.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(
    modoOscuroActivo: Boolean, onModoOscuroCambiado: (Boolean) -> Unit,
    colorTemaActual: String, onColorTemaCambiChanged: (String) -> Unit,
    tipoTexturaActual: String, onTipoTexturaCambiado: (String) -> Unit,
    grosorLineaActual: Float = 1f, onGrosorLineaCambiado: (Float) -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val usuarioActual = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var nombreUsuario by remember { mutableStateOf(usuarioActual?.displayName ?: "Usuario") }
    var apellidoUsuario by remember { mutableStateOf("") }
    var telefonoUsuario by remember { mutableStateOf("") }
    var fotoPerfilUri by remember { mutableStateOf<Uri?>(null) }

    var dPerfil by remember { mutableStateOf(false) }
    var dSeguridad by remember { mutableStateOf(false) }
    var dPersonalizacion by remember { mutableStateOf(false) }
    var dPermisos by remember { mutableStateOf(false) }
    var dAcerca by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) fotoPerfilUri = it }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) galleryLauncher.launch("image/*") }

    LaunchedEffect(usuarioActual?.uid) {
        usuarioActual?.uid?.let { uid ->
            db.collection("usuarios").document(uid).addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    nombreUsuario = doc.getString("nombre") ?: "Usuario"
                    apellidoUsuario = doc.getString("apellido") ?: ""
                    telefonoUsuario = doc.getString("telefono") ?: ""
                    doc.getString("fotoPerfilUrl")?.let { if (it.isNotEmpty()) fotoPerfilUri = Uri.parse(it) }
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
        ItemAjuste(Icons.Default.AccountBox, "Ver y editar perfil completo", containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) { dPerfil = true }
        ItemAjuste(Icons.Default.Lock, "Seguridad", "Cambiar contraseña") { dSeguridad = true }
        ItemAjuste(Icons.Default.Palette, "Personalización", "Tema, colores y texturas") { dPersonalizacion = true }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Ayuda y Soporte", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        ItemAjuste(Icons.Default.Info, "Permisos de la app", "Gestionar accesos") { dPermisos = true }
        ItemAjuste(Icons.Default.Android, "Información de la app", "Versión y sistema") { dAcerca = true }
        TarjetaActualizacion()

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { auth.signOut(); onCerrarSesion() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }

    if (dPerfil) DialogoEditarPerfil(nombreUsuario, apellidoUsuario, telefonoUsuario, "", usuarioActual?.email ?: "", fotoPerfilUri, { permissionLauncher.launch(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE) }, { dPerfil = false }, { n, a, t, _ -> nombreUsuario = n; apellidoUsuario = a; telefonoUsuario = t; dPerfil = false })
    if (dSeguridad) DialogoSeguridadApp(usuarioActual?.email ?: "", { dSeguridad = false })
    if (dAcerca) DialogoAcercaDe { dAcerca = false }
    if (dPermisos) DialogoPermisosApp(false, false, false, {}, {}, {}, { dPermisos = false })
    if (dPersonalizacion) DialogoPersonalizacion(modoOscuroActivo, onModoOscuroCambiado, colorTemaActual, onColorTemaCambiChanged, tipoTexturaActual, onTipoTexturaCambiado, grosorLineaActual, onGrosorLineaCambiado) { dPersonalizacion = false }
}