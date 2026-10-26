package org.ucb.appp1

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.ucb.appp1.designsystem.AppButton
import org.ucb.appp1.designsystem.AppCard
import org.ucb.appp1.designsystem.AppCopy
import org.ucb.appp1.designsystem.AppSpacing
import org.ucb.appp1.designsystem.AppTitle
import org.ucb.appp1.designsystem.EmptyView
import org.ucb.appp1.designsystem.ErrorView
import org.ucb.appp1.designsystem.LanguageMode
import org.ucb.appp1.designsystem.LoadingView
import org.ucb.appp1.designsystem.ThemeMode
import org.ucb.appp1.log.GrpcExamClient
import org.ucb.appp1.integrator.IntegratorStore
import java.text.DateFormat
import java.util.Date

@Composable
fun AuthScreen(gateway: FirebaseGateway, copy: AppCopy, onDemo: () -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        AppTitle(copy[if (isRegister) "register" else "login"])
        if (!gateway.ready) Text(copy["firebase_missing"], color = MaterialTheme.colorScheme.error)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(copy["email"]) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(copy["password"]) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (loading) LoadingView(copy["loading"])
        AppButton(
            text = copy[if (isRegister) "register" else "login"],
            enabled = gateway.ready && !loading,
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = copy["invalid_input"]
                } else {
                    loading = true
                    error = null
                    val result: (Boolean) -> Unit = { success ->
                        loading = false
                        if (!success) error = copy["auth_error"]
                    }
                    if (isRegister) gateway.register(email.trim(), password, result)
                    else gateway.signIn(email.trim(), password, result)
                }
            }
        )
        AppButton(
            text = copy[if (isRegister) "have_account" else "create_account"],
            onClick = { isRegister = !isRegister; error = null }
        )
        if (!gateway.ready) AppButton(copy["continue_demo"], onDemo)
    }
}

@Composable
fun MainListScreen(copy: AppCopy, welcomeMessage: String, onFilm: (Film) -> Unit) {
    var films by remember { mutableStateOf<List<Film>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var retry by remember { mutableStateOf(0) }

    LaunchedEffect(retry) {
        loading = true
        error = false
        runCatching { StarWarsService.films() }
            .onSuccess { films = it }
            .onFailure { error = true }
        loading = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        AppTitle(welcomeMessage.ifBlank { copy["welcome_default"] })
        Text(copy["star_wars"], style = MaterialTheme.typography.titleLarge)
        when {
            loading -> LoadingView(copy["loading"])
            error -> ErrorView(copy["offline"], copy["retry"]) { retry++ }
            films.isEmpty() -> EmptyView(copy["empty"])
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                items(films, key = { it.episode }) { film ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Text(film.title, style = MaterialTheme.typography.titleMedium)
                        Text("${copy["episode"]} ${film.episode}")
                        AppButton(copy["opening"], onClick = { onFilm(film) })
                    }
                }
            }
        }
    }
}

@Composable
fun FilmDetailScreen(film: Film, copy: AppCopy, onBack: () -> Unit) {
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        AppButton(copy["back"], onBack)
        AppTitle(film.title)
        Text("${copy["episode"]}: ${film.episode}")
        Text("${copy["director"]}: ${film.director}")
        Text("${copy["release"]}: ${film.releaseDate}")
        HorizontalDivider()
        Text(copy["opening"], style = MaterialTheme.typography.titleMedium)
        Text(film.openingCrawl)
    }
}

@Composable
fun NotesScreen(gateway: FirebaseGateway, copy: AppCopy, snackbar: SnackbarHostState) {
    var noteText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var retry by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val canUse = gateway.ready && gateway.user != null && gateway.flags.notesEnabled

    if (canUse) {
        DisposableEffect(gateway, gateway.user?.uid, retry) {
            loading = true
            error = false
            val stop = gateway.observeNotes(
                onNotes = { notes = it; loading = false; error = false },
                onError = { loading = false; error = true }
            )
            onDispose { stop() }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        AppTitle(copy["notes"])
        when {
            !gateway.flags.notesEnabled -> EmptyView(copy["notes_disabled"])
            !gateway.ready -> EmptyView(copy["firebase_missing"])
            gateway.user == null -> EmptyView(copy["sign_in_required"])
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { if (it.length <= 500) noteText = it },
                        label = { Text(copy["notes_hint"]) },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    AppButton(copy["add"], enabled = noteText.isNotBlank(), onClick = {
                        gateway.addNote(noteText) { success ->
                            if (success) noteText = ""
                            else scope.launch { snackbar.showSnackbar(copy["notes_save_error"]) }
                        }
                    })
                }
                when {
                    loading -> LoadingView(copy["loading"])
                    error -> ErrorView(copy["notes_error"], copy["retry"]) { retry++ }
                    notes.isEmpty() -> EmptyView(copy["notes_empty"])
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                        items(notes, key = { it.id }) { note ->
                            AppCard(Modifier.fillMaxWidth()) {
                                Text(note.text)
                                Spacer(Modifier.height(AppSpacing.small))
                                AppButton(copy["delete"], onClick = {
                                    gateway.deleteNote(note.id) { success ->
                                        if (!success) scope.launch {
                                            snackbar.showSnackbar(copy["notes_delete_error"])
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    gateway: FirebaseGateway,
    copy: AppCopy,
    onUserInfo: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        AppTitle(copy["profile"])
        Text(copy["current_user"], style = MaterialTheme.typography.titleMedium)
        Text(gateway.user?.email ?: copy["guest"])
        AppButton(copy["user_info"], onUserInfo)
        if (gateway.user == null) AppButton(copy["login"], onSignIn)
        else AppButton(copy["logout"], onClick = {
            gateway.signOut()
            onSignOut()
        })
    }
}

@Composable
fun CollaborativeScreen(gateway: FirebaseGateway, copy: AppCopy) {
    val service = remember { CollaborativeGateway() }
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<CollaborativeItem>>(emptyList()) }
    var summary by remember { mutableStateOf<CollaborationCount?>(null) }
    var mirrored by remember { mutableStateOf<CollaborationCount?>(null) }
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }

    if (gateway.user != null) {
        DisposableEffect(gateway.user?.uid) {
            val stopItems = service.observeItems({ list, cached ->
                items = list; fromCache = cached; loading = false
            }, { error = it; loading = false })
            val stopCounter = service.observeCounter({ mirrored = it }, { error = it })
            val stopSummary = service.observeSummary({ value ->
                summary = value
                scope.launch {
                    runCatching { IntegratorSync.mirrorCounter(value.count, value.revision) }
                        .onFailure { error = copy["counter_pending"] }
                }
            }, { error = it })
            onDispose { stopItems(); stopCounter(); stopSummary() }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        AppTitle(copy["collaborative"])
        if (gateway.user == null) {
            EmptyView(copy["sign_in_required"])
        } else {
            Text("${copy["counter"]}: ${mirrored?.count ?: 0}")
            if (summary != null && summary != mirrored) Text(copy["counter_pending"])
            if (fromCache) Text(copy["cached_items"])
            OutlinedTextField(text, { if (it.length <= 500) text = it },
                label = { Text(copy["collaborative_hint"]) }, modifier = Modifier.fillMaxWidth())
            AppButton(copy["add"], enabled = text.isNotBlank() && !busy, onClick = {
                busy = true; error = null
                service.add(text) { ok ->
                    busy = false
                    if (ok) text = "" else error = copy["collaborative_error"]
                }
            })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (loading) LoadingView(copy["loading"])
            else if (items.isEmpty()) EmptyView(copy["empty"])
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                items(items, key = { it.id }) { item ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Text(item.text)
                        if (item.owner == gateway.user?.uid) {
                            AppButton(copy["delete"], enabled = !busy, onClick = {
                                busy = true
                                service.delete(item.id) { ok ->
                                    busy = false
                                    if (!ok) error = copy["collaborative_error"]
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    gateway: FirebaseGateway,
    copy: AppCopy,
    language: LanguageMode,
    theme: ThemeMode,
    grpcServerUrl: String,
    onLanguage: (LanguageMode) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onGrpcServerUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val history by remember { IntegratorStore.database(context).integratorDao().observeHistory() }
        .collectAsState(initial = emptyList())
    var copied by remember { mutableStateOf(false) }
    var grpcLoading by remember { mutableStateOf(false) }
    var grpcResult by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { gateway.refreshToken() }

    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        AppTitle(copy["settings"])
        Text(copy["language"], style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            listOf(
                LanguageMode.SYSTEM to "system",
                LanguageMode.ES to "spanish",
                LanguageMode.EN to "english"
            ).forEach { (mode, label) ->
                AppButton((if (language == mode) "✓ " else "") + copy[label], { onLanguage(mode) })
            }
        }
        Text(copy["theme"], style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            listOf(
                ThemeMode.SYSTEM to "system",
                ThemeMode.LIGHT to "light",
                ThemeMode.DARK to "dark"
            ).forEach { (mode, label) ->
                AppButton((if (theme == mode) "✓ " else "") + copy[label], { onTheme(mode) })
            }
        }
        HorizontalDivider()
        AppButton(copy["refresh_config"], { gateway.refreshConfig() }, enabled = gateway.ready)
        Text(copy["push_token"], style = MaterialTheme.typography.titleMedium)
        SelectionContainer {
            Text(gateway.token ?: copy["token_unavailable"])
        }
        AppButton(copy["refresh_token"], { gateway.refreshToken() }, enabled = gateway.ready)
        AppButton(copy["copy_token"], enabled = gateway.token != null, onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("FCM token", gateway.token))
            copied = true
        })
        if (copied) Text(copy["token_copied"])
        Text(copy["notification_permission"])
        HorizontalDivider()
        Text(copy["grpc_title"], style = MaterialTheme.typography.titleMedium)
        Text(copy["grpc_help"])
        OutlinedTextField(
            value = grpcServerUrl,
            onValueChange = {
                onGrpcServerUrl(it)
                grpcResult = null
            },
            label = { Text(copy["grpc_url"]) },
            placeholder = { Text("https://ejemplo.ngrok-free.dev") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        AppButton(
            text = if (grpcLoading) copy["grpc_sending"] else copy["grpc_test"],
            enabled = grpcServerUrl.isNotBlank() && !grpcLoading,
            onClick = {
                grpcLoading = true
                grpcResult = null
                scope.launch {
                    grpcResult = runCatching {
                        GrpcExamClient.send(
                            context = context.applicationContext,
                            serverUrl = grpcServerUrl.trim(),
                            message = "Prueba gRPC enviada desde la app UCBP1",
                            userId = gateway.user?.uid.orEmpty()
                        )
                    }.fold(
                        onSuccess = { if (it) copy["grpc_success"] else copy["grpc_rejected"] },
                        onFailure = { copy["grpc_error"] + " " + (it.message ?: "") }
                    )
                    grpcLoading = false
                }
            }
        )
        grpcResult?.let {
            Text(
                text = it,
                color = if (it == copy["grpc_success"]) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
        HorizontalDivider()
        Text(copy["config_history"], style = MaterialTheme.typography.titleMedium)
        if (history.isEmpty()) Text(copy["history_empty"])
        history.forEach { change ->
            Text("${DateFormat.getDateTimeInstance().format(Date(change.changedAt))}: ${change.description}")
        }
    }
}

@Composable
fun UserInformationScreen(copy: AppCopy, onBack: () -> Unit) {
    var alias by remember { mutableStateOf("") }
    var requestedAlias by remember { mutableStateOf<String?>(null) }
    var requestId by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf<GithubUser?>(null) }

    LaunchedEffect(requestedAlias, requestId) {
        val query = requestedAlias ?: return@LaunchedEffect
        loading = true
        error = false
        user = null
        runCatching { GithubService.find(query) }
            .onSuccess { user = it }
            .onFailure { error = true }
        loading = false
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        AppButton(copy["back"], onBack)
        AppTitle(copy["user_info"])
        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text(copy["alias"]) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        AppButton(copy["search"], enabled = alias.isNotBlank(), onClick = {
            requestedAlias = alias.trim()
            requestId++
        })
        when {
            loading -> LoadingView(copy["loading"])
            error -> ErrorView(copy["unknown_user"], copy["retry"]) {
                requestedAlias = alias.trim()
                requestId++
            }
            user != null -> AppCard(Modifier.fillMaxWidth()) {
                Text(user!!.name ?: user!!.login, style = MaterialTheme.typography.titleMedium)
                Text("@${user!!.login}")
                user!!.bio?.let { Text(it) }
            }
        }
    }
}
