package com.eva.ventanasmartiot

/**
 * Pega EXACTAMENTE la URL que muestra Firebase Console > Realtime Database.
 * Ejemplo: https://mi-proyecto-default-rtdb.firebaseio.com
 * ¡No usar la URL del sitio de Firebase ni la ID del proyecto!
 */
object Config {
    const val DATABASE_URL = "https://ventanasmartiot-eva2-default-rtdb.firebaseio.com"

    val estaConfigurado: Boolean
        get() = DATABASE_URL.startsWith("https://") &&
                (DATABASE_URL.contains("firebaseio.com") || DATABASE_URL.contains("firebasedatabase.app"))
}
