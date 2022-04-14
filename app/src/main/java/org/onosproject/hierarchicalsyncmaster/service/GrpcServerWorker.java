package org.onosproject.hierarchicalsyncmaster.service;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.onosproject.event.Event;
import org.onosproject.hierarchicalsyncmaster.proto.Hierarchical;
import org.onosproject.hierarchicalsyncmaster.proto.HierarchicalServiceGrpc;
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.hierarchicalsyncmaster.api.EventConversionService;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.link.LinkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

//TODO: you could use immediate here
public class GrpcServerWorker {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Server server;
    private final EventConversionService eventConversionService;

    public GrpcServerWorker(EventConversionService service){
        this.eventConversionService = service;
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
            log.info("Received "+ request.getType());
            String eventType = request.getType();
            //Questo evento va mandato dentro la coda
            OnosEvent event = new OnosEvent(OnosEvent.Type.valueOf(eventType), request.getRequest().toByteArray());

            //E questo è quello che succede quando lo prendi da dentro la coda
            Event<?, ?> myevent = eventConversionService.inverseEvent(event);

            if (myevent instanceof DeviceEvent){
                log.info("Instance of device event");
            } else if (myevent instanceof LinkEvent){
                log.info("Instance of link event");
            }
            Hierarchical.Response reply = Hierarchical.Response.newBuilder().setResponse(request.getType()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

}
