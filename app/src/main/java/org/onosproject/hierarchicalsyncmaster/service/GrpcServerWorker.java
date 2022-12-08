package org.onosproject.hierarchicalsyncmaster.service;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.onosproject.hierarchicalsyncmaster.api.GrpcEventStorageService;
import org.onosproject.hierarchicalsyncmaster.api.GrpcServerService;
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.hierarchicalsyncmaster.proto.Hierarchical;
import org.onosproject.hierarchicalsyncmaster.proto.HierarchicalServiceGrpc;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.Instant;

@Component(immediate = true, service = {GrpcServerService.class})
public class GrpcServerWorker implements GrpcServerService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Server server;
    private final HierarchicalServiceGrpc.HierarchicalServiceImplBase implBase = new HierarchicalSyncServer();
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcEventStorageService grpcEventStorageService;
    @Activate
    public void start() {
        try {
            server = NettyServerBuilder.forPort(5908)
                    .addService(implBase)
                    .build()
                    .start();
        } catch (IOException e) {
            log.error("Unable to start gRPC server", e);
            throw new IllegalStateException("Unable to start gRPC server", e);
        }
        log.info("Started");
    }

    @Deactivate
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        log.info("Stopped");
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    @Override
    public boolean isRunning() {
        return !server.isTerminated() && !server.isShutdown();
    }

    private class HierarchicalSyncServer extends HierarchicalServiceGrpc.HierarchicalServiceImplBase {
        @Override
        public void sayHello(Hierarchical.Request request,
                             io.grpc.stub.StreamObserver<Hierarchical.Response> responseObserver) {
            OnosEvent event = new OnosEvent(OnosEvent.Type.valueOf(request.getType()), request.getRequest().toByteArray(),
                    request.getClusterid(), request.getCaptured(), request.getSent(), Instant.now().toEpochMilli());
            grpcEventStorageService.publishEvent(event);
            log.debug("Pushed event {} to grpc storage", event);
            Hierarchical.Response reply = Hierarchical.Response.newBuilder().setResponse("ACK").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

}
