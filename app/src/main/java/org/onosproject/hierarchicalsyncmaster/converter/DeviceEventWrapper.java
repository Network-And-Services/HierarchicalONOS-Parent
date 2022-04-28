package org.onosproject.hierarchicalsyncmaster.converter;

import com.google.protobuf.InvalidProtocolBufferException;
import org.onlab.packet.ChassisId;
import org.onosproject.grpc.net.device.models.DeviceEnumsProto;
import org.onosproject.grpc.net.device.models.DeviceEventProto;
import org.onosproject.grpc.net.models.DeviceProtoOuterClass;
import org.onosproject.grpc.net.models.PortProtoOuterClass;
import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.incubator.protobuf.models.net.AnnotationsTranslator;
import org.onosproject.net.*;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;

public class DeviceEventWrapper extends EventWrapper {

    public DeviceId deviceId;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DeviceEventWrapper(OnosEvent event) {
        DeviceEventProto.DeviceNotificationProto deviceNotificationProto;
        try {
            deviceNotificationProto = DeviceEventProto.DeviceNotificationProto.parseFrom(event.subject());
            String myEventTypeName = deviceNotificationProto.getDeviceEventType().name();
            if (deviceEventTypeSupported(myEventTypeName)) {
                log.debug("Received Update --> Type: " + deviceNotificationProto.getDeviceEventType()
                        + " Device: " + deviceNotificationProto.getDevice().getDeviceId());
                eventTypeName = myEventTypeName;
                deviceId = DeviceId.deviceId(deviceNotificationProto.getDevice().getDeviceId());
                if (eventTypeName.startsWith("DEVICE")) {
                    description = getDeviceFromProto(deviceNotificationProto.getDevice());
                } else {
                    description = getPortFromProto(deviceNotificationProto.getPort());
                }
            } else {
                log.error("Unsupported Onos Device Event {}. There is no matching"
                        + "proto Device Event type", myEventTypeName);
            }
        } catch (Exception e) {
           log.error("Error while converting Device Event. Exception {}", e.toString());
        }
    }


    private DefaultDeviceDescription getDeviceFromProto(DeviceProtoOuterClass.DeviceProto deviceProto){
        DefaultDeviceDescription defaultDeviceDescription =
                new DefaultDeviceDescription(URI.create(deviceProto.getDeviceId()),
                        Device.Type.valueOf(deviceProto.getType().name()),
                        deviceProto.getManufacturer(), deviceProto.getHwVersion(),
                        deviceProto.getSwVersion(), deviceProto.getSerialNumber(),
                        new ChassisId(Long.parseLong(deviceProto.getChassisId())), AnnotationsTranslator.asAnnotations(deviceProto.getAnnotationsMap()));
        log.debug("Correctly converted proto of Device " + defaultDeviceDescription);
        return defaultDeviceDescription;
    }

    private DefaultPortDescription getPortFromProto(PortProtoOuterClass.PortProto portProto){
        DefaultPortDescription defaultPortDescription = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.fromString(portProto.getPortNumber()))
                .isEnabled(portProto.getIsEnabled())
                .type(Port.Type.valueOf(portProto.getType().name()))
                .portSpeed(portProto.getPortSpeed())
                .annotations(AnnotationsTranslator.asAnnotations(portProto.getAnnotationsMap())).build();
        log.debug("Correctly converted proto of Port " + defaultPortDescription);
        return defaultPortDescription;
    }

    private boolean deviceEventTypeSupported(String deviceEventType) {
        DeviceEnumsProto.DeviceEventTypeProto[] grpcDeviceEvents = DeviceEnumsProto.DeviceEventTypeProto.values();
        for (DeviceEnumsProto.DeviceEventTypeProto deviceEventTypeProto : grpcDeviceEvents) {
            if (deviceEventTypeProto.name().equals(deviceEventType)) {
                return true;
            }
        }
        return false;
    }
}
