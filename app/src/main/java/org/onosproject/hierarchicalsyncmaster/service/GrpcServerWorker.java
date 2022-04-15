package org.onosproject.hierarchicalsyncmaster.service;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.onosproject.hierarchicalsyncmaster.api.GrpcReceiverService;
import org.onosproject.hierarchicalsyncmaster.api.GrpcServerService;
import org.onosproject.hierarchicalsyncmaster.proto.Hierarchical;
import org.onosproject.hierarchicalsyncmaster.proto.HierarchicalServiceGrpc;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component(immediate = true, service = {GrpcServerService.class})
public class GrpcServerWorker implements GrpcServerService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Server server;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcReceiverService grpcReceiverService;

    @Activate
    public void activate(){
        start();
    }

    @Deactivate
    public void deactivate(){
        stop();
    }

    private void createServer(){
        try {
            server = NettyServerBuilder.forPort(5908)
                    .addService(new HierarchicalSyncServer())
                    .build()
                    .start();
        } catch (IOException e) {
            log.error("Unable to start gRPC server", e);
            throw new IllegalStateException("Unable to start gRPC server", e);
        } catch (Exception e){
            log.error("PROBLEMAAAAAAA: "+ e);
        }

        log.info("Started");
        }


    @Override
    public void start() {
        createServer();
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
        start();
    }

    private class HierarchicalSyncServer extends HierarchicalServiceGrpc.HierarchicalServiceImplBase {

        private final Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void sayHello(Hierarchical.Request request,
                             io.grpc.stub.StreamObserver<Hierarchical.Response> responseObserver) {
            log.debug("Received event {} from grpc server", request.getType());
            grpcReceiverService.receive(request);
            Hierarchical.Response reply = Hierarchical.Response.newBuilder().setResponse("ACK").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

}
