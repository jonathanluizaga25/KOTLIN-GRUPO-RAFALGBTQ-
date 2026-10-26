package org.ucb.appp1

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import org.ucb.appp1.integrator.CachedConfig

data class RemoteFlags(
    val welcomeMessage: String = "",
    val notesEnabled: Boolean = true,
    val maintenanceMode: Boolean = false
)

data class Note(val id: String, val text: String)

class FirebaseGateway(private val context: Context) {
    val ready: Boolean = FirebaseApp.getApps(context).isNotEmpty()

    private val auth = if (ready) FirebaseAuth.getInstance() else null
    private val remote = if (ready) FirebaseRemoteConfig.getInstance() else null
    private fun notesRef(): DatabaseReference? {
        val uid = auth?.currentUser?.uid ?: return null
        return runCatching { FirebaseDatabase.getInstance().getReference("notes").child(uid) }
            .getOrNull()
    }

    var user: FirebaseUser? by mutableStateOf(auth?.currentUser)
        private set
    var flags by mutableStateOf(RemoteFlags())
        private set
    var token: String? by mutableStateOf(null)
        private set

    init {
        auth?.addAuthStateListener {
            user = it.currentUser
            if (it.currentUser != null) IntegratorSync.requestNow(context)
        }
        remote?.let { config ->
            config.setDefaultsAsync(
                mapOf(
                    "welcome_message" to "¡Bienvenido!",
                    "notes_enabled" to true,
                    "maintenance_mode" to false
                )
            )
            config.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    IntegratorSync.requestNow(context)
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    // Keep the last activated values if the realtime connection fails.
                }
            })
        }
        refreshToken()
    }

    fun publishCachedConfig(config: CachedConfig) {
        flags = RemoteFlags(
            welcomeMessage = config.welcomeMessage,
            notesEnabled = config.notesEnabled,
            maintenanceMode = config.maintenanceMode
        )
    }

    fun refreshConfig() {
        if (ready) IntegratorSync.requestNow(context)
    }

    fun refreshToken() {
        if (!ready) return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token = it }
            .addOnFailureListener { token = null }
    }

    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { onResult(it.isSuccessful) }
            ?: onResult(false)
    }

    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { onResult(it.isSuccessful) }
            ?: onResult(false)
    }

    fun signOut() { auth?.signOut() }

    fun observeNotes(
        onNotes: (List<Note>) -> Unit,
        onError: () -> Unit
    ): () -> Unit {
        val ref = notesRef() ?: run { onError(); return {} }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onNotes(snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val text = child.child("text").getValue(String::class.java) ?: return@mapNotNull null
                    Note(id, text)
                }.reversed())
            }

            override fun onCancelled(error: DatabaseError) { onError() }
        }
        ref.addValueEventListener(listener)
        return { ref.removeEventListener(listener) }
    }

    fun addNote(text: String, onResult: (Boolean) -> Unit) {
        notesRef()?.push()?.setValue(mapOf("text" to text.trim()))
            ?.addOnCompleteListener { onResult(it.isSuccessful) }
            ?: onResult(false)
    }

    fun deleteNote(id: String, onResult: (Boolean) -> Unit) {
        notesRef()?.child(id)?.removeValue()
            ?.addOnCompleteListener { onResult(it.isSuccessful) }
            ?: onResult(false)
    }
}
