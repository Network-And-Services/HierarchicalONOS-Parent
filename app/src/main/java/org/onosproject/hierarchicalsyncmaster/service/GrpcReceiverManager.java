/**
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.hierarchicalsyncmaster.service;

import org.onosproject.hierarchicalsyncmaster.api.GrpcReceiverService;
import org.onosproject.hierarchicalsyncmaster.api.GrpcServerService;
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.hierarchicalsyncmaster.proto.Hierarchical;
import org.onosproject.hierarchicalsyncmaster.api.GrpcEventStorageService;
import org.onosproject.hierarchicalsyncmaster.proto.HierarchicalServiceGrpc;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;
@Component(immediate = true, service = {GrpcReceiverService.class})
public class GrpcReceiverManager implements GrpcReceiverService {
    protected ExecutorService eventExecutor;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcEventStorageService grpcEventStorageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcServerService grpcServerService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        eventExecutor = newSingleThreadScheduledExecutor(groupedThreads("onos/onosReceivedEvents", "events-%d", log));
        grpcServerService.start(new HierarchicalSyncServer());
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        eventExecutor.shutdownNow();
        grpcServerService.stop();
        log.info("Stopped");
    }

    @Override
    public void receive(Hierarchical.Request record) {
        eventExecutor.execute(() -> {
            OnosEvent event = new OnosEvent(OnosEvent.Type.valueOf(record.getType()), record.getRequest().toByteArray());
            grpcEventStorageService.publishEvent(event);
            log.debug("Pushed event {} to grpc storage", event);
        });
    }

    private class HierarchicalSyncServer extends HierarchicalServiceGrpc.HierarchicalServiceImplBase {

        private final Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void sayHello(Hierarchical.Request request,
                             io.grpc.stub.StreamObserver<Hierarchical.Response> responseObserver) {
            log.debug("Received event {} from grpc server", request.getType());
            receive(request);
            Hierarchical.Response reply = Hierarchical.Response.newBuilder().setResponse("ACK").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
