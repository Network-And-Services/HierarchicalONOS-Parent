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
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.hierarchicalsyncmaster.proto.Hierarchical;
import org.onosproject.hierarchicalsyncmaster.api.GrpcEventStorageService;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;

/**
 * Implementation of a Kafka Producer.
 */
//TODO: Remove immediate once you will invoke the service from other classes
@Component(service = {GrpcReceiverService.class})
public class GrpcReceiverManager implements GrpcReceiverService {

    protected ExecutorService eventExecutor;
    private GrpcServerWorker serverWorker;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcEventStorageService grpcEventStorageService;

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Activate
    protected void activate() {
        eventExecutor = newSingleThreadScheduledExecutor(groupedThreads("onos/onosEvents", "events-%d", log));
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        eventExecutor.shutdownNow();
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
}
