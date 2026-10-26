package org.ucb.appp1.log.data.repository

import io.ktor.client.HttpClient

class LogRepository(baseUrl: String) {
    private val httpClient = HttpClient()
//    private val transport = KtorHttpClient(httpClient, baseUrl)
//    private val protocolClient = ProtocolClient(
//        transport = transport,
//        config = ProtocolClientConfig()
//    )
//
//    // Cliente generado automáticamente para tu service LogService
//    private val logServiceClient = LogServiceClient(protocolClient)
//
//    suspend fun enviarLogs(
//        logsList: List<LogData>,
//        onSuccess: () -> Unit,
//        onError: (String) -> Unit
//    ) {
//        try {
//            // 1. Construir el request usando las clases generadas
//            val request = LogRequest.newBuilder().apply {
//                addAllLogs(logsList)
//            }.build() // Nota: Dependiendo de si usas la API builder o propiedades directas en Kotlin, puede variar ligeramente.
//
//            // 2. Invocar el método remoto como una función suspend
//            val response = logServiceClient.send(request)
//
//            // 3. Procesar la respuesta
//            val result = response.message.resultCode
//            if (result.name == "LOG_RESULT_OK") {
//                onSuccess()
//            } else {
//                onError("Error en servidor: $result")
//            }
//
//        } catch (e: Exception) {
//            // Manejo de errores de red o excepciones gRPC
//            onError(e.localizedMessage ?: "Error desconocido al enviar logs")
//        }
//    }
}