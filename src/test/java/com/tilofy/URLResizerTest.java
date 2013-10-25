package com.tilofy;

import com.tilofy.image.URLResizer;
import com.tilofy.manager.ImageJobManager;
import com.tilofy.manager.Manager;
import com.tilofy.manager.Status;
import com.tilofy.manager.TimeoutManager;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import java.util.concurrent.Executors;

/**
 * It is important to note that this class requires internet connectivity
 * in order to pass.
 */
public class URLResizerTest {

    Manager manager;
    URL url;
    File resultFile;
    File resultDirectory;
    URLResizer resizer;

    @Before
    public void setup() {
        resultDirectory = new File("test");
        TimeoutManager timeoutManager = new TimeoutManager(30000);
        manager = new ImageJobManager(resultDirectory, Executors.newFixedThreadPool(1));
        timeoutManager.setManager(manager);
        manager.setTimeoutManager(timeoutManager);
        resizer = new URLResizer();
        resizer.setDimensions(10, 10);
        resizer.setManager(manager);
    }

    @After
    public void tearDown() {
        manager = null;
        url = null;
        resizer = null;
        if (resultFile != null && resultFile.exists())
            resultFile.delete();
        if (resultDirectory.exists())
            resultDirectory.delete();
    }

    @Test
    public void testBadURL() throws Exception {
        try {
            url = new URL("http://abc");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        resizer.setTargetImage(url);

        int jobID = manager.submitJob(resizer);
        waitForResizing(jobID);
        Assert.assertEquals(manager.getStatus(jobID), Status.FAILED);
        Assert.assertEquals(manager.getError(jobID), "Can't get input stream from URL!");
    }

    @Test
    public void testNotImage() throws Exception {
        try {
            url = new URL("http://www.google.com");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        resizer.setTargetImage(url);

        int jobID = manager.submitJob(resizer);
        waitForResizing(jobID);
        Assert.assertEquals(manager.getStatus(jobID), Status.FAILED);
        Assert.assertEquals(manager.getError(jobID), "URL does not represent an image");
    }

    @Test
    public void testImageResizing() throws Exception {
        try {
            url = new URL("http://upload.wikimedia.org/wikipedia/commons/2/23/Lake_mapourika_NZ.jpeg");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        resizer.setTargetImage(url);

        int jobID = manager.submitJob(resizer);

        waitForResizing(jobID);

        Assert.assertEquals(manager.getStatus(jobID), Status.COMPLETED);
        resultFile = new File("test" + File.separator + jobID + ".jpeg");
        Assert.assertTrue(resultFile.exists());
    }

    @Test
    public void testTimeout() throws Exception {
        TimeoutManager timeoutManager = new TimeoutManager(0);
        timeoutManager.setManager(manager);
        manager.setTimeoutManager(timeoutManager);
        try {
            url = new URL("http://upload.wikimedia.org/wikipedia/commons/2/23/Lake_mapourika_NZ.jpeg");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        resizer.setTargetImage(url);
        // Here we're trying to make a job that will take more than 1 second so we can see the timeout working
        resizer.setDimensions(1000000000, 1000000000);

        int jobID = manager.submitJob(resizer);

        waitForResizing(jobID);

        Assert.assertEquals(manager.getStatus(jobID), Status.TIMED_OUT);
    }

    private void waitForResizing(int jobID) {
        // Let's fail after 100 seconds
        // TODO Replace this with a better waiting mechanism
        for (int i=0; i<100 && manager.getStatus(jobID) == Status.IN_PROGRESS; ++i) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
