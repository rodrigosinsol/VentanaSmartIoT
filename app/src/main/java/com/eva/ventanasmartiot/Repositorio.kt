package com.eva.ventanasmartiot

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

/** Solo datos bajo usuarios/{uid}; las reglas comprueban auth.uid == uid. */
object Repositorio {
    private val raiz: DatabaseReference
        get() = FirebaseDatabase.getInstance(Config.DATABASE_URL).reference

    fun estadoRef(uid: String): DatabaseReference = raiz.child("usuarios").child(uid).child("estado")
    fun historialRef(uid: String): DatabaseReference = raiz.child("usuarios").child(uid).child("historial")

    fun inicializar(uid: String, resultado: (Boolean, String) -> Unit) {
        val inicial = mapOf(
            "temperatura" to 24.0,
            "humedad" to 55.0,
            "abierta" to true,
            "automatico" to true,
            "timestamp" to ServerValue.TIMESTAMP,
            "origen" to "Simulador Android"
        )
        estadoRef(uid).setValue(inicial).addOnCompleteListener { task ->
            resultado(task.isSuccessful, task.exception?.localizedMessage ?: "")
        }
    }

    /** Una escritura multirruta actualiza el estado y crea un registro a la vez. */
    fun guardar(uid: String, nuevo: EstadoVentana, evento: String, resultado: (Boolean, String) -> Unit) {
        val id = historialRef(uid).push().key
        if (id == null) {
            resultado(false, "No se pudo generar el identificador del registro")
            return
        }
        val estado = mapOf<String, Any>(
            "temperatura" to nuevo.temperatura,
            "humedad" to nuevo.humedad,
            "abierta" to nuevo.abierta,
            "automatico" to nuevo.automatico,
            "timestamp" to ServerValue.TIMESTAMP,
            "origen" to "Simulador Android"
        )
        val historial = mapOf<String, Any>(
            "temperatura" to nuevo.temperatura,
            "humedad" to nuevo.humedad,
            "abierta" to nuevo.abierta,
            "automatico" to nuevo.automatico,
            "timestamp" to ServerValue.TIMESTAMP,
            "evento" to evento
        )
        val cambios: Map<String, Any> = mapOf(
            "usuarios/$uid/estado" to estado,
            "usuarios/$uid/historial/$id" to historial
        )
        raiz.updateChildren(cambios).addOnCompleteListener { task ->
            resultado(task.isSuccessful, task.exception?.localizedMessage ?: "")
        }
    }
}
