package com.agendaapppractica.agendaappxd.networkData

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.agendaapppractica.agendaappxd.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    // 🌐 Coloca aquí tu URL pública donde alojarás el JSON (GitHub, Hosting, etc.)
    private const val URL_VERSION_JSON = "https://tu-servidor.com/update.json"

    class UpdateInfo(val urlApk: String, val versionCode: Int, val versionName: String)

    suspend fun verificarActualizacion(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(URL_VERSION_JSON)
            val conexion = url.openConnection() as HttpURLConnection
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 5000

            if (conexion.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = conexion.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)

                val latestVersionCode = json.getInt("versionCode")
                val latestVersionName = json.getString("versionName")
                val apkUrl = json.getString("apkUrl")

                if (latestVersionCode > BuildConfig.VERSION_CODE) {
                    return@withContext UpdateInfo(apkUrl, latestVersionCode, latestVersionName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    fun descargarEInstalarApk(context: Context, urlApk: String, nombreVersion: String) {
        val nombreArchivo = "AgendaJefatura_v$nombreVersion.apk"
        val destinoFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)

        if (destinoFile.exists()) { destinoFile.delete() }

        val request = DownloadManager.Request(Uri.parse(urlApk))
            .setTitle("Actualizando Agenda")
            .setDescription("Descargando versión $nombreVersion")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destinoFile))

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    context.unregisterReceiver(this)
                    instalarApk(ctx, destinoFile)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun instalarApk(context: Context, archivo: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}