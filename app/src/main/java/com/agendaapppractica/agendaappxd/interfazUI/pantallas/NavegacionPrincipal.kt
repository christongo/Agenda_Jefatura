package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agendaapppractica.agendaappxd.networkData.AutenticacionManager
import com.agendaapppractica.agendaappxd.ui.theme.AgendaappxdTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun NavegacionPrincipal(sharedPref: SharedPreferences) {
    var usuarioLogueado by remember {
        mutableStateOf(sharedPref.getBoolean("isLoggedIn", false) && !AutenticacionManager.sesionComoInvitadoLocal)
    }

    var mostrandoPantallaCarga by remember { mutableStateOf(false) }

    var darkTheme by remember { mutableStateOf(sharedPref.getBoolean("darkMode", false)) }
    var colorTema by remember { mutableStateOf(sharedPref.getString("colorTema", "Morado") ?: "Morado") }
    var tipoTextura by remember { mutableStateOf(sharedPref.getString("tipoTextura", "Ninguno") ?: "Ninguno") }
    var grosorLinea by remember { mutableStateOf(sharedPref.getFloat("grosorLinea", 1.5f)) }
    var mostrarRegistro by remember { mutableStateOf(false) }

    LaunchedEffect(mostrandoPantallaCarga) {
        if (mostrandoPantallaCarga) {
            delay(1800)
            mostrandoPantallaCarga = false
            usuarioLogueado = true
        }
    }

    AgendaappxdTheme(darkTheme = darkTheme, colorTema = colorTema) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (!usuarioLogueado && !mostrandoPantallaCarga) {
                if (mostrarRegistro) {
                    PantallaRegistro(
                        onRegistroExitoso = { mostrarRegistro = false },
                        volverLogin = { mostrarRegistro = false }
                    )
                } else {
                    PantallaLogin(
                        onLoginSuccess = {
                            val esInvitadoReal = AutenticacionManager.sesionComoInvitadoLocal
                            sharedPref.edit().putBoolean("isLoggedIn", !esInvitadoReal).apply()
                            mostrandoPantallaCarga = true
                        },
                        irRegistro = { mostrarRegistro = true }
                    )
                }
            } else if (usuarioLogueado) {
                ContenedorApp(
                    sharedPref = sharedPref,
                    darkTheme = darkTheme,
                    colorTema = colorTema,
                    tipoTextura = tipoTextura,
                    grosorLinea = grosorLinea,
                    onDarkThemeChange = { darkTheme = it },
                    onColorTemaChange = { colorTema = it },
                    onTipoTexturaChange = { tipoTextura = it },
                    onGrosorLineaChange = { grosorLinea = it },
                    onCerrarSesionClick = {
                        FirebaseAuth.getInstance().signOut()
                        AutenticacionManager.sesionComoInvitadoLocal = false

                        usuarioLogueado = false
                        sharedPref.edit().putBoolean("isLoggedIn", false).apply()
                    }
                )
            }

            AnimatedVisibility(
                visible = mostrandoPantallaCarga,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(600))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Agenda Jefatura",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Preparando tu espacio de trabajo...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }
    }
}