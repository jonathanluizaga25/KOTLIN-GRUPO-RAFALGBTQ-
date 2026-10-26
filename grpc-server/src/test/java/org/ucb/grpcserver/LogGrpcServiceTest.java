package org.ucb.grpcserver;

import static org.assertj.core.api.Assertions.assertThat;

import com.develoop.logs.ELogLevel;
import com.develoop.logs.ELogResultCode;
import com.develoop.logs.LogData;
import com.develoop.logs.LogRequest;
import com.develoop.logs.LogResponse;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogGrpcServiceTest {

    @Test
    void sendReturnsOk() {
        LogRequest request = LogRequest.newBuilder()
                .addLogs(LogData.newBuilder()
                        .setLogLevel(ELogLevel.LEVEL_INFO)
                        .setMessage("Prueba del examen")
                        .setUserId("jonathan")
                        .build())
                .build();
        List<LogResponse> responses = new ArrayList<>();

        new LogGrpcService().send(request, new StreamObserver<>() {
            @Override public void onNext(LogResponse value) { responses.add(value); }
            @Override public void onError(Throwable throwable) { throw new AssertionError(throwable); }
            @Override public void onCompleted() { }
        });

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getResultCode()).isEqualTo(ELogResultCode.LOG_RESULT_OK);
    }
}
