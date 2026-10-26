package org.ucb.appp1.log

import com.develoop.logs.LogRequest
import com.develoop.logs.LogResponse
import com.develoop.logs.LogServiceClient
import com.squareup.wire.GrpcClient
import okhttp3.OkHttpClient
import okhttp3.Protocol

actual class LogService actual constructor(
    serverUrl: String
) {
    private val normalizedServerUrl = serverUrl.trim().let {
        if (it.endsWith('/')) it else "$it/"
    }

    private val httpClient = OkHttpClient.Builder()
        // HTTPS negotiates HTTP/2 through ALPN. OkHttp requires HTTP/1.1 as a
        // fallback in this mode; gRPC still uses HTTP/2 when ngrok advertises it.
        .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        .build()

    private val grpcClient = GrpcClient.Builder()
        .client(httpClient)
        .baseUrl(normalizedServerUrl)
        .build()

    private val logService =
        grpcClient.create(LogServiceClient::class)

    actual suspend fun sendLogs(
        request: LogRequest
    ): LogResponse {
        return logService.send().execute(request)
    }
}
