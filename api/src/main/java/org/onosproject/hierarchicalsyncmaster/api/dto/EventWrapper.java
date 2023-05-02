package org.onosproject.hierarchicalsyncmaster.api.dto;

import org.onosproject.net.AbstractDescription;

public abstract class EventWrapper {
    public String eventTypeName;

    public String clusterid;
    public AbstractDescription description;

}
