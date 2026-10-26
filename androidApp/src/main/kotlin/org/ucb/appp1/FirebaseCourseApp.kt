package org.ucb.appp1

import android.content.Context
import android.content.res.Configuration
import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import org.ucb.appp1.designsystem.AppCopy
import org.ucb.appp1.designsystem.AppSpacing
import org.ucb.appp1.designsystem.AppTheme
import org.ucb.appp1.designsystem.AppTitle
import org.ucb.appp1.designsystem.LanguageMode
import org.ucb.appp1.designsystem.ThemeMode
import org.ucb.appp1.integrator.IntegratorStore

private enum class Route { MAIN_LIST, NOTES, COLLABORATIVE, PROFILE, SETTINGS, FILM, USER_INFO }

private const val DEFAULT_GRPC_SERVER_URL = "https://silly-occultist-blazer.ngrok-free.dev"

@Composable
fun FirebaseCourseApp(destination: String? = null, onDestinationConsumed: () -> Unit = {}) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE) }
    val gateway = remember { FirebaseGateway(context.applicationContext) }
    var language by remember {
        mutableStateOf(enumValueOrDefault(preferences.getString("language", null), LanguageMode.SYSTEM))
    }
    var theme by remember {
        mutableStateOf(enumValueOrDefault(preferences.getString("theme", null), ThemeMode.SYSTEM))
    }
    var grpcServerUrl by remember {
        mutableStateOf(
            preferences.getString("grpc_server_url", DEFAULT_GRPC_SERVER_URL)
                ?: DEFAULT_GRPC_SERVER_URL
        )
    }
    val copy = remember(language) { AppCopy(language) }
    val isDark = when (theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }
    var demo by remember { mutableStateOf(false) }
    var route by remember { mutableStateOf(Route.MAIN_LIST) }
    var selectedFilm by remember { mutableStateOf<Film?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(gateway) {
        IntegratorStore.database(context).integratorDao().observeConfig().collect { cached ->
            if (cached != null) gateway.publishCachedConfig(cached)
        }
    }
    LaunchedEffect(destination) {
        if (destination != null) {
            route = when (destination.lowercase()) {
                "notes" -> Route.NOTES
                "collaborative", "collaboration" -> Route.COLLABORATIVE
                "profile" -> Route.PROFILE
                "settings" -> Route.SETTINGS
                else -> Route.MAIN_LIST
            }
            onDestinationConsumed()
        }
    }

    AppTheme(isDark) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
            gateway.flags.maintenanceMode -> MaintenanceScreen(copy)
            gateway.user == null && !demo -> AuthScreen(gateway, copy, onDemo = { demo = true })
            else -> Scaffold(
                snackbarHost = { SnackbarHost(snackbar) },
                bottomBar = {
                    NavigationBar {
                        listOf(
                            Triple(Route.MAIN_LIST, "⌂", "home"),
                            Triple(Route.NOTES, "☷", "notes"),
                            Triple(Route.COLLABORATIVE, "♧", "collaborative"),
                            Triple(Route.PROFILE, "●", "profile"),
                            Triple(Route.SETTINGS, "⚙", "settings")
                        ).forEach { (target, symbol, label) ->
                            NavigationBarItem(
                                selected = route == target ||
                                    (route == Route.FILM && target == Route.MAIN_LIST) ||
                                    (route == Route.USER_INFO && target == Route.PROFILE),
                                onClick = { route = target },
                                icon = { Text(symbol) },
                                label = { Text(copy[label]) },
                                alwaysShowLabel = true
                            )
                        }
                    }
                }
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).padding(AppSpacing.medium)) {
                    when (route) {
                        Route.MAIN_LIST -> MainListScreen(copy, gateway.flags.welcomeMessage) {
                            selectedFilm = it
                            route = Route.FILM
                        }
                        Route.NOTES -> NotesScreen(gateway, copy, snackbar)
                        Route.COLLABORATIVE -> CollaborativeScreen(gateway, copy)
                        Route.PROFILE -> ProfileScreen(gateway, copy,
                            onUserInfo = { route = Route.USER_INFO },
                            onSignIn = { demo = false },
                            onSignOut = { demo = false; route = Route.MAIN_LIST }
                        )
                        Route.SETTINGS -> SettingsScreen(
                            gateway = gateway,
                            copy = copy,
                            language = language,
                            theme = theme,
                            grpcServerUrl = grpcServerUrl,
                            onLanguage = {
                                language = it
                                preferences.edit().putString("language", it.name).apply()
                            },
                            onTheme = {
                                theme = it
                                preferences.edit().putString("theme", it.name).apply()
                            },
                            onGrpcServerUrl = {
                                grpcServerUrl = it
                                preferences.edit().putString("grpc_server_url", it).apply()
                            }
                        )
                        Route.FILM -> selectedFilm?.let { film ->
                            FilmDetailScreen(film, copy) { route = Route.MAIN_LIST }
                        }
                        Route.USER_INFO -> UserInformationScreen(copy) { route = Route.PROFILE }
                    }
                }
            }
            }
        }
    }
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback

@Composable
private fun MaintenanceScreen(copy: AppCopy) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        AppTitle(copy["maintenance"])
        Text(copy["maintenance_message"], style = MaterialTheme.typography.bodyLarge)
    }
}
