/**
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

package org.onosproject.hierarchicalsyncmaster.impl;

import org.onosproject.hierarchicalsyncmaster.api.EventConversionService;
import org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent;
import org.onosproject.hierarchicalsyncmaster.converter.DeviceEventWrapper;
import org.onosproject.hierarchicalsyncmaster.api.dto.EventWrapper;
import org.onosproject.hierarchicalsyncmaster.converter.LinkEventWrapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.hierarchicalsyncmaster.api.dto.OnosEvent.Type.*;

/**
 * Implementation of Event Conversion Service.
 *
 */
@Component(immediate = true, service = EventConversionService.class)
public class EventConversionManager implements EventConversionService {

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
    public EventWrapper convertEvent(OnosEvent onosEvent) {
        if (onosEvent.type().equals(DEVICE)) {
            return new DeviceEventWrapper(onosEvent);
        } else if (onosEvent.type().equals(LINK)) {
            return new LinkEventWrapper(onosEvent);
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
    }
}
