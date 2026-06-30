package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agendaapppractica.agendaappxd.networkData.AutenticacionManager
import com.agendaapppractica.agendaappxd.networkData.GoogleAuthHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PantallaLogin(
    onLoginSuccess: () -> Unit,
    irRegistro: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var esMensajeExito by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    val animacionEntradaScale = remember { Animatable(0.95f) }
    val animacionEntradaAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animacionEntradaScale.animateTo(1f, animationSpec = tween(durationMillis = 500))
    }
    LaunchedEffect(Unit) {
        animacionEntradaAlpha.animateTo(1f, animationSpec = tween(durationMillis = 400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .scale(animacionEntradaScale.value)
                .alpha(animacionEntradaAlpha.value)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Agenda Jefatura",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Gestiona tu día de forma limpia y eficiente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; mensaje = "" },
                placeholder = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; mensaje = "" },
                placeholder = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(
                    onClick = {
                        if (email.isBlank()) {
                            esMensajeExito = false
                            mensaje = "Ingresa tu correo primero"
                            return@TextButton
                        }
                        cargando = true
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                cargando = false
                                if (task.isSuccessful) {
                                    esMensajeExito = true
                                    mensaje = "Correo de recuperación enviado"
                                } else {
                                    esMensajeExito = false
                                    mensaje = "Error al enviar recuperación"
                                }
                            }
                    },
                    enabled = !cargando,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                }
            }

            AnimatedVisibility(
                visible = mensaje.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = mensaje,
                    color = if (esMensajeExito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) { esMensajeExito = false; mensaje = "Completa todos los campos"; return@Button }
                    if (password.length < 6) { esMensajeExito = false; mensaje = "Mínimo 6 caracteres"; return@Button }
                    mensaje = ""
                    cargando = true

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            cargando = false
                            if (task.isSuccessful) {
                                AutenticacionManager.sesionComoInvitadoLocal = false
                                onLoginSuccess()
                            } else {
                                esMensajeExito = false
                                mensaje = "Credenciales incorrectas"
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !cargando
            ) {
                AnimatedContent(targetState = cargando) { estaCargando ->
                    if (estaCargando) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = irRegistro,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !cargando
            ) {
                Text("Crear una cuenta", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Text(text = " o continuar con ", modifier = Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    mensaje = ""
                    cargando = true
                    coroutineScope.launch {
                        val webClientId = "TU_ID_DE_CLIENTE_WEB.apps.googleusercontent.com"
                        val exito = GoogleAuthHelper.iniciarSesionConGoogle(context, webClientId)
                        cargando = false
                        if (exito) {
                            AutenticacionManager.sesionComoInvitadoLocal = false
                            onLoginSuccess()
                        } else {
                            esMensajeExito = false
                            mensaje = "Cancelado o error de autenticación con Google"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !cargando,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "G ", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    Text("Google", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    AutenticacionManager.sesionComoInvitadoLocal = true
                    onLoginSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !cargando,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PersonOutline, null, modifier = Modifier.size(20.dp))
                    Text("Modo Invitado", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}