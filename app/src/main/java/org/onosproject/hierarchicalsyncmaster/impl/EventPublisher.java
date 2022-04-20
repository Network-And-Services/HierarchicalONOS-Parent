package org.onosproject.hierarchicalsyncmaster.impl;

import org.onosproject.hierarchicalsyncmaster.api.PublisherService;
import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;
import org.onosproject.hierarchicalsyncmaster.converter.DeviceEventWrapper;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.*;
import org.onosproject.net.link.*;
import org.onosproject.net.provider.ProviderId;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(immediate = true, service = PublisherService.class)
public class EventPublisher implements PublisherService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected DeviceProviderService deviceProviderService;
    protected LinkProviderService linkProviderService;
    private DeviceProvider deviceProvider = new DeviceLocalProvider();
    private LinkProvider linkProvider = new LinkLocalProvider();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry deviceProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkProviderRegistry linkProviderRegistry;

    @Activate
    protected void activate() {
        deviceProviderService = deviceProviderRegistry.register(deviceProvider);
        linkProviderService = linkProviderRegistry.register(linkProvider);
        log.info("Started!");
    }

    @Deactivate
    protected void deactivate() {
        deviceProviderRegistry.unregister(deviceProvider);
        linkProviderRegistry.unregister(linkProvider);
        log.info("Stopped");
    }

    @Override
    public void newDeviceTopologyEvent(EventWrapper deviceEventWrapper) {
        DeviceEventWrapper device = (DeviceEventWrapper) deviceEventWrapper;
        if(device.description instanceof DeviceDescription){
            DeviceDescription descriptor = (DeviceDescription) device.description;
            switch(DeviceEvent.Type.valueOf(device.eventTypeName)) {
                case DEVICE_ADDED:
                    deviceProviderService.deviceConnected(device.deviceId,descriptor);
                    break;
                case DEVICE_REMOVED:
                    deviceProviderService.deviceDisconnected(device.deviceId);
                    break;
                case DEVICE_UPDATED:
                    deviceProviderService.deviceConnected(device.deviceId,descriptor);
                    break;
                case DEVICE_AVAILABILITY_CHANGED:
                    deviceProviderService.deviceConnected(device.deviceId,descriptor);
                    break;
            }

        } else{
            PortDescription descriptor = (PortDescription) device.description;
            List<PortDescription> descPorts = Arrays.asList(descriptor);
            switch(DeviceEvent.Type.valueOf(device.eventTypeName)) {
                case PORT_ADDED:
                    deviceProviderService.updatePorts(device.deviceId,descPorts);
                    break;
                case PORT_REMOVED:
                    deviceProviderService.deletePort(device.deviceId,descPorts.get(1));
                    break;
                case PORT_UPDATED:
                    deviceProviderService.updatePorts(device.deviceId,descPorts);
                    break;
            }
        }

    }

    @Override
    public void newLinkTopologyEvent(EventWrapper deviceEventWrapper) {
        LinkDescription descriptor = (LinkDescription) deviceEventWrapper.description;
        switch(LinkEvent.Type.valueOf(deviceEventWrapper.eventTypeName)) {
            case LINK_ADDED:
                linkProviderService.linkDetected(descriptor);
                break;
            case LINK_REMOVED:
                linkProviderService.linkVanished(descriptor);
                break;
            case LINK_UPDATED:
                linkProviderService.linkDetected(descriptor);
                break;
        }
    }

    private class DeviceLocalProvider implements DeviceProvider{
        @Override
        public void triggerProbe(DeviceId deviceId) {
        }

        @Override
        public void roleChanged(DeviceId deviceId, MastershipRole newRole) {
        }

        @Override
        public boolean isReachable(DeviceId deviceId) {
            return true;
        }

        @Override
        public boolean isAvailable(DeviceId deviceId) {
            return isReachable(deviceId);
        }

        @Override
        public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {
        }

        @Override
        public ProviderId id() {
            return ProviderId.NONE;
        }
    }

    private class LinkLocalProvider implements LinkProvider {
        @Override
        public ProviderId id() {
            return ProviderId.NONE;
        }
    }
}
