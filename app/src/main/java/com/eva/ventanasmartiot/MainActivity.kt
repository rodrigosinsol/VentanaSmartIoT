package com.eva.ventanasmartiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Azul = Color(0xFF185ABC)
private val Claro = Color(0xFFEDF4FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Azul)) {
                VentanaSmartApp()
            }
        }
    }
}

@Composable
private fun VentanaSmartApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var usuario by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            !Config.estaConfigurado -> AvisoConfiguracion()
            usuario == null -> LoginScreen(auth, alIngresar = { usuario = auth.currentUser })
            else -> PanelPrincipal(
                usuario = usuario!!,
                salir = { auth.signOut(); usuario = null }
            )
        }
    }
}

@Composable
private fun AvisoConfiguracion() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Falta conectar Firebase", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Abre Config.kt y reemplaza PEGA_AQUI_LA_URL_DE_REALTIME_DATABASE por la URL de tu base de datos.")
        Spacer(Modifier.height(8.dp))
        Text("También debes agregar app/google-services.json y activar Authentication + Realtime Database.")
    }
}

@Composable
private fun LoginScreen(auth: FirebaseAuth, alIngresar: () -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    fun autenticar(registro: Boolean) {
        val email = correo.trim()
        if (!email.contains("@") || clave.length < 6) {
            mensaje = "Escribe un correo válido y una contraseña de al menos 6 caracteres."
            return
        }
        cargando = true
        mensaje = ""
        val tarea = if (registro) auth.createUserWithEmailAndPassword(email, clave)
                    else auth.signInWithEmailAndPassword(email, clave)
        tarea.addOnCompleteListener { resultado ->
            cargando = false
            if (resultado.isSuccessful) alIngresar()
            else mensaje = "No se pudo ${if (registro) "registrar" else "ingresar"}: " +
                    (resultado.exception?.localizedMessage ?: "revisa tu conexión y tus credenciales")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("VentanaSmart IoT", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Azul)
        Spacer(Modifier.height(4.dp))
        Text("Control de ventanas • EVA 2", color = Color.DarkGray)
        Spacer(Modifier.height(30.dp))
        Text("Accede a tu sistema", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo electrónico") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = clave, onValueChange = { clave = it },
            label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { autenticar(false) }, enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (cargando) "Conectando..." else "Iniciar sesión") }
        OutlinedButton(
            onClick = { autenticar(true) }, enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Crear cuenta") }
        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Para demostrar conexión entre 2 celulares, inicia sesión con la MISMA cuenta en ambos.",
            fontSize = 13.sp, color = Color.DarkGray
        )
    }
}

@Composable
private fun PanelPrincipal(usuario: FirebaseUser, salir: () -> Unit) {
    val uid = usuario.uid
    var estado by remember(uid) { mutableStateOf<EstadoVentana?>(null) }
    var historial by remember(uid) { mutableStateOf<List<Registro>>(emptyList()) }
    var aviso by remember(uid) { mutableStateOf("Conectando con Firebase...") }
    var ocupado by remember(uid) { mutableStateOf(false) }
    var pagina by rememberSaveable { mutableStateOf("control") }

    DisposableEffect(uid) {
        val estadoRef = Repositorio.estadoRef(uid)
        val historialQuery = Repositorio.historialRef(uid).orderByChild("timestamp").limitToLast(30)
        val estadoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    estado = snapshot.leerEstado()
                    aviso = "Conectado • datos sincronizados"
                } else {
                    aviso = "Creando estado inicial..."
                    Repositorio.inicializar(uid) { ok, error ->
                        if (!ok) aviso = "Error al iniciar: $error"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                aviso = "Sin acceso a datos: ${error.message}"
            }
        }
        val historialListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historial = snapshot.children.map { it.leerRegistro() }.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {
                aviso = "Error al leer historial: ${error.message}"
            }
        }
        estadoRef.addValueEventListener(estadoListener)
        historialQuery.addValueEventListener(historialListener)
        onDispose {
            estadoRef.removeEventListener(estadoListener)
            historialQuery.removeEventListener(historialListener)
        }
    }

    fun guardar(nuevo: EstadoVentana, evento: String) {
        if (ocupado) return
        ocupado = true
        aviso = "Guardando cambios..."
        Repositorio.guardar(uid, nuevo, evento) { ok, error ->
            ocupado = false
            aviso = if (ok) "Cambio guardado y sincronizado" else "No se pudo guardar: $error"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("VentanaSmart IoT", style = MaterialTheme.typography.titleLarge, color = Azul)
                Text(usuario.email ?: "Usuario", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = salir) { Text("Salir") }
        }
        Spacer(Modifier.height(8.dp))
        Text(aviso, style = MaterialTheme.typography.labelMedium,
            color = if (aviso.startsWith("Error") || aviso.startsWith("Sin acceso")) MaterialTheme.colorScheme.error else Azul)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = pagina == "control", onClick = { pagina = "control" }, label = { Text("Control") })
            FilterChip(selected = pagina == "historial", onClick = { pagina = "historial" }, label = { Text("Historial") })
        }
        Spacer(Modifier.height(8.dp))
        val actual = estado
        if (actual == null) {
            CircularProgressIndicator()
        } else if (pagina == "control") {
            ControlScreen(actual, ocupado, alGuardar = ::guardar)
        } else {
            HistorialScreen(historial)
        }
    }
}

@Composable
private fun ControlScreen(actual: EstadoVentana, ocupado: Boolean, alGuardar: (EstadoVentana, String) -> Unit) {
    var temperatura by remember { mutableFloatStateOf(24f) }
    var humedad by remember { mutableFloatStateOf(55f) }
    var yaCargado by remember { mutableStateOf(false) }
    LaunchedEffect(actual.timestamp) {
        if (!yaCargado) {
            temperatura = actual.temperatura.toFloat()
            humedad = actual.humedad.toFloat()
            yaCargado = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(colors = CardDefaults.cardColors(containerColor = Claro), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("ESTADO ACTUAL", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Text(if (actual.abierta) "VENTANA ABIERTA" else "VENTANA CERRADA", fontWeight = FontWeight.Bold, fontSize = 23.sp, color = Azul)
                Text(if (actual.automatico) "Modo: Automático" else "Modo: Manual")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("🌡 ${formato(actual.temperatura)} °C", fontWeight = FontWeight.Medium)
                    Text("💧 ${formato(actual.humedad)} %", fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Control remoto simulado", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { alGuardar(actual.copy(abierta = true, automatico = false), "Apertura manual") }, enabled = !ocupado, modifier = Modifier.weight(1f)) { Text("ABRIR") }
            Button(onClick = { alGuardar(actual.copy(abierta = false, automatico = false), "Cierre manual") }, enabled = !ocupado, modifier = Modifier.weight(1f)) { Text("CERRAR") }
        }
        OutlinedButton(
            onClick = {
                val abierto = aplicarHisteresis(actual, actual.humedad)
                alGuardar(actual.copy(abierta = abierto, automatico = true), "Modo automático activado")
            },
            enabled = !ocupado, modifier = Modifier.fillMaxWidth()
        ) { Text("ACTIVAR MODO AUTOMÁTICO") }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(14.dp))
        Text("Simular lectura DHT11", style = MaterialTheme.typography.titleMedium)
        Text("Valores de prueba: NO son mediciones reales de Arduino.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Text("Temperatura: ${formato(temperatura.toDouble())} °C")
        Slider(value = temperatura, onValueChange = { temperatura = it }, valueRange = 10f..40f)
        Text("Humedad: ${formato(humedad.toDouble())} %")
        Slider(value = humedad, onValueChange = { humedad = it }, valueRange = 0f..100f)
        Button(
            onClick = {
                val nuevaHumedad = humedad.toDouble()
                val abierto = if (actual.automatico) aplicarHisteresis(actual, nuevaHumedad) else actual.abierta
                val nuevo = actual.copy(
                    temperatura = temperatura.toDouble(),
                    humedad = nuevaHumedad,
                    abierta = abierto
                )
                alGuardar(nuevo, "Lectura de sensor simulada")
            },
            enabled = !ocupado, modifier = Modifier.fillMaxWidth()
        ) { Text("ENVIAR LECTURA A FIREBASE") }
        Spacer(Modifier.height(16.dp))
        Text("Regla automática del proyecto: si humedad ≥ 70 %, cierra; si ≤ 60 %, abre; entre 60 % y 70 %, mantiene su estado.",
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun HistorialScreen(registros: List<Registro>) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Últimos registros", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        if (registros.isEmpty()) Text("Todavía no hay eventos. Vuelve a Control y guarda una acción o lectura.")
        registros.forEach { registro ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(registro.evento, fontWeight = FontWeight.SemiBold)
                    Text("${formato(registro.temperatura)} °C • ${formato(registro.humedad)} % • " +
                        (if (registro.abierta) "Abierta" else "Cerrada"))
                    Text("${if (registro.automatico) "Automático" else "Manual"} • ${fecha(registro.timestamp)}",
                        style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }
        }
    }
}

private fun formato(valor: Double): String = String.format(Locale.getDefault(), "%.1f", valor)
private fun fecha(ms: Long): String = if (ms <= 0) "Sin fecha" else
    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(ms))
