# LEER PRIMERO — instalación y ejecución, explicadas desde cero (Windows)

> Este ZIP entrega archivos de código preparados para Android Studio. **No incluye una APK ya compilada**, ni la configuración personal de Firebase, ni el `gradle-wrapper.jar` binario. La generación de la APK y las pruebas requieren seguir estos pasos en tu PC.

## PASO 1 — instalar Android Studio
1. Abre https://developer.android.com/studio desde Windows y descarga el instalador EXE.
2. Ejecútalo, elige opciones recomendadas y termina el Setup Wizard.
3. Si te pide instalar Android SDK, Google Play SDK u otros componentes necesarios, acepta.
4. Ten conexión a Internet: Gradle descargará dependencias la primera vez.

## PASO 2 — crear un proyecto vacío UNA sola vez para obtener Gradle Wrapper
1. Android Studio > **New Project** > plantilla **Empty Activity** (Jetpack Compose).
2. Nombre: **VentanaSmartIoT**.
3. Package name: **com.eva.ventanasmartiot**.
4. Language: **Kotlin**. Minimum SDK: **API 26**.
5. Pulsa **Finish** y espera a que sincronice.
6. Cierra Android Studio (o el proyecto) antes de copiar archivos.

## PASO 3 — pegar archivos de este ZIP al proyecto nuevo
1. Descomprime este ZIP a una carpeta fácil de encontrar.
2. Abre el Explorador de archivos en dos ventanas:
   - La carpeta que descomprimiste **VentanaSmartIoT** (la que contiene README.md, app, settings.gradle.kts...).
   - La carpeta del proyecto que Android Studio creó (usualmente `C:\Users\TU_USUARIO\AndroidStudioProjects\VentanaSmartIoT`).
3. **Copia el contenido de la carpeta descomprimida y pégalo encima de tu proyecto creado.**
4. Cuando Windows pregunte **reemplazar archivos**, acepta. No borres archivos que no vengan en el ZIP.
5. En particular **conserva** `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar` que Android Studio generó: son el lanzador de Gradle. El ZIP solo reemplaza `gradle-wrapper.properties`.
6. Reabre el proyecto con **File > Open** y espera el `Gradle Sync`.
7. Si aparece solicitud de instalar SDK 37, elige **Install SDK**. Si pide usar JDK, elige el **Embedded JDK** de Android Studio.

## PASO 4 — Firebase: crear el proyecto
1. Abre https://console.firebase.google.com e inicia sesión con tu cuenta Google.
2. Selecciona **Create project / Crear proyecto** y ponle `VentanaSmartIoT`.
3. Puedes desactivar Google Analytics para simplificar, si te da la opción.
4. En el proyecto, añade una app **Android**. El **Android package name** debe ser exactamente: `com.eva.ventanasmartiot`.
5. Descarga `google-services.json`.
6. Copia ese archivo dentro de la carpeta **app/** del proyecto Android Studio, al mismo nivel que `app/build.gradle.kts`. No cambies su nombre. No lo compartas en capturas ni lo subas a GitHub.

## PASO 5 — habilitar autenticación (Firebase)
1. Firebase Console > **Authentication** > **Get started**.
2. Abre **Sign-in method** y habilita **Email/Password (Correo/contraseña)**; guarda.
3. No hace falta escribir manualmente usuarios en Firebase: la app tiene botón **Crear cuenta**.

## PASO 6 — crear Realtime Database y pegar las reglas
1. Firebase Console > **Build > Realtime Database** > **Create Database**.
2. Elige ubicación que te muestre Firebase. Elige empezar en **modo bloqueado** (no en modo de prueba abierto).
3. En Realtime Database abre pestaña **Rules / Reglas**.
4. Abre `firebase.database.rules.json` de este ZIP, selecciona TODO y copia el JSON.
5. Reemplaza las reglas de consola por el contenido copiado y presiona **Publish / Publicar**.
6. Si Firebase marca error de reglas, conserva el mensaje para corregirlo antes de probar. Nunca cambies a reglas `.read: true` o `.write: true` para “arreglar” el error.

## PASO 7 — agregar la URL de Firebase al código
1. Firebase > Realtime Database > pestaña Data. Copia la URL completa de la base (debe iniciar `https://`).
2. En Android Studio abre: **app > src > main > java > com.eva.ventanasmartiot > Config.kt**.
3. Encuentra `PEGA_AQUI_LA_URL_DE_REALTIME_DATABASE` y reemplázala por tu URL, **sin borrar las comillas**.
4. Ejemplo (solo ejemplo, NO usar literalmente):
   `const val DATABASE_URL = "https://mi-app-default-rtdb.firebaseio.com"`
5. Haz clic **Sync Now** si aparece arriba del editor.

## PASO 8 — ejecutar en teléfono Android (recomendado si el PC va lento)
1. En tu teléfono Android abre **Ajustes > Acerca del teléfono > Información de software**; toca siete veces “Número de compilación” para habilitar Opciones de desarrollador (la ruta varía por fabricante).
2. En **Opciones de desarrollador**, habilita **Depuración USB**.
3. Conecta el teléfono al PC con un cable USB de datos y acepta la ventana “Permitir depuración USB” en el teléfono.
4. Arriba en Android Studio selecciona el dispositivo conectado, luego presiona el botón verde **Run ▶**.
5. Debes ver el login. Pulsa **Crear cuenta**, escribe un correo de prueba y una contraseña segura de al menos 6 caracteres.
6. Si falla Gradle Sync, captura **Build Output** o el error exacto; no borres archivos al azar.

## PASO 9 — probar la conectividad con DOS Android
1. En el primer teléfono inicia sesión y pulsa **CERRAR**.
2. Instala la app también en el segundo teléfono (Run de nuevo o comparte la APK localmente).
3. Inicia sesión con **el mismo usuario y contraseña** en el segundo teléfono.
4. Cambia el estado en el primer teléfono; confirma que se actualiza el segundo. En modo automático, humedad de 80% cierra y 50% abre.
5. Abre Historial y Firebase Console > Realtime Database > Data para comprobar los registros.
6. Revisa las pruebas y capturas de `PRUEBAS_Y_PRESENTACION.md`.

## PASO 10 — generar APK
1. Menú **Build > Build Bundle(s) / APK(s) > Build APK(s)** (los nombres pueden cambiar ligeramente).
2. Si finaliza correctamente, Android Studio mostrará una notificación con **Locate**.
3. El archivo se guarda generalmente en `app/build/outputs/apk/debug/app-debug.apk`.
4. Ese APK se puede instalar en el segundo Android; usa tu propio dispositivo y concede permisos de instalación solo temporalmente si Android lo exige.

## PASO 11 — subir el proyecto a GitHub y entregar en AAI
1. Crea un repositorio en https://github.com/new llamado **VentanaSmartIoT**.
2. Sube `app/src`, archivos `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, README, docs, `gradle/wrapper/gradle-wrapper.properties` y reglas de Firebase.
3. **NO SUBAS** `local.properties`, `.gradle`, carpetas `build` o `app/google-services.json`.
4. Si utilizas el proyecto Empty Activity creado por Android Studio, incluye también los archivos normales `gradlew`, `gradlew.bat`, y `gradle/wrapper/gradle-wrapper.jar` (son parte del wrapper estándar).
5. Copia la URL pública o visible para el profesor del repositorio.
6. Abre `ENLACE_GITHUB_PARA_AAI.txt`, cambia la línea entre corchetes por la URL REAL y guarda.
7. Sube ese TXT a la tarea AAI **antes del término de la clase correspondiente**. La guía indica que NO se aceptan entregas por correo.
8. Verifica de nuevo el plazo en AAI; **no consta una fecha exacta en la guía proporcionada**.

## AYUDA SI APARECEN ERRORES
- `File google-services.json is missing`: poner el archivo de Firebase en `app/`.
- `Firebase Database URL`: revisar Config.kt y copiar URL exacta de Realtime Database.
- `Permission denied`: publicar reglas y comprobar que estás dentro con tu cuenta.
- `Cannot resolve` o `SDK 37 missing`: espera sincronización y acepta instalación de SDK solicitado.
- `GradleWrapperMain not found`: no se copiaron/conservaron el `gradle-wrapper.jar`, `gradlew` y `gradlew.bat` del Empty Activity de Android Studio.
- `Email/password not enabled`: habilitar el método de acceso desde Firebase Console.
- Errores de conexión: probar Internet del PC y móvil, cerrar sesión e ingresar otra vez.
