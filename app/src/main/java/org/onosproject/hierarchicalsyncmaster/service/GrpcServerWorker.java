package org.onosproject.hierarchicalsyncmaster.service;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.onosproject.hierarchicalsyncmaster.api.GrpcServerService;
import org.onosproject.hierarchicalsyncmaster.proto.HierarchicalServiceGrpc;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

@Component(service = {GrpcServerService.class})
public class GrpcServerWorker implements GrpcServerService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Server server;
    private HierarchicalServiceGrpc.HierarchicalServiceImplBase implBase;

    @Override
    public void start(HierarchicalServiceGrpc.HierarchicalServiceImplBase implBase) {
        this.implBase = implBase;
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

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        log.info("Stopped");
    }

    @Override
    public void restart() {
        stop();
        start(implBase);
    }

    @Override
    public boolean isRunning() {
        return !server.isTerminated() && !server.isShutdown();
    }

}
