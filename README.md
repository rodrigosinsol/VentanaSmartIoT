# VentanaSmart IoT — EVA 2 Aplicaciones Móviles para IoT

**INACAP • TI3042 • Unidad 2 • Propuesta basada en EVA 1 (Control Automático de Ventanas).**

## Qué hace

Aplicación Android creada con **Kotlin + Jetpack Compose**, que simula el monitoreo y control de una ventana inteligente. Permite iniciar sesión y registrar usuarios con Firebase Authentication, abrir/cerrar ventana, seleccionar modo automático (humedad >=70% cierra y <=60% abre), simular datos del DHT11, observar cambios en tiempo real desde **dos celulares con la misma cuenta**, y ver un historial guardado en Firebase Realtime Database.

**IMPORTANTE:** esta versión de la EVA 2 usa un simulador de sensor. No se conecta físicamente al Arduino UNO/HC-05. Se basa en la problemática y umbrales de la EVA 1; su integración física es trabajo posterior.

## EMPIEZA POR `INSTRUCCIONES_PASO_A_PASO.md`

Este paquete contiene archivos fuente completos, pero **no incluye un APK compilado**, ni `google-services.json`, ni el Gradle Wrapper JAR. Se debe abrir con Android Studio, configurar Firebase y generar la APK desde el computador. Leer las instrucciones antes de ejecutar.

## Archivos relevantesr
- `app/src/main/java/com/eva/ventanasmartiot/`: código Android escrito en Kotlin
- `app/src/main/java/com/eva/ventanasmartiot/Config.kt`: URL pendiente de Firebase
- `firebase.database.rules.json`: permisos y validaciones para Firebase
- `MOCKUP.svg`: diseño de pantallas del proyecto
- `PROPUESTA_Y_DECISIONES.md`: problema, arquitectura, seguridad y justificación
- `CHECKLIST_RUBRICA.md`: criterios de la escala de apreciación
- `PRUEBAS_Y_PRESENTACION.md`: pruebas y guion para exponer
- `ENLACE_GITHUB_PARA_AAI.txt`: plantilla de archivo de entrega, completar con URL real

## Privacidad y seguridad

- Contraseñas: las procesa Firebase Authentication; no se guardan en Realtime Database.
- Los datos están bajo `usuarios/{uid}` y las reglas permiten acceder únicamente si `auth.uid == uid`.
- Firebase usa HTTPS/TLS; no se habilita tráfico HTTP sin cifrado en el manifest.
- En la demo se usan cuentas de prueba. **La aplicación no es un sistema de seguridad industrial certificado bajo ISO/IEC 27400**; aplica prácticas alineadas con el estándar en el alcance educativo.

## Fuentes oficiales

- https://developer.android.com/studio
- https://developer.android.com/develop/ui/compose
- https://firebase.google.com/docs/android/setup
- https://firebase.google.com/docs/auth/android/password-auth
- https://firebase.google.com/docs/database/android/read-and-write
- https://firebase.google.com/docs/database/security
