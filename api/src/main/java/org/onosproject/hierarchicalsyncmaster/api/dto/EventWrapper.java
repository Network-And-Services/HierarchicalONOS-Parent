package org.onosproject.hierarchicalsyncmaster.api.dto;

import org.onosproject.net.AbstractDescription;

public abstract class EventWrapper {
    public String eventTypeName;

    public String clusterid;
    public long generated;
    public long sent;
    public long received;
    public AbstractDescription description;

}
