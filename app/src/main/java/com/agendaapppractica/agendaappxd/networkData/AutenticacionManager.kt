package com.agendaapppractica.agendaappxd.networkData

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {

    suspend fun iniciarSesionConGoogle(context: Context, webClientId: String): Boolean {
        val credentialManager = CredentialManager.create(context)
        val auth = FirebaseAuth.getInstance()

        // 1. Configurar la solicitud de ID de Google
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            // 2. Lanzar el selector nativo de cuentas de Google en Android
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is GoogleIdTokenCredential) {
                val googleIdToken = credential.idToken

                // 3. Autenticar en tu Firebase usando el Token obtenido
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}

// 🛠️ CORREGIDO: Cambiado de 'annotation class' a un 'object' limpio e inicializado por defecto
object AutenticacionManager {

    // Inicializado en false por defecto para evitar el error de compilación
    var sesionComoInvitadoLocal: Boolean = false

    // Propiedad calculada útil para verificar de forma rápida el estado en las vistas
    val esInvitado: Boolean
        get() = sesionComoInvitadoLocal || FirebaseAuth.getInstance().currentUser == null

    /**
     * Cierra de manera limpia la sesión actual tanto de Firebase como del estado local.
     */
    fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        sesionComoInvitadoLocal = false
    }
}