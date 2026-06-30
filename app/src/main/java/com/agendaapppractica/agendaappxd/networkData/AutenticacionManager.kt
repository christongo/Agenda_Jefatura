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

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is GoogleIdTokenCredential) {
                val googleIdToken = credential.idToken

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

object AutenticacionManager {

    var sesionComoInvitadoLocal: Boolean = false

    val esInvitado: Boolean
        get() = sesionComoInvitadoLocal || FirebaseAuth.getInstance().currentUser == null

    fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        sesionComoInvitadoLocal = false
    }
}