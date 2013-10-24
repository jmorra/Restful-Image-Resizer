package com.tilofy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Did you remember documentation? Test cases?
 *
 */
@Path("test")
public class PhotoQueueController {
    @GET
    public Response showMessage() {

        String result = "Hello World";

        return Response.status(200).entity(result).build();
    }
}
