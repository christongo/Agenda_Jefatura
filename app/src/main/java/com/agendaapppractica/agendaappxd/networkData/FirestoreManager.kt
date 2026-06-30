package com.agendaapppractica.agendaappxd.networkData

import android.util.Log
import com.agendaapppractica.agendaappxd.model.Anuncio
import com.agendaapppractica.agendaappxd.model.Grupo
import com.agendaapppractica.agendaappxd.model.Tarea
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.agendaapppractica.agendaappxd.model.MiembroUsuario

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coleccionEventos = "eventos"
    private val coleccionGrupos = "grupos"

    fun añadirTarea(tarea: Tarea) {
        val id = db.collection(coleccionEventos).document().id
        val nueva = tarea.copy(id = id)

        db.collection(coleccionEventos)
            .document(id)
            .set(nueva)
            .addOnSuccessListener { Log.d("FIRESTORE", "Evento guardado") }
            .addOnFailureListener { e -> Log.e("FIRESTORE", "Error al guardar", e) }
    }

    fun eliminarTarea(tareaId: String) {
        db.collection(coleccionEventos).document(tareaId).delete()
    }

    fun actualizarTarea(tarea: Tarea) {
        db.collection(coleccionEventos).document(tarea.id).set(tarea)
    }

    fun escucharEventosPersonales(callback: (List<Tarea>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection(coleccionEventos)
            .whereEqualTo("usuarioId", uid)
            .whereEqualTo("visibilidad", "personal")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                callback(value?.toObjects(Tarea::class.java) ?: emptyList())
            }
    }

    fun escucharEventosGrupo(grupoId: String, callback: (List<Tarea>) -> Unit) {
        db.collection(coleccionEventos)
            .whereEqualTo("grupoId", grupoId)
            .whereEqualTo("visibilidad", "grupo")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                callback(value?.toObjects(Tarea::class.java) ?: emptyList())
            }
    }

    fun escucharTareasDelDia(fecha: String, misGruposIds: List<String>, callback: (List<Tarea>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(emptyList())

        db.collection(coleccionEventos)
            .whereEqualTo("fecha", fecha)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                val todosLosEventos = value?.toObjects(Tarea::class.java) ?: emptyList()

                val listaFiltrada = todosLosEventos.filter { tarea ->
                    when (tarea.visibilidad) {
                        "personal" -> tarea.usuarioId == uid
                        "grupo" -> misGruposIds.contains(tarea.grupoId)
                        "publico" -> true
                        else -> false
                    }
                }
                callback(listaFiltrada)
            }
    }

    fun expulsarMiembro(grupoId: String, uidMiembro: String) {
        db.collection(coleccionGrupos)
            .document(grupoId)
            .update("miembros", FieldValue.arrayRemove(uidMiembro))
            .addOnSuccessListener {
                db.collection(coleccionGrupos).document(grupoId)
                    .update("administradores", FieldValue.arrayRemove(uidMiembro))
            }
    }

    fun crearGrupo(nombre: String, descripcion: String) {
        val uid = auth.currentUser?.uid ?: return
        val id = db.collection(coleccionGrupos).document().id
        val codigoGrupo = (100000..999999).random().toString()

        val grupo = Grupo(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            codigo = codigoGrupo,
            creadorId = uid,
            fotoGrupo = "",
            miembros = listOf(uid),
            solicitudes = emptyList(),
            administradores = listOf(uid)
        )
        db.collection(coleccionGrupos).document(id).set(grupo)
            .addOnSuccessListener { Log.d("FIRESTORE", "Grupo creado exitosamente mapeado") }
            .addOnFailureListener { e -> Log.e("FIRESTORE", "Error al crear grupo", e) }
    }

    fun actualizarGrupo(grupoId: String, nuevoNombre: String, nuevaDescripcion: String) {
        db.collection(coleccionGrupos)
            .document(grupoId)
            .update(mapOf("nombre" to nuevoNombre, "descripcion" to nuevaDescripcion))
    }

    // 🔥 NUEVA: Implementación asíncrona compatible con los componentes separados
    fun actualizarDetallesGrupo(grupoId: String, nuevoNombre: String, nuevaDescripcion: String, onResultado: (Boolean) -> Unit) {
        db.collection(coleccionGrupos)
            .document(grupoId)
            .update(mapOf("nombre" to nuevoNombre, "descripcion" to nuevaDescripcion))
            .addOnSuccessListener { onResultado(true) }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error al actualizar grupo", e)
                onResultado(false)
            }
    }

    fun unirseAGrupo(codigoGrupo: String, onResultado: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResultado(false)
        db.collection(coleccionGrupos)
            .whereEqualTo("codigo", codigoGrupo)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    documentos.documents.first().reference.update("solicitudes", FieldValue.arrayUnion(uid))
                        .addOnSuccessListener { onResultado(true) }
                        .addOnFailureListener { onResultado(false) }
                } else {
                    onResultado(false)
                }
            }
            .addOnFailureListener {
                onResultado(false)
            }
    }

    fun aceptarSolicitudDeUnion(grupoId: String, uidSolicitante: String, onResultado: (Boolean) -> Unit) {
        val ref = db.collection(coleccionGrupos).document(grupoId)
        db.runTransaction { transaccion ->
            transaccion.update(ref, "solicitudes", FieldValue.arrayRemove(uidSolicitante))
            transaccion.update(ref, "miembros", FieldValue.arrayUnion(uidSolicitante))
        }.addOnSuccessListener {
            onResultado(true)
        }.addOnFailureListener { e ->
            Log.e("FIRESTORE", "Error al aceptar miembro", e)
            onResultado(false)
        }
    }

    fun rechazarSolicitudDeUnion(grupoId: String, uidSolicitante: String, onResultado: (Boolean) -> Unit) {
        db.collection(coleccionGrupos)
            .document(grupoId)
            .update("solicitudes", FieldValue.arrayRemove(uidSolicitante))
            .addOnSuccessListener { onResultado(true) }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error al rechazar solicitud", e)
                onResultado(false)
            }
    }

    fun asignarAdministradorGrupo(grupoId: String, uidMiembro: String, onResultado: (Boolean) -> Unit) {
        db.collection(coleccionGrupos)
            .document(grupoId)
            .update("administradores", FieldValue.arrayUnion(uidMiembro))
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Miembro $uidMiembro ascendido a Admin")
                onResultado(true)
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error al asignar administrador", e)
                onResultado(false)
            }
    }

    fun quitarAdministradorGrupo(grupoId: String, uidMiembro: String, onResultado: (Boolean) -> Unit) {
        db.collection(coleccionGrupos)
            .document(grupoId)
            .update("administradores", FieldValue.arrayRemove(uidMiembro))
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Miembro $uidMiembro degradado de Admin")
                onResultado(true)
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error al quitar administrador", e)
                onResultado(false)
            }
    }

    fun escucharMisGrupos(callback: (List<Grupo>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection(coleccionGrupos)
            .whereArrayContains("miembros", uid)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                callback(value?.toObjects(Grupo::class.java) ?: emptyList())
            }
    }

    fun crearAnuncio(grupoId: String, titulo: String, contenido: String) {
        val uid = auth.currentUser?.uid ?: return
        obtenerUsuario(uid) { nombre ->
            val anuncioId = db.collection("anuncios").document().id
            val anuncio = hashMapOf(
                "id" to anuncioId,
                "grupoId" to grupoId,
                "titulo" to titulo,
                "contenido" to contenido,
                "autorId" to uid,
                "autorNombre" to nombre,
                "fecha" to System.currentTimeMillis()
            )
            db.collection("anuncios").document(anuncioId).set(anuncio)
        }
    }

    fun obtenerDatosUsuario(uid: String, onResultado: (MiembroUsuario) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nombre = document.getString("nombre") ?: "Usuario"
                    val correo = document.getString("correo") ?: "Sin correo"
                    val fotoUrl = document.getString("fotoUrl")
                    onResultado(MiembroUsuario(uid, nombre, correo, fotoUrl))
                } else {
                    onResultado(MiembroUsuario(uid, "Usuario Desconocido", "Sin correo", null))
                }
            }
            .addOnFailureListener {
                onResultado(MiembroUsuario(uid, "Error al cargar", "Sin correo", null))
            }
    }

    fun guardarUsuario(uid: String, nombre: String, email: String, fotoPerfil: String = "") {
        val usuario = hashMapOf("uid" to uid, "nombre" to nombre, "email" to email, "fotoPerfil" to fotoPerfil)
        db.collection("usuarios").document(uid).set(usuario)
    }

    fun obtenerUsuario(uid: String, callback: (String) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { callback(it.getString("nombre") ?: "usuario") }
    }

    fun obtenerDatosUsuario(uid: String, callback: (String, String) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener {
                callback(it.getString("nombre") ?: "Usuario", it.getString("email") ?: "")
            }
    }

    fun actualizarNombre(nombre: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).update("nombre", nombre)
    }

    fun eliminarGrupo(grupoId: String) {
        db.collection(coleccionGrupos).document(grupoId).delete()
    }

    fun salirDelGrupo(grupoId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection(coleccionGrupos)
            .document(grupoId)
            .update("miembros", FieldValue.arrayRemove(uid))
            .addOnSuccessListener {
                db.collection(coleccionGrupos).document(grupoId)
                    .update("administradores", FieldValue.arrayRemove(uid))
            }
    }

    fun escucharAnuncios(grupoId: String, callback: (List<Anuncio>) -> Unit) {
        db.collection("anuncios")
            .whereEqualTo("grupoId", grupoId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                callback(value?.toObjects(Anuncio::class.java) ?: emptyList())
            }
    }

    fun escucharTareasMisGrupos(misGruposIds: List<String>, callback: (List<Tarea>) -> Unit) {
        if (misGruposIds.isEmpty()) {
            callback(emptyList())
            return
        }
        db.collection(coleccionEventos)
            .whereIn("grupoId", misGruposIds)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                callback(value?.toObjects(Tarea::class.java) ?: emptyList())
            }
    }

    fun obtenerMiembrosGrupo(grupo: Grupo, callback: (List<Pair<String, String>>) -> Unit) {
        val lista = mutableListOf<Pair<String, String>>()
        if (grupo.miembros.isEmpty()) {
            callback(emptyList())
            return
        }
        var cargados = 0
        grupo.miembros.forEach { uid ->
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener {
                    val nombre = it.getString("nombre") ?: "Usuario"
                    lista.add(Pair(uid, nombre))
                    cargados++
                    if (cargados == grupo.miembros.size) {
                        callback(lista)
                    }
                }
        }
    }

    fun crearEventoGrupo(
        grupoId: String,
        titulo: String,
        fecha: String,
        hora: String,
        fechaFin: String,
        horaFin: String,
        tipo: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val id = db.collection(coleccionEventos).document().id

        val nuevaTareaGrupo = Tarea(
            id = id,
            titulo = titulo,
            fecha = fecha,
            hora = hora,
            fechaFin = fechaFin,
            horaFin = horaFin,
            visibilidad = "grupo",
            usuarioId = uid,
            grupoId = grupoId,
            tipoEvento = tipo
        )

        db.collection(coleccionEventos)
            .document(id)
            .set(nuevaTareaGrupo)
            .addOnSuccessListener { Log.d("FIRESTORE", "$tipo de grupo creado con éxito") }
            .addOnFailureListener { e -> Log.e("FIRESTORE", "Error al crear $tipo", e) }
    }
}