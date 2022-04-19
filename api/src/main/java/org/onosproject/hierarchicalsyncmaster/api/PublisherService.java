package org.onosproject.hierarchicalsyncmaster.api;

import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;

public interface PublisherService {

    void newDeviceTopologyEvent(EventWrapper deviceEventWrapper);
    void newLinkTopologyEvent(EventWrapper deviceEventWrapper);
}
