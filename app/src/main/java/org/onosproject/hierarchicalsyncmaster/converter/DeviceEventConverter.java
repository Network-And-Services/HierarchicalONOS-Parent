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
import org.onlab.packet.ChassisId;
import org.onosproject.event.Event;
import org.onosproject.grpc.net.device.models.DeviceEnumsProto.DeviceEventTypeProto;
import org.onosproject.grpc.net.device.models.DeviceEnumsProto.DeviceTypeProto;
import org.onosproject.grpc.net.device.models.DeviceEventProto;
import org.onosproject.grpc.net.device.models.DeviceEventProto.DeviceNotificationProto;
import org.onosproject.grpc.net.device.models.PortEnumsProto;
import org.onosproject.grpc.net.models.DeviceProtoOuterClass.DeviceProto;
import org.onosproject.grpc.net.models.PortProtoOuterClass.PortProto;
import org.onosproject.incubator.protobuf.models.net.AnnotationsTranslator;
import org.onosproject.incubator.protobuf.models.net.device.DeviceProtoTranslator;
import org.onosproject.incubator.protobuf.models.net.device.PortProtoTranslator;
import org.onosproject.net.*;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.google.common.base.Strings.nullToEmpty;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Converts ONOS Device event message to protobuf format.
 */
public class DeviceEventConverter implements EventConverter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public byte[] convertToProtoMessage(Event<?, ?> event) {

        DeviceEvent deviceEvent = (DeviceEvent) event;

        if (!deviceEventTypeSupported(deviceEvent)) {
            log.error("Unsupported Onos Device Event {}. There is no matching"
                              + "proto Device Event type", deviceEvent.type().toString());
            return null;
        }

        return ((GeneratedMessageV3) buildDeviceProtoMessage(deviceEvent)).toByteArray();
    }

    /**
     * Checks if the ONOS Device Event type is supported.
     *
     * @param event ONOS Device event
     * @return true if there is a match and false otherwise
     */
    private boolean deviceEventTypeSupported(DeviceEvent event) {
        DeviceEventTypeProto[] deviceEvents = DeviceEventTypeProto.values();
        for (DeviceEventTypeProto deviceEventType : deviceEvents) {
            if (deviceEventType.name().equals(event.type().name())) {
                return true;
            }
        }

        return false;
    }

    private DeviceNotificationProto buildDeviceProtoMessage(DeviceEvent deviceEvent) {
        DeviceNotificationProto.Builder notificationBuilder =
                DeviceNotificationProto.newBuilder();
        DeviceProto deviceCore =
                DeviceProto.newBuilder()
                        .setChassisId(deviceEvent.subject().chassisId().id()
                                              .toString())
                        .setDeviceId(deviceEvent.subject().id().toString())
                        .setHwVersion(deviceEvent.subject().hwVersion())
                        .setManufacturer(deviceEvent.subject().manufacturer())
                        .setSerialNumber(deviceEvent.subject().serialNumber())
                        .setSwVersion(deviceEvent.subject().swVersion())
                        .setType(DeviceTypeProto
                                         .valueOf(deviceEvent.subject().type().name()))
                        .putAllAnnotations(AnnotationsTranslator.asMap(deviceEvent.subject().annotations()))
                        .build();

        PortProto portProto = null;
        if (deviceEvent.port() != null) {
            portProto =
                    PortProto.newBuilder()
                            .setIsEnabled(deviceEvent.port().isEnabled())
                            .setPortNumber(deviceEvent.port().number()
                                                   .toString())
                            .setPortSpeed(deviceEvent.port().portSpeed())
                            .setType(PortEnumsProto.PortTypeProto
                                             .valueOf(deviceEvent.port().type().name()))
                            .build();

            notificationBuilder.setPort(portProto);
        }

        notificationBuilder.setDeviceEventType(getProtoType(deviceEvent))
                .setDevice(deviceCore);

        return notificationBuilder.build();
    }

    /**
     * Retrieves the protobuf generated device event type.
     *
     * @param event ONOS Device Event
     * @return generated Device Event Type
     */
    private DeviceEventTypeProto getProtoType(DeviceEvent event) {
        DeviceEventTypeProto protobufEventType = null;
        DeviceEventTypeProto[] deviceEvents = DeviceEventTypeProto.values();
        for (DeviceEventTypeProto deviceEventType : deviceEvents) {
            if (deviceEventType.name().equals(event.type().name())) {
                protobufEventType = deviceEventType;
            }
        }
        return protobufEventType;
    }

    private DefaultDeviceDescription getDeviceFromProto(DeviceProto deviceProto){
        DefaultDeviceDescription defaultDeviceDescription =
                new DefaultDeviceDescription(URI.create(deviceProto.getDeviceId()),
                        Device.Type.valueOf(deviceProto.getType().name()),
                        deviceProto.getManufacturer(), deviceProto.getHwVersion(),
                        deviceProto.getSwVersion(), deviceProto.getSerialNumber(),
                        new ChassisId(deviceProto.getChassisId()), AnnotationsTranslator.asAnnotations(deviceProto.getAnnotationsMap()));
        /*
        DefaultDevice device =
                new DefaultDevice(new ProviderId("Test", "test"), DeviceId.deviceId(deviceProto.getDeviceId()),
                        DeviceProtoTranslator.translate(deviceProto.getType()),
                        deviceProto.getManufacturer(), deviceProto.getHwVersion(),
                        deviceProto.getSwVersion(), deviceProto.getSerialNumber(),
                        new ChassisId(deviceProto.getChassisId()), AnnotationsTranslator.asAnnotations(deviceProto.getAnnotationsMap()));

         */
        log.info("Correctly converted proto of Device " + defaultDeviceDescription);
        return defaultDeviceDescription;
    }

    private DefaultPortDescription getPortFromProto(PortProto portProto){
        if (!nullToEmpty(portProto.getPortNumber()).isEmpty()){
            DefaultPortDescription defaultPortDescription = DefaultPortDescription.builder()
                    .withPortNumber(PortNumber.fromString(portProto.getPortNumber()))
                    .isEnabled(portProto.getIsEnabled())
                    .type(Port.Type.valueOf(portProto.getType().name()))
                    .portSpeed(portProto.getPortSpeed())
                    .annotations(AnnotationsTranslator.asAnnotations(portProto.getAnnotationsMap())).build();
            /*
            DefaultPort port = new DefaultPort(device,
                    PortNumber.fromString(portProto.getPortNumber()),
                    portProto.getIsEnabled(),
                    Port.Type.valueOf(portProto.getType().name()),
                    portProto.getPortSpeed(), AnnotationsTranslator.asAnnotations(portProto.getAnnotationsMap()));

             */
            log.info("Correctly converted proto of Port " + defaultPortDescription);
            return defaultPortDescription;
        }
        return null;
    }

    @Override
    public Event<?, ?> convertToEvent(byte[] event) {
        DeviceEventProto.DeviceNotificationProto deviceNotificationProto;
        try {
            deviceNotificationProto = DeviceEventProto.DeviceNotificationProto.parseFrom(event);
            log.info("Received Update --> Type: " + deviceNotificationProto.getDeviceEventType()
                    + " Device: " + deviceNotificationProto.getDevice().getDeviceId());
            DefaultDeviceDescription deviceDescription = getDeviceFromProto(deviceNotificationProto.getDevice());
            DefaultPortDescription portDescription = getPortFromProto(deviceNotificationProto.getPort());
            //TODO: SEND THIS EVENT TO THE SERVICE THAT WILL HAVE THE DEVICE/LINK PROVIDER WITHIN
            //return new DeviceEvent(DeviceEvent.Type.valueOf(deviceNotificationProto.getDeviceEventType().toString()), device, port);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
}
