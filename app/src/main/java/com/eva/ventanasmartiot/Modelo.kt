package com.eva.ventanasmartiot

import com.google.firebase.database.DataSnapshot

/** Los datos de medición SON SIMULADOS para la EVA 2; no provienen del DHT11 real. */
data class EstadoVentana(
    val temperatura: Double = 24.0,
    val humedad: Double = 55.0,
    val abierta: Boolean = true,
    val automatico: Boolean = true,
    val timestamp: Long = 0L,
    val origen: String = "Simulador Android"
)

data class Registro(
    val id: String,
    val temperatura: Double,
    val humedad: Double,
    val abierta: Boolean,
    val automatico: Boolean,
    val evento: String,
    val timestamp: Long
)

fun DataSnapshot.leerEstado(): EstadoVentana = EstadoVentana(
    temperatura = (child("temperatura").value as? Number)?.toDouble() ?: 24.0,
    humedad = (child("humedad").value as? Number)?.toDouble() ?: 55.0,
    abierta = child("abierta").getValue(Boolean::class.java) ?: true,
    automatico = child("automatico").getValue(Boolean::class.java) ?: true,
    timestamp = (child("timestamp").value as? Number)?.toLong() ?: 0L,
    origen = child("origen").getValue(String::class.java) ?: "Simulador Android"
)

fun DataSnapshot.leerRegistro(): Registro = Registro(
    id = key ?: "",
    temperatura = (child("temperatura").value as? Number)?.toDouble() ?: 0.0,
    humedad = (child("humedad").value as? Number)?.toDouble() ?: 0.0,
    abierta = child("abierta").getValue(Boolean::class.java) ?: true,
    automatico = child("automatico").getValue(Boolean::class.java) ?: true,
    evento = child("evento").getValue(String::class.java) ?: "Evento",
    timestamp = (child("timestamp").value as? Number)?.toLong() ?: 0L
)

/** Umbrales idénticos a la EVA 1: cerrar >= 70 %, abrir <= 60 %. */
fun aplicarHisteresis(actual: EstadoVentana, humedad: Double): Boolean = when {
    humedad >= 70.0 -> false
    humedad <= 60.0 -> true
    else -> actual.abierta
}
