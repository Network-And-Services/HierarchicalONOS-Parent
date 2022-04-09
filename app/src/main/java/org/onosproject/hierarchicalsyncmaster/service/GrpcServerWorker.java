package org.onosproject.hierarchicalsyncmaster.service;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.onosproject.hierarchicalsyncmaster.proto.Hierarchical;
import org.onosproject.hierarchicalsyncmaster.proto.HierarchicalServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GrpcServerWorker {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Server server;

    public GrpcServerWorker(){
        createServer();
    }

    public void deactivate() {
        stop();
    }

    private void createServer(){
        try {
            server = NettyServerBuilder.forPort(5908)
                    .addService(new HierarchicalSyncServer())
                    .build()
                    .start();
        } catch (IOException e) {
            log.info("Unable to start gRPC server", e);
            throw new IllegalStateException("Unable to start gRPC server", e);
        } catch (Exception e){
            log.info("PROBLEMAAAAAAA: "+ e.toString());
        }

        log.info("Started");
        }


    private void stop() {
        if (server != null) {
            server.shutdown();
        }
        log.info("Stopped");

    }

    private class HierarchicalSyncServer extends HierarchicalServiceGrpc.HierarchicalServiceImplBase {
        @Override
        public void sayHello(Hierarchical.Request request,
                             io.grpc.stub.StreamObserver<Hierarchical.Response> responseObserver) {
            Hierarchical.Response reply = Hierarchical.Response.newBuilder().setResponse(request.getType()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

}
