# Uso completo desde Android Studio

Abre como proyecto la carpeta `ucbp1_firebase_independiente`. Android Studio
importará tres módulos:

- `androidApp`: aplicación Android y Firebase.
- `shared`: lógica compartida, cliente Wire gRPC y WorkManager.
- `grpcServer`: servidor Spring Boot gRPC.

Después de que termine **Gradle Sync**, el selector de ejecuciones muestra:

- **UCBP1 - Android**: compila, instala y abre la app en el dispositivo elegido.
- **UCBP1 - Servidor gRPC**: ejecuta solamente Spring Boot en el puerto 9090.
- **UCBP1 - Servidor y ngrok**: ejecuta el servidor y publica la URL HTTPS.

Para el examen, selecciona **UCBP1 - Servidor y ngrok** y pulsa Run. Cuando la
consola muestre que ngrok está activo, selecciona **UCBP1 - Android**, elige un
emulador o teléfono y pulsa Run. En la aplicación abre **Ajustes > Servidor gRPC** y pulsa
**Probar gRPC**.

Para detener el backend usa el botón rojo **Stop** de Android Studio. El script
cierra también el proceso de Spring Boot que inició.

Descarga `androidApp/google-services.json` desde Firebase Console: ese archivo no
se distribuye en Git. Instala ngrok desde su sitio oficial y configura tu token
en tu cuenta local; el token no se incluye en el proyecto.
