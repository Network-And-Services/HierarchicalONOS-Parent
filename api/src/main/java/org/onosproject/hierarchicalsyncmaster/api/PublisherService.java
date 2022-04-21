package org.onosproject.hierarchicalsyncmaster.api;

import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;

public interface PublisherService {

    boolean newDeviceTopologyEvent(EventWrapper deviceEventWrapper);
    boolean newLinkTopologyEvent(EventWrapper deviceEventWrapper);
}
