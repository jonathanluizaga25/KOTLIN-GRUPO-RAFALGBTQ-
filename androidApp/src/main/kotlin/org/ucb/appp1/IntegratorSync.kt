package org.ucb.appp1

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import org.ucb.appp1.integrator.CachedConfig
import org.ucb.appp1.integrator.IntegratorStore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object IntegratorSync {
    private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun schedule(context: Context) {
        val manager = WorkManager.getInstance(context)
        manager.enqueueUniquePeriodicWork(
            "integrator-periodic", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<IntegratorWorker>(15, TimeUnit.MINUTES)
                .setConstraints(network).build()
        )
        requestNow(context)
    }

    fun requestNow(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "integrator-now", ExistingWorkPolicy.APPEND_OR_REPLACE,
            OneTimeWorkRequestBuilder<IntegratorWorker>().setConstraints(network).build()
        )
    }

    suspend fun mirrorCounter(count: Long, revision: Long) {
        val ref = FirebaseDatabase.getInstance().getReference("collaborative_counter")
        suspendCancellableCoroutine<Unit> { continuation ->
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(current: MutableData): Transaction.Result {
                    val existingRevision = current.child("revision").getValue(Long::class.java) ?: -1L
                    if (existingRevision > revision) return Transaction.abort()
                    current.value = mapOf("count" to count, "revision" to revision)
                    return Transaction.success(current)
                }
                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (!continuation.isActive) return
                    if (error != null) continuation.resumeWithException(error.toException())
                    else continuation.resume(Unit)
                }
            }, false)
        }
    }
}

class IntegratorWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        var failed = false
        try {
            val remote = FirebaseRemoteConfig.getInstance()
            remote.setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
            ).await()
            remote.setDefaultsAsync(mapOf(
                "welcome_message" to "¡Bienvenido!", "notes_enabled" to true, "maintenance_mode" to false
            )).await()
            remote.fetchAndActivate().await()
            val config = CachedConfig(
                welcomeMessage = remote.getString("welcome_message"),
                notesEnabled = remote.getBoolean("notes_enabled"),
                maintenanceMode = remote.getBoolean("maintenance_mode"),
                syncedAt = System.currentTimeMillis()
            )
            if (IntegratorStore.database(applicationContext).integratorDao().saveAndRecord(config)) {
                PushService.showNotification(applicationContext, "Configuración actualizada", config.welcomeMessage, "settings")
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Do not replace Room's last successful configuration on a failed fetch.
            failed = true
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val token = FirebaseMessaging.getInstance().token.await()
                val installation = FirebaseInstallations.getInstance().id.await()
                firestore.collection("users").document(user.uid).collection("devices").document(installation)
                    .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis())).await()
                val meta = firestore.collection("collaboration").document("summary").get(Source.SERVER).await()
                if (meta.exists()) {
                    IntegratorSync.mirrorCounter(meta.getLong("count") ?: 0, meta.getLong("revision") ?: 0)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failed = true
            }
        }
        return if (failed) Result.retry() else Result.success()
    }
}
