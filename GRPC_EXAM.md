# Guía gRPC para el examen

El proyecto incluye un servidor Spring Boot gRPC independiente que implementa
`LogService/send`. Escucha en el puerto `9090`, acepta una lista de registros y
responde `LOG_RESULT_OK`.

## 1. Iniciar el servidor

Desde la carpeta raíz del proyecto:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :grpcServer:bootJar
./start-grpc-server.sh
```

La consola debe mostrar:

```text
Registered gRPC service: LogService
gRPC Server started ... port: 9090
```

## 2. Probar localmente

```bash
grpcurl -plaintext \
  -d '{"logs":[{"logLevel":"LEVEL_INFO","message":"Prueba del examen","versionCode":1,"userId":"jonathan"}]}' \
  localhost:9090 LogService/send
```

Respuesta esperada:

```json
{
  "resultCode": "LOG_RESULT_OK"
}
```

En Postman: crea una petición **gRPC**, usa `localhost:9090`, importa
`grpc-server/src/main/proto/LogApi.proto`, selecciona `LogService/send`, pega el
JSON anterior y pulsa **Invoke**. Para la prueba local desactiva TLS.

## 3. Configurar ngrok una sola vez

Instala ngrok desde <https://ngrok.com/download>, obtén el token en
<https://dashboard.ngrok.com/get-started/your-authtoken> y ejecuta:

```bash
ngrok config add-authtoken TU_TOKEN
```

El token se guarda en la configuración personal de ngrok fuera de este proyecto.
No lo copies en archivos del código ni lo subas a GitHub.

## 4. Publicar el servidor

Mantén el servidor abierto y, en otra Terminal, ejecuta:

```bash
./start-ngrok.sh
```

Copia la dirección `https://...ngrok-free.app` o
`https://...ngrok-free.dev`. En Postman úsala sin `https://` como host gRPC y
con TLS activo. En la app Android ve a **Ajustes > Servidor gRPC**, pega la URL
HTTPS completa y pulsa **Probar gRPC**.

La URL gratuita puede cambiar al reiniciar ngrok. La app la guarda localmente,
por lo que solo hay que reemplazarla en Ajustes.

## Archivos principales

- Contrato: `grpc-server/src/main/proto/LogApi.proto`
- Servidor: `grpc-server/src/main/java/org/ucb/grpcserver/LogGrpcService.java`
- Cliente Android: `shared/src/androidMain/kotlin/org/ucb/appp1/log/GrpcExamClient.kt`
- Envío periódico: `shared/src/androidMain/kotlin/org/ucb/appp1/scheduler/LogUploadWorker.kt`
