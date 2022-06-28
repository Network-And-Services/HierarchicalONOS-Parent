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

import org.onosproject.net.DeviceId;
import org.onosproject.net.region.Region;
import org.onosproject.ui.UiTopoOverlay;
import org.onosproject.ui.topo.PropertyPanel;
import org.onosproject.ui.topo.TopoConstants.CoreButtons;
import org.onosproject.ui.GlyphConstants;
import static org.onosproject.ui.topo.TopoConstants.Properties.FLOWS;
import static org.onosproject.ui.topo.TopoConstants.Properties.INTENTS;
import static org.onosproject.ui.topo.TopoConstants.Properties.LATITUDE;
import static org.onosproject.ui.topo.TopoConstants.Properties.LONGITUDE;
import static org.onosproject.ui.topo.TopoConstants.Properties.TOPOLOGY_SSCS;
import static org.onosproject.ui.topo.TopoConstants.Properties.TUNNELS;
import static org.onosproject.ui.topo.TopoConstants.Properties.VERSION;

/**
 * Our topology overlay.
 */
public class AppUiTopovOverlay extends UiTopoOverlay {

    // NOTE: this must match the ID defined in sampleTopov.js
    private static final String OVERLAY_ID = "hierarchical-overlay";
    private static final String MY_TITLE = "Hierarchical View";

    public AppUiTopovOverlay() {
        super(OVERLAY_ID);
    }


    @Override
    public void modifySummary(PropertyPanel pp) {
        pp.title(MY_TITLE)
                .glyphId(GlyphConstants.CROWN)
                .addProp("CHILD", "Child clusters", AppUiTopovMessageHandler.regionService.getRegions().stream().map(Region::name).count())
                .removeProps(
                        TOPOLOGY_SSCS,
                        INTENTS,
                        TUNNELS,
                        FLOWS,
                        VERSION
                );
        for (Region region : AppUiTopovMessageHandler.regionService.getRegions()){
            pp.addSeparator();
            pp.addProp("NAME"+region.id().toString(), "Child name", region.name());
            pp.addProp("DEVICE"+region.id().toString(), "Devices", AppUiTopovMessageHandler.regionService.getRegionDevices(region.id()).stream().count());
        }
        //.addProp("CHILD", "Child clusters", AppUiTopovMessageHandler.regionService.getRegions().stream().map(Region::name).collect(Collectors.joining("\n")))
    }

    @Override
    public void modifyDeviceDetails(PropertyPanel pp, DeviceId deviceId) {
        pp.title(AppUiTopovMessageHandler.deviceService.getDevice(deviceId).annotations().value("originalId"));
        pp.addProp("CHILDCLUSTER", "Child cluster", AppUiTopovMessageHandler.regionService.getRegionForDevice(deviceId).name());
        pp.removeProps(LATITUDE, LONGITUDE);
        pp.removeButtons(CoreButtons.SHOW_PORT_VIEW)
                .removeButtons(CoreButtons.SHOW_GROUP_VIEW)
                .removeButtons(CoreButtons.SHOW_METER_VIEW);
    }

}
