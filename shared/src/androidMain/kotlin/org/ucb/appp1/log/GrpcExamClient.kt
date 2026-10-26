package org.ucb.appp1.log

import android.content.Context
import android.provider.Settings
import com.develoop.logs.ELogLevel
import com.develoop.logs.ELogResultCode
import com.develoop.logs.LogData
import com.develoop.logs.LogRequest
import java.util.UUID
import okio.ByteString.Companion.encodeUtf8

object GrpcExamClient {

    suspend fun send(
        context: Context,
        serverUrl: String,
        message: String,
        userId: String = ""
    ): Boolean {
        require(serverUrl.startsWith("https://") || serverUrl.startsWith("http://")) {
            "La URL debe comenzar con https:// o http://"
        }

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ).orEmpty()
        val log = LogData(
            androidId = androidId.encodeUtf8(),
            appInstanceId = UUID.randomUUID().toString().encodeUtf8(),
            logLevel = ELogLevel.LEVEL_INFO,
            message = message,
            mobileTimeStamp = System.currentTimeMillis(),
            versionCode = 1,
            userId = userId
        )
        val response = LogService(serverUrl).sendLogs(LogRequest(logs = listOf(log)))
        return response.resultCode == ELogResultCode.LOG_RESULT_OK
    }
}
