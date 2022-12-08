package org.onosproject.hierarchicalsyncmaster.impl;

import org.onlab.util.ItemNotFoundException;
import org.onosproject.hierarchicalsyncmaster.api.PublisherService;
import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;
import org.onosproject.hierarchicalsyncmaster.converter.DeviceEventWrapper;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.*;
import org.onosproject.net.link.*;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.region.Region;
import org.onosproject.net.region.RegionAdminService;
import org.onosproject.net.region.RegionId;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(service = PublisherService.class)
public class EventPublisher implements PublisherService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ProviderId providerId = new ProviderId("vr", "org.onosproject.hierarchical-sync-master");
    private DeviceProviderService deviceProviderService;
    private LinkProviderService linkProviderService;
    private final DeviceProvider deviceProvider = new DeviceLocalProvider();
    private final LinkProvider linkProvider = new LinkLocalProvider();
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry deviceProviderRegistry;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected RegionAdminService regionAdminService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceAdminService deviceAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
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
        if(GrpcStorageManager.topicLeader){
            for(Device device: deviceService.getDevices()){
                linkProviderService.linksVanished(device.id());
                deviceAdminService.removeDevice(device.id());
            }
            for (Region region : regionAdminService.getRegions()){
                    regionAdminService.removeDevices(region.id(), regionAdminService.getRegionDevices(region.id()));
                    regionAdminService.removeRegion(region.id());
            }
        }
        deviceProviderRegistry.unregister(deviceProvider);
        linkProviderRegistry.unregister(linkProvider);
        log.info("Stopped");
    }

    public void checkRegion(String clusterid, DeviceId deviceid){
        if (regionAdminService.getRegionForDevice(deviceid)  == null) {
            Region region = null;
            try {
                region = regionAdminService.getRegion(RegionId.regionId(clusterid));
            } catch (ItemNotFoundException e) {
                region = regionAdminService.createRegion(RegionId.regionId(clusterid), clusterid, Region.Type.LOGICAL_GROUP, null);
            }
            regionAdminService.addDevices(region.id(), Collections.singleton(deviceid));
        }
    }

    @Override
    public boolean newDeviceTopologyEvent(EventWrapper deviceEventWrapper) {
        DeviceEventWrapper device = (DeviceEventWrapper) deviceEventWrapper;
        if(device.description instanceof DeviceDescription){
            DeviceDescription descriptor = (DeviceDescription) device.description;
            switch(DeviceEvent.Type.valueOf(device.eventTypeName)) {
                case DEVICE_ADDED:
                case DEVICE_UPDATED:
                case DEVICE_AVAILABILITY_CHANGED:
                    checkRegion(device.clusterid, device.deviceId);
                    deviceProviderService.deviceConnected(device.deviceId,descriptor);
                    break;
                case DEVICE_REMOVED:
                    deviceAdminService.removeDevice(device.deviceId);
                    //TODO: we should not vanish the link for the optical part
                    //linkProviderService.linksVanished(device.deviceId);
                    break;
            }
        } else{
            PortDescription descriptor = (PortDescription) device.description;
            List<PortDescription> descPorts = List.of(descriptor);
            switch(DeviceEvent.Type.valueOf(device.eventTypeName)) {
                case PORT_ADDED:
                case PORT_UPDATED:
                    deviceProviderService.portStatusChanged(device.deviceId,descriptor);
                    break;
                case PORT_REMOVED:
                    deviceProviderService.deletePort(device.deviceId,descPorts.get(0));
                    break;
            }
        }
        printE2E(deviceEventWrapper);
        return true;
    }

    @Override
    public boolean newLinkTopologyEvent(EventWrapper linkEventWrapper) {
        LinkDescription descriptor = (LinkDescription) linkEventWrapper.description;
        switch(LinkEvent.Type.valueOf(linkEventWrapper.eventTypeName)) {
            case LINK_ADDED:
            case LINK_UPDATED:
                linkProviderService.linkDetected(descriptor);
                break;
            case LINK_REMOVED:
                linkProviderService.linkVanished(descriptor);
                break;
        }
        printE2E(linkEventWrapper);
        return true;
    }

    private class DeviceLocalProvider implements DeviceProvider {
        @Override
        public void triggerProbe(DeviceId deviceId) {
        }

        @Override
        public void roleChanged(DeviceId deviceId, MastershipRole newRole) {
            deviceProviderService.receivedRoleReply(deviceId, newRole, newRole);
        }

        @Override
        public boolean isReachable(DeviceId deviceId) {
            return GrpcStorageManager.topicLeader;
        }

        @Override
        public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {
        }

        @Override
        public void triggerDisconnect(DeviceId deviceId) {
        }

        @Override
        public CompletableFuture<Boolean> probeReachability(DeviceId deviceId) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public int gracePeriod() {
            return DeviceProvider.super.gracePeriod();
        }

        @Override
        public ProviderId id() {
            return providerId;
        }
    }

    private class LinkLocalProvider implements LinkProvider {
        @Override
        public ProviderId id() {
            return ProviderId.NONE;
        }
    }

    public void printE2E(EventWrapper eventWrapper){
        long now = Instant.now().toEpochMilli();
        log.error("CAPTURED: "+eventWrapper.generated + "; SENT: " +eventWrapper.sent+ "; RECEIVED: "+eventWrapper.received+ "; PUBLISHED: "+now);
    }
}
