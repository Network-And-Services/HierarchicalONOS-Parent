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

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import org.onosproject.event.Event;
import org.onosproject.grpc.net.link.models.LinkEnumsProto.LinkEventTypeProto;
import org.onosproject.grpc.net.link.models.LinkEnumsProto.LinkStateProto;
import org.onosproject.grpc.net.link.models.LinkEnumsProto.LinkTypeProto;
import org.onosproject.grpc.net.link.models.LinkEventProto;
import org.onosproject.grpc.net.link.models.LinkEventProto.LinkNotificationProto;
import org.onosproject.grpc.net.models.ConnectPointProtoOuterClass.ConnectPointProto;
import org.onosproject.grpc.net.models.LinkProtoOuterClass.LinkProto;
import org.onosproject.grpc.net.models.PortProtoOuterClass;
import org.onosproject.incubator.protobuf.models.net.AnnotationsTranslator;
import org.onosproject.incubator.protobuf.models.net.ConnectPointProtoTranslator;
import org.onosproject.incubator.protobuf.models.net.LinkProtoTranslator;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.Link;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Converts for ONOS Link event message to protobuf format.
 */
public class LinkEventConverter implements EventConverter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public byte[] convertToProtoMessage(Event<?, ?> event) {

        LinkEvent linkEvent = (LinkEvent) event;

        if (!linkEventTypeSupported(linkEvent)) {
            log.error("Unsupported Onos Event {}. There is no matching "
                              + "proto Event type", linkEvent.type().toString());
            return null;
        }

        return ((GeneratedMessageV3) buildDeviceProtoMessage(linkEvent)).toByteArray();
    }

    private boolean linkEventTypeSupported(LinkEvent event) {
        LinkEventTypeProto[] kafkaLinkEvents = LinkEventTypeProto.values();
        for (LinkEventTypeProto linkEventType : kafkaLinkEvents) {
            if (linkEventType.name().equals(event.type().name())) {
                return true;
            }
        }
        return false;
    }

    private LinkNotificationProto buildDeviceProtoMessage(LinkEvent linkEvent) {
        LinkNotificationProto notification = LinkNotificationProto.newBuilder()
                .setLinkEventType(getProtoType(linkEvent))
                .setLink(LinkProto.newBuilder()
                                 .setState(LinkStateProto.ACTIVE
                                                   .valueOf(linkEvent.subject().state().name()))
                                 .setType(LinkTypeProto.valueOf(linkEvent.subject().type().name()))
                                 .setDst(ConnectPointProto.newBuilder()
                                                 .setDeviceId(linkEvent.subject().dst()
                                                                      .deviceId().toString())
                                                 .setPortNumber(linkEvent.subject().dst().port()
                                                                        .toString()))
                                 .setSrc(ConnectPointProto.newBuilder()
                                                 .setDeviceId(linkEvent.subject().src()
                                                                      .deviceId().toString())
                                                 .setPortNumber(linkEvent.subject().src().port()
                                                                        .toString())))
                .build();

        return notification;
    }

    /**
     * Returns the specific Kafka Device Event Type for the corresponding ONOS
     * Device Event Type.
     *
     * @param event ONOS Device Event
     * @return Kafka Device Event Type
     */
    private LinkEventTypeProto getProtoType(LinkEvent event) {
        LinkEventTypeProto generatedEventType = null;
        LinkEventTypeProto[] kafkaEvents = LinkEventTypeProto.values();
        for (LinkEventTypeProto linkEventType : kafkaEvents) {
            if (linkEventType.name().equals(event.type().name())) {
                generatedEventType = linkEventType;
            }
        }

        return generatedEventType;
    }

    private DefaultLinkDescription getLinkDescriptionFromProto(LinkProto linkProto) {
        DefaultLinkDescription defaultLinkDescription =
                new DefaultLinkDescription(ConnectPointProtoTranslator.translate(linkProto.getSrc()).get(),
                        ConnectPointProtoTranslator.translate(linkProto.getDst()).get(),
                        Link.Type.valueOf(linkProto.getType().name()),
                        linkProto.getIsExpected(),
                        AnnotationsTranslator.asAnnotations(linkProto.getAnnotationsMap()));
        /*
        DefaultLink link = DefaultLink.builder().providerId(ProviderId.NONE).
                src(ConnectPointProtoTranslator.translate(linkProto.getSrc()).get()).
                dst(ConnectPointProtoTranslator.translate(linkProto.getDst()).get()).
                type(Link.Type.valueOf(linkProto.getType().name())).
                state(Link.State.valueOf(linkProto.getState().name())).
                annotations(AnnotationsTranslator.asAnnotations(linkProto.getAnnotationsMap())).build();

         */
        log.info("Correctly converted proto of Link "+ defaultLinkDescription);
        return defaultLinkDescription;
    }

    @Override
    public Event<?, ?> convertToEvent(byte[] event) {
        LinkEventProto.LinkNotificationProto linkNotificationProto;
        try {
            linkNotificationProto = LinkNotificationProto.parseFrom(event);
            log.info("Received Update --> Type: " + linkNotificationProto.getLinkEventType()
                    + " Link: " + linkNotificationProto.getLink());
            DefaultLinkDescription defaultLinkDescriptionProto = getLinkDescriptionFromProto(linkNotificationProto.getLink());
            //TODO: SEND THIS EVENT TO THE SERVICE THAT WILL HAVE THE DEVICE/LINK PROVIDER WITHIN
            //return new LinkEvent(LinkEvent.Type.valueOf(linkNotificationProto.getLinkEventType().toString()), link);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (Exception e){
            log.error(e.toString());
        }
        return null;
    }
}
