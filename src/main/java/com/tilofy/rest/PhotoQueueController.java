package com.tilofy.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.tilofy.image.ResizerFactory;
import com.tilofy.image.Resizer;
import com.tilofy.json.JSONStatus;
import com.tilofy.manager.Manager;
import org.codehaus.jackson.map.ObjectMapper;
import java.net.URL;
import com.tilofy.manager.Status;

/**
 * This is the one controller for the image resizing.  It responds to 2 REST commands.
 * index -- Show a list of all the jobs and their status.
 * show -- Shows the status of the supplied job_id
 * create -- Creates a new image resizing job and redirects to the show page
 *
 */
@Path("queue")
public class PhotoQueueController {
    private Manager manager;

    @Inject
    public PhotoQueueController(Manager manager) {
        this.manager = manager;
    }

    @GET
    /**
     * Index should show all the jobs and their status.
     * TODO Implement Pagination
     */
    public Response index() {
        String json = "JSON Parse Error";
        try {
            json = new ObjectMapper().writeValueAsString(manager.getAllJobs());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return getResponse(json);
    }

    @GET
    @Path("{job_id}")
    public Response show(@PathParam("job_id") int jobID) {
        JSONStatus jsonStatus = new JSONStatus();
        jsonStatus.jobID = jobID;
        Status status = manager.getStatus(jobID);
        jsonStatus.status = status.toString();
        if (status == Status.FAILED)
            jsonStatus.statusError = manager.getError(jobID);
        return getResponse(jsonStatus);
    }

    @PUT
    public Response create(@QueryParam("url") String urlString, @QueryParam("size") String size) {
        if (urlString == null || urlString.isEmpty())
            return getErrorResponse("Must supply URL");
        if  (size == null || size.isEmpty())
            return getErrorResponse("Must supply size");

        String[] split = size.split("x");
        if (split.length != 2)
            return getErrorResponse("Size must be of the format <integer>x<integer>");

        int targetWidth, targetHeight;
        try {
            targetWidth = Integer.parseInt(split[0]);
            targetHeight = Integer.parseInt(split[1]);
        }
        catch (Exception e) {
            return getErrorResponse("Size must be of the format <integer>x<integer>");
        }

        if (targetHeight < 1 || targetWidth < 1)
            return getErrorResponse("Width and height must both be positive");

        URL url;

        try {
            url = new URL(urlString);
        }
        catch (Exception e) {
            return getErrorResponse("Not a valid URL");
        }

        // TODO Figure out how to use Guice here
        Resizer resizer = ResizerFactory.getURLResizer(url, targetWidth, targetHeight, manager);
        int jobID = manager.submitJob(resizer);
        return show(jobID);
    }

    private Response getErrorResponse(String error) {
        JSONStatus status = new JSONStatus();
        status.error = error;
        return getResponse(status);
    }

    private Response getResponse(JSONStatus status) {
        String json = "JSON Parse Error";
        try {
            json = new ObjectMapper().writeValueAsString(status);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return getResponse(json);
    }

    private Response getResponse(Object input) {
        return Response.status(200).entity(input).build();
    }
}
