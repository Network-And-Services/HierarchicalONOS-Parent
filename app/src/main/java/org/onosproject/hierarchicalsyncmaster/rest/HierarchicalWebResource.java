/*
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
package org.onosproject.hierarchicalsyncmaster.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.hierarchicalsyncmaster.api.GrpcServerService;
import org.onosproject.hierarchicalsyncmaster.api.dto.Action;
import org.onosproject.rest.AbstractWebResource;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * The Web Resource for REST API calls to the Hierarchical application.
 */
@Path("action")
public class HierarchicalWebResource extends AbstractWebResource {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcServerService grpcServerService;


    /**
     * Run action on children.
     *
     * @return 200 OK
     */
    @GET
    @Path("run-action")
    public Response runAction() {
        //String result = get(CastorStore.class).getAddressMap().toString();
        get(GrpcServerService.class).sendActionToChild(new Action(Action.ChildType.RAN, "Test"));
        ObjectNode node = mapper().createObjectNode().put("response", "YO");
        return ok(node).build();
    }

    /**
     * Add a Peer.
     * Use this to add a Customer or a BGP Peer
     *
     * @param incomingData json Data
     * @return 200 OK
     * @onos.rsModel PeerModel

    @POST
    @Path("add-peer")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPeer(String incomingData) {

        String arpResult = ", Mac was known";
        ObjectMapper mapper = new ObjectMapper();
        //Peer peer = mapper.readValue(incomingData, Peer.class);
        String result = "Success: Peer Entered" + arpResult;
        ObjectNode node = mapper().createObjectNode().put("response", result);
        return ok(node).build();
    }
     */
}