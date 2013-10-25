package com.tilofy;

import com.tilofy.image.URLResizer;
import com.tilofy.json.JSONStatus;
import com.tilofy.manager.ImageJobManager;
import com.tilofy.manager.Manager;
import com.tilofy.manager.Status;
import com.tilofy.manager.TimeoutManager;
import com.tilofy.rest.PhotoQueueController;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * The framework for this class was taken from
 * http://docs.jboss.org/resteasy/docs/2.3.5.Final/userguide/html/RESTEasy_Server-side_Mock_Framework.html
 * and showed me how to use a singleton resource from
 * http://stackoverflow.com/questions/6761144/resteasy-server-side-mock-framework
 */
public class PhotoQueueControllerTest {

    Dispatcher dispatcher;
    File resultDirectory;
    Manager manager;
    URLResizer resizer;
    URL url;
    JSONStatus status;
    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        resultDirectory = new File("test");
        TimeoutManager timeoutManager = new TimeoutManager(30000);
        manager = new ImageJobManager(resultDirectory, Executors.newFixedThreadPool(1));
        timeoutManager.setManager(manager);
        manager.setTimeoutManager(timeoutManager);

        resizer = new URLResizer();
        resizer.setDimensions(10, 10);
        resizer.setManager(manager);

        dispatcher = MockDispatcherFactory.createDispatcher();
        PhotoQueueController service = new PhotoQueueController();
        service.setManager(manager);
        dispatcher.getRegistry().addSingletonResource(service);
        status = new JSONStatus();
    }

    @After
    public void tearDown() throws Exception {
        dispatcher = null;
        if (resultDirectory.exists())
            resultDirectory.delete();
        manager = null;
        resizer = null;
        url = null;
        status = null;
    }

    @Test
    public void blankIndexTest() throws Exception {
        MockHttpRequest request = MockHttpRequest.get("/queue");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        JSONAssert.assertEquals(response.getContentAsString(), "{}", false);
    }

    @Test
    public void indexWithDataTest() throws Exception {
        int jobID = manager.submitJob(resizer);
        MockHttpRequest request = MockHttpRequest.get("/queue");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        JSONAssert.assertEquals(response.getContentAsString(), "{" + jobID + ":IN_PROGRESS}", false);
    }

    @Test
    public void showStatus() throws Exception {
        try {
            url = new URL("http://upload.wikimedia.org/wikipedia/commons/2/23/Lake_mapourika_NZ.jpeg");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        resizer.setTargetImage(url);
        int jobID = manager.submitJob(resizer);

        URLResizerTest.waitForResizing(jobID, manager);

        MockHttpRequest request = MockHttpRequest.get("/queue/0");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        status.jobID = jobID;
        status.status = Status.COMPLETED.toString();

        JSONAssert.assertEquals(mapper.writeValueAsString(status), response.getContentAsString(), false);
    }

    @Test
    public void showImage() throws Exception {
        try {
            url = new URL("http://upload.wikimedia.org/wikipedia/commons/2/23/Lake_mapourika_NZ.jpeg");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        resizer.setTargetImage(url);
        int jobID = manager.submitJob(resizer);

        URLResizerTest.waitForResizing(jobID, manager);

        MockHttpRequest request = MockHttpRequest.get("/queue/0.jpg");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void createNoURL() throws Exception {
        testCreateError("/queue", "Must supply URL");
    }

    @Test
    public void createNoSize() throws Exception {
        testCreateError("/queue/?url=foo", "Must supply size");
    }

    @Test
    public void createInvalidSize() throws Exception {
        testCreateError("/queue/?url=foo&size=bar", "Size must be of the format [integer]x[integer]");
    }

    @Test
    public void createNonIntegerSize() throws Exception {
        testCreateError("/queue/?url=foo&size=axb", "Size must be of the format [integer]x[integer]");
    }

    @Test
    public void createNegativeSize() throws Exception {
        testCreateError("/queue/?url=foo&size=0x0", "Width and height must both be positive");
    }

    @Test
    public void createInvalidURL() throws Exception {
        testCreateError("/queue/?url=foo&size=1x1", "Not a valid URL");
    }

    @Test
    public void createValid() throws Exception {
        MockHttpRequest request = MockHttpRequest.post("/queue/?url=http://upload.wikimedia.org/wikipedia/commons/2/23/Lake_mapourika_NZ.jpeg&size=1x1");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        status.status = Status.IN_PROGRESS.toString();
        status.jobID = 0;

        JSONAssert.assertEquals(mapper.writeValueAsString(status), response.getContentAsString(), false);
    }

    private void testCreateError(String URL, String error) throws Exception {
        MockHttpRequest request = MockHttpRequest.post(URL);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        status.error = error;
        JSONAssert.assertEquals(mapper.writeValueAsString(status), response.getContentAsString(), false);
    }
}
