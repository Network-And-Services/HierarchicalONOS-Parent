package org.onosproject.hierarchicalsyncmaster.service;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.Server;
import org.onosproject.hierarchicalsyncmaster.api.GrpcEventStorageService;
import org.onosproject.hierarchicalsyncmaster.api.GrpcServerService;
import org.onosproject.hierarchicalsyncmaster.api.dto.Action;
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.hierarchicalsyncmaster.proto.ChildServiceGrpc;
import org.onosproject.hierarchicalsyncmaster.proto.Hierarchical;
import org.onosproject.hierarchicalsyncmaster.proto.HierarchicalServiceGrpc;
import org.onosproject.hierarchicalsyncmaster.proto.ChildServiceGrpc.ChildServiceBlockingStub;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

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

    @Override
    public void sendActionToChild(Action action) {
        String target;
        Hierarchical.ActionTypeProto request;
        if (action.childType.equals(Action.ChildType.RAN)){
            target = OsgiPropertyConstants.RAN_CHILD_ADDRESS_DEFAULT;
            request = Hierarchical.ActionTypeProto.CONFIGURE_RAN;
        } else {
            target = OsgiPropertyConstants.TRANSPORT_CHILD_ADDRESS_DEFAULT;
            request = Hierarchical.ActionTypeProto.CONFIGURE_PON;
        }
        try {
            ManagedChannel channel = NettyChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
            ChildServiceBlockingStub blockingStub = ChildServiceGrpc.newBlockingStub(channel);
            Hierarchical.Response response = blockingStub.sendActionRequest(Hierarchical.ActionRequest.newBuilder().
                    setType(request).setName(action.name).build());
        } catch (Exception e) {
            log.error("Unable to send gRPC message", e);
        }
        log.debug("Action event {} sent to child", request);
    }

    private class HierarchicalSyncServer extends HierarchicalServiceGrpc.HierarchicalServiceImplBase {
        @Override
        public void sendDeviceUpdate(Hierarchical.DeviceRequest request, io.grpc.stub.StreamObserver<Hierarchical.Response> responseObserver){
            OnosEvent event = new OnosEvent(OnosEvent.Type.DEVICE, request.getRequest().toByteArray(), request.getClusterid());
            grpcEventStorageService.publishEvent(event);
            log.debug("Pushed event {} to grpc storage", event);
            Hierarchical.Response reply = Hierarchical.Response.newBuilder().setResponse("ACK").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void sendLinkUpdate(Hierarchical.LinkRequest request, io.grpc.stub.StreamObserver<Hierarchical.Response> responseObserver){
            OnosEvent event = new OnosEvent(OnosEvent.Type.LINK, request.getRequest().toByteArray(), request.getClusterid());
            grpcEventStorageService.publishEvent(event);
            log.debug("Pushed event {} to grpc storage", event);
            Hierarchical.Response reply = Hierarchical.Response.newBuilder().setResponse("ACK").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

}
