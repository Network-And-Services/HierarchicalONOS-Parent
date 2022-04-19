package org.onosproject.hierarchicalsyncmaster.impl;

import org.onosproject.hierarchicalsyncmaster.api.PublisherService;
import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = PublisherService.class)
public class EventPublisher implements PublisherService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        log.info("Started!");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    @Override
    public void newDeviceTopologyEvent(EventWrapper deviceEventWrapper) {

    }

    @Override
    public void newLinkTopologyEvent(EventWrapper deviceEventWrapper) {

    }
}
