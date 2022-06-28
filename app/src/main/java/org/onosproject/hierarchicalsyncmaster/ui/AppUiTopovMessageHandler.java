/*
 * Copyright 2022-present Open Networking Foundation
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
package org.onosproject.hierarchicalsyncmaster.ui;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.onlab.osgi.ServiceDirectory;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Element;
import org.onosproject.net.Link;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.region.Region;
import org.onosproject.net.region.RegionService;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiConnection;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.topo.*;
import org.onosproject.ui.topo.NodeBadge.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

/**
 * Skeletal ONOS UI Topology-Overlay message handler.
 */
public class AppUiTopovMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_TOPOV_DISPLAY_START = "sampleTopovDisplayStart";
    private static final String SAMPLE_TOPOV_DISPLAY_UPDATE = "sampleTopovDisplayUpdate";
    private static final String SAMPLE_TOPOV_DISPLAY_STOP = "sampleTopovDisplayStop";
    private static final String ID = "id";
    private static final String MODE = "mode";
    private enum Mode { IDLE, MOUSE, LINK }

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static DeviceService deviceService;
    private LinkService linkService;
    public static RegionService regionService;
    private Mode currentMode = Mode.IDLE;
    private Element elementOfNote;

    // ===============-=-=-=-=-=-======================-=-=-=-=-=-=-================================


    @Override
    public void init(UiConnection connection, ServiceDirectory directory) {
        super.init(connection, directory);
        deviceService = directory.get(DeviceService.class);
        regionService = directory.get(RegionService.class);
        linkService = directory.get(LinkService.class);
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new DisplayStartHandler(),
                new DisplayUpdateHandler(),
                new DisplayStopHandler()
        );
    }
    private final class DisplayStartHandler extends RequestHandler {
        public DisplayStartHandler() {
            super(SAMPLE_TOPOV_DISPLAY_START);
        }

        @Override
        public void process(ObjectNode payload) {
            String mode = string(payload, MODE);

            log.debug("Start Display: mode [{}]", mode);
            clearState();
            clearForMode();

            switch (mode) {
                case "mouse":
                    currentMode = Mode.MOUSE;
                    populateAllRegions();
                    break;

                case "link":
                    currentMode = Mode.LINK;
                    break;

                default:
                    currentMode = Mode.IDLE;
                    break;
            }
        }
    }

    private final class DisplayUpdateHandler extends RequestHandler {
        public DisplayUpdateHandler() {
            super(SAMPLE_TOPOV_DISPLAY_UPDATE);
        }

        @Override
        public void process(ObjectNode payload) {
            String id = string(payload, ID);
            log.info("Update Display: id [{}]", id);
            if (currentMode == Mode.LINK) {
                if (!Strings.isNullOrEmpty(id)) {
                    updateForMode(id);
                } else {
                    clearForMode();
                }
            }
        }
    }

    private final class DisplayStopHandler extends RequestHandler {
        public DisplayStopHandler() {
            super(SAMPLE_TOPOV_DISPLAY_STOP);
        }

        @Override
        public void process(ObjectNode payload) {
            log.debug("Stop Display");
            clearState();
            clearForMode();
        }
    }

    // === ------------

    private void clearState() {
        currentMode = Mode.IDLE;
        elementOfNote = null;
    }

    private void updateForMode(String id) {
        try {
            DeviceId did = DeviceId.deviceId(id);
            elementOfNote = deviceService.getDevice(did);
            log.debug("device element {}", elementOfNote);

        } catch (Exception e2) {
            log.debug("Unable to process ID [{}]", id);
            elementOfNote = null;
        }
        populateSingleRegion();

    }

    private void clearForMode() {
        sendHighlights(new Highlights());
    }

    private void sendHighlights(Highlights highlights) {
        sendMessage(TopoJson.highlightsMessage(highlights));
    }

    private void fromLinks(Highlights highlights, DeviceId devId) {
        DemoLinkMap linkMap = new DemoLinkMap();
        Set<Link> links = linkService.getDeviceEgressLinks(devId);
        for (Link link : links) {
            linkMap.add(link);
        }
        for (DemoLink dlink : linkMap.biLinks()) {
            if (regionService.getRegionForDevice(dlink.one().src().deviceId()) != regionService.getRegionForDevice(dlink.one().dst().deviceId())){
                dlink.makeImportant();
            }
            highlights.add(dlink.highlight(null));
        }
    }
    private void singleRegionData(Region region, Highlights highlights){
        int regionint = Integer.parseInt(region.name().split("-")[1]);
        for (DeviceId deviceId : regionService.getRegionDevices(region.id())) {
            log.debug("Processing device " + deviceId.toString() + " of region " + region.name());
            DeviceHighlight dh = new DeviceHighlight(deviceId.toString());
            dh.setBadge(NodeBadge.number(Status.WARN, regionint, region.name()));
            highlights.add(dh);
            fromLinks(highlights, deviceId);
        }
    }

    private void populateAllRegions() {
        Set<Region> regions = regionService.getRegions();
        Highlights highlights = new Highlights();
        for (Region region : regions) {
            singleRegionData(region, highlights);
        }
        sendHighlights(highlights);
    }

    private void populateSingleRegion() {
        if (elementOfNote != null && elementOfNote instanceof Device) {
            Highlights highlights = new Highlights();
            Region region = regionService.getRegionForDevice(((Device) elementOfNote).id());
            singleRegionData(region, highlights);
            sendHighlights(highlights);
        }
    }
}
