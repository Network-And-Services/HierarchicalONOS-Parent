/*
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
package org.onosproject.hierarchicalsyncmaster.converter;

import org.onosproject.grpc.net.link.models.LinkEnumsProto;
import org.onosproject.grpc.net.link.models.LinkEventProto.LinkNotificationProto;
import org.onosproject.grpc.net.models.LinkProtoOuterClass.LinkProto;
import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.incubator.protobuf.models.net.AnnotationsTranslator;
import org.onosproject.incubator.protobuf.models.net.ConnectPointProtoTranslator;
import org.onosproject.net.Link;
import org.onosproject.net.link.DefaultLinkDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class LinkEventWrapper extends EventWrapper {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public LinkEventWrapper(OnosEvent event) {

        LinkNotificationProto linkNotificationProto;
        try {
            linkNotificationProto = LinkNotificationProto.parseFrom(event.subject());
            String myeventTypeName = linkNotificationProto.getLinkEventType().name();
            if (linkEventTypeSupported(myeventTypeName)){
                log.debug("Received Update --> Type: " + myeventTypeName
                        + " Link: " + linkNotificationProto.getLink());
                eventTypeName = myeventTypeName;
                description = getLinkDescriptionFromProto(linkNotificationProto.getLink());
            } else {
                log.error("Unsupported Onos Link Event {}. There is no matching"
                        + "proto Link Event type", myeventTypeName);
            }
        } catch (Exception e) {
            log.error("Error while converting Link Event. Exception {}", e.toString());
        }
    }

    private DefaultLinkDescription getLinkDescriptionFromProto(LinkProto linkProto) {
        DefaultLinkDescription defaultLinkDescription =
                new DefaultLinkDescription(ConnectPointProtoTranslator.translate(linkProto.getSrc()).get(),
                        ConnectPointProtoTranslator.translate(linkProto.getDst()).get(),
                        Link.Type.valueOf(linkProto.getType().name()),
                        linkProto.getIsExpected(),
                        AnnotationsTranslator.asAnnotations(linkProto.getAnnotationsMap()));
        log.debug("Correctly converted proto of Link " + defaultLinkDescription);
        return defaultLinkDescription;
    }

    private boolean linkEventTypeSupported(String linkeventType) {
        LinkEnumsProto.LinkEventTypeProto[] grpcLinkEvents = LinkEnumsProto.LinkEventTypeProto.values();
        for (LinkEnumsProto.LinkEventTypeProto linkEventType : grpcLinkEvents) {
            if (linkEventType.name().equals(linkeventType)) {
                return true;
            }
        }
        return false;
    }
}
