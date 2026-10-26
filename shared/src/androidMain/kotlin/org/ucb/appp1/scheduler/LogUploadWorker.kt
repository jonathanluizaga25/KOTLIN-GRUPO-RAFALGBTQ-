package org.ucb.appp1.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.ucb.appp1.log.GrpcExamClient

class LogUploadWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val preferences = applicationContext.getSharedPreferences(
            "app_preferences",
            Context.MODE_PRIVATE
        )
        val serverUrl = preferences.getString("grpc_server_url", "").orEmpty()
        if (serverUrl.isBlank()) return Result.success()

        return runCatching {
            GrpcExamClient.send(
                context = applicationContext,
                serverUrl = serverUrl,
                message = "Envío periódico de WorkManager"
            )
        }.fold(
            onSuccess = { accepted -> if (accepted) Result.success() else Result.failure() },
            onFailure = { Result.retry() }
        )
    }
}
