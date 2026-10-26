package org.ucb.appp1.designsystem

import java.util.Locale

enum class LanguageMode { SYSTEM, ES, EN }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

class AppCopy(mode: LanguageMode) {
    private val english = when (mode) {
        LanguageMode.EN -> true
        LanguageMode.ES -> false
        LanguageMode.SYSTEM -> Locale.getDefault().language == "en"
    }

    operator fun get(key: String): String =
        (if (english) en else es)[key] ?: es[key] ?: key

    private companion object {
        val es = mapOf(
            "app" to "Proyecto móvil",
            "home" to "Inicio", "notes" to "Notas", "profile" to "Perfil",
            "settings" to "Ajustes", "login" to "Iniciar sesión", "register" to "Crear cuenta",
            "email" to "Correo electrónico", "password" to "Contraseña",
            "logout" to "Cerrar sesión", "continue_demo" to "Explorar sin Firebase",
            "firebase_missing" to "Falta un google-services.json válido. Agrega tu configuración de Firebase para usar cuenta, notas, configuración remota y notificaciones.",
            "welcome_default" to "¡Bienvenido!", "loading" to "Cargando…",
            "retry" to "Reintentar", "empty" to "No hay elementos todavía.",
            "error" to "No se pudo completar la operación.",
            "auth_error" to "Revisa el correo y la contraseña. Activa Email/Password en Firebase Authentication.",
            "create_account" to "¿No tienes cuenta? Regístrate", "have_account" to "Ya tengo cuenta",
            "star_wars" to "Películas de Star Wars", "episode" to "Episodio",
            "director" to "Director", "release" to "Estreno", "opening" to "Introducción",
            "notes_hint" to "Escribe una nota", "add" to "Agregar", "delete" to "Borrar",
            "notes_disabled" to "Las notas están desactivadas por Remote Config.",
            "notes_error" to "No se pudieron cargar las notas. Revisa la conexión y las reglas de Realtime Database.",
            "notes_empty" to "Todavía no hay notas.",
            "maintenance" to "Aplicación en mantenimiento",
            "maintenance_message" to "Vuelve a intentarlo más tarde.",
            "language" to "Idioma", "theme" to "Apariencia", "system" to "Sistema",
            "spanish" to "Español", "english" to "Inglés", "light" to "Claro",
            "dark" to "Oscuro", "push_token" to "Token FCM",
            "token_unavailable" to "Token no disponible. Comprueba Firebase y Google Play Services.",
            "copy_token" to "Copiar token", "token_copied" to "Token copiado",
            "refresh_config" to "Actualizar configuración", "refresh_token" to "Actualizar token",
            "notification_permission" to "Permite las notificaciones para recibir mensajes de prueba.",
            "user_info" to "Información de usuario", "alias" to "Usuario de GitHub",
            "search" to "Buscar", "back" to "Volver", "unknown_user" to "Usuario no encontrado.",
            "current_user" to "Cuenta", "guest" to "Modo de demostración",
            "sign_in_required" to "Inicia sesión para guardar notas.",
            "notes_save_error" to "No se pudo guardar la nota.",
            "notes_delete_error" to "No se pudo borrar la nota.",
            "invalid_input" to "Completa los campos requeridos.",
            "offline" to "Sin conexión o servicio no disponible.",
            "grpc_title" to "Servidor gRPC",
            "grpc_help" to "Inicia el servidor y pega aquí la URL HTTPS de ngrok.",
            "grpc_url" to "URL del servidor",
            "grpc_test" to "Probar gRPC",
            "grpc_sending" to "Enviando…",
            "grpc_success" to "✓ El servidor respondió LOG_RESULT_OK",
            "grpc_rejected" to "El servidor rechazó el registro.",
            "grpc_error" to "No se pudo conectar al servidor:",
            "collaborative" to "Lista compartida", "collaborative_hint" to "Nuevo elemento",
            "counter" to "Contador RTDB", "counter_pending" to "Contador pendiente de sincronización",
            "cached_items" to "Mostrando datos guardados sin conexión",
            "collaborative_error" to "No se pudo guardar. Comprueba tu conexión y las reglas de Firebase.",
            "config_history" to "Historial de configuración", "history_empty" to "Sin cambios registrados."
        )

        val en = mapOf(
            "app" to "Mobile project",
            "home" to "Home", "notes" to "Notes", "profile" to "Profile",
            "settings" to "Settings", "login" to "Sign in", "register" to "Create account",
            "email" to "Email", "password" to "Password",
            "logout" to "Sign out", "continue_demo" to "Explore without Firebase",
            "firebase_missing" to "A valid google-services.json is missing. Add your Firebase configuration to use accounts, notes, Remote Config and notifications.",
            "welcome_default" to "Welcome!", "loading" to "Loading…",
            "retry" to "Retry", "empty" to "Nothing here yet.",
            "error" to "The operation could not be completed.",
            "auth_error" to "Check your email and password. Enable Email/Password in Firebase Authentication.",
            "create_account" to "New here? Create an account", "have_account" to "I already have an account",
            "star_wars" to "Star Wars films", "episode" to "Episode",
            "director" to "Director", "release" to "Released", "opening" to "Opening crawl",
            "notes_hint" to "Write a note", "add" to "Add", "delete" to "Delete",
            "notes_disabled" to "Notes are disabled by Remote Config.",
            "notes_error" to "Could not load notes. Check the connection and Realtime Database rules.",
            "notes_empty" to "No notes yet.",
            "maintenance" to "App under maintenance",
            "maintenance_message" to "Please try again later.",
            "language" to "Language", "theme" to "Appearance", "system" to "System",
            "spanish" to "Spanish", "english" to "English", "light" to "Light",
            "dark" to "Dark", "push_token" to "FCM token",
            "token_unavailable" to "Token unavailable. Check Firebase and Google Play Services.",
            "copy_token" to "Copy token", "token_copied" to "Token copied",
            "refresh_config" to "Refresh configuration", "refresh_token" to "Refresh token",
            "notification_permission" to "Allow notifications to receive test messages.",
            "user_info" to "User information", "alias" to "GitHub username",
            "search" to "Search", "back" to "Back", "unknown_user" to "User not found.",
            "current_user" to "Account", "guest" to "Demo mode",
            "sign_in_required" to "Sign in to save notes.",
            "notes_save_error" to "Could not save the note.",
            "notes_delete_error" to "Could not delete the note.",
            "invalid_input" to "Complete the required fields.",
            "offline" to "Offline or service unavailable.",
            "grpc_title" to "gRPC server",
            "grpc_help" to "Start the server and paste the ngrok HTTPS URL here.",
            "grpc_url" to "Server URL",
            "grpc_test" to "Test gRPC",
            "grpc_sending" to "Sending…",
            "grpc_success" to "✓ Server replied LOG_RESULT_OK",
            "grpc_rejected" to "The server rejected the log.",
            "grpc_error" to "Could not connect to the server:",
            "collaborative" to "Shared list", "collaborative_hint" to "New item",
            "counter" to "RTDB counter", "counter_pending" to "Counter sync pending",
            "cached_items" to "Showing cached offline data",
            "collaborative_error" to "Could not save. Check your connection and Firebase rules.",
            "config_history" to "Configuration history", "history_empty" to "No changes recorded."
        )
    }
}
