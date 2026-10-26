package org.ucb.grpcserver;

import com.develoop.logs.ELogResultCode;
import com.develoop.logs.LogData;
import com.develoop.logs.LogRequest;
import com.develoop.logs.LogResponse;
import com.develoop.logs.LogServiceGrpc;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class LogGrpcService extends LogServiceGrpc.LogServiceImplBase {

    private static final Logger LOGGER = Logger.getLogger(LogGrpcService.class.getName());

    @Override
    public void send(LogRequest request, StreamObserver<LogResponse> responseObserver) {
        LOGGER.info(() -> "Petición gRPC recibida: " + request.getLogsCount() + " registro(s)");

        for (LogData log : request.getLogsList()) {
            LOGGER.info(() -> String.format(
                    "[%s] nivel=%s usuario=%s mensaje=%s fechaMovil=%s",
                    Instant.now(),
                    log.getLogLevel(),
                    log.getUserId().isBlank() ? "invitado" : log.getUserId(),
                    log.getMessage(),
                    log.getMobileTimeStamp()));
        }

        LogResponse response = LogResponse.newBuilder()
                .setResultCode(ELogResultCode.LOG_RESULT_OK)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
