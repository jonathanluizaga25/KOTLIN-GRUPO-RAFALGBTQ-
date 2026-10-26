# Configuración de Firebase

La aplicación Android usa el identificador `org.ucb.appp1`. El archivo
`androidApp/google-services.json` no está incluido en Git: descárgalo desde
Firebase Console antes de compilar y colócalo en esa ubicación.

## Estado actual

- Authentication con correo y contraseña: habilitado y probado.
- Realtime Database en `us-central1`: reglas publicadas y probadas.
- Remote Config versión 1: publicado y recibido por la app.
- Cloud Messaging: inicialización, permiso, canal y generación del token probados.

1. Registra una app Android con el identificador `org.ucb.appp1` en tu proyecto
   Firebase. Crea Realtime Database y descarga después el archivo actualizado
   `google-services.json` a `androidApp/`, para incluir la URL de la base de datos.
2. Activa **Authentication → Sign-in method → Email/Password** para Login y Register.
3. Crea **Realtime Database** y publica las reglas de
   [`firebase/database.rules.json`](firebase/database.rules.json). Cada cuenta
   puede leer y escribir solo en `notes/<uid>`. Si usas Firebase CLI, el archivo
   `firebase.json` ya apunta a las reglas; selecciona tu proyecto y ejecuta
   `firebase deploy --only database`.
4. En **Remote Config**, crea y publica:

   | Clave | Tipo | Valor inicial |
   | --- | --- | --- |
   | `welcome_message` | String | `¡Bienvenido a UCBP1!` |
   | `notes_enabled` | Boolean | `true` |
   | `maintenance_mode` | Boolean | `false` |

5. En **Cloud Messaging**, envía una notificación de prueba al token que aparece
   en Ajustes. En Android 13+ acepta el permiso de notificaciones cuando se
   solicite. El canal de mensajes se crea al iniciar la app.

Para compilar: `./gradlew :androidApp:assembleDebug`. Si se copia el proyecto a
otro equipo, conserva o vuelve a descargar `google-services.json` desde la
configuración de la app Android en Firebase Console.

La pantalla Inicio obtiene películas de `https://swapi.dev/api/films/` y la
pantalla de usuario consulta la API pública de GitHub. Ambas requieren Internet.
