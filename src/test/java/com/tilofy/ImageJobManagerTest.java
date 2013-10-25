package com.tilofy;

import com.tilofy.image.Resizer;
import com.tilofy.image.ResizerFactory;
import com.tilofy.manager.ImageJobManager;
import com.tilofy.manager.Manager;
import com.tilofy.manager.Status;
import com.tilofy.manager.TimeoutManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.File;

import java.util.HashMap;

public class ImageJobManagerTest {

    Manager manager;
    Resizer resizer;
    @Before
    public void setup() {
        TimeoutManager timeoutManager = new TimeoutManager(30000);
        manager = new ImageJobManager(new File("test"), null);
        timeoutManager.setManager(manager);
        manager.setTimeoutManager(timeoutManager);
        resizer = ResizerFactory.getURLResizer(null, 0, 0, manager);
    }

    @After
    public void tearDown() {
        manager = null;
        resizer = null;
    }

    @Test
    public void testImageJobManagerGetStatus() throws Exception {
        int jobID = manager.submitJob(resizer);
        Assert.assertEquals(manager.getStatus(jobID), Status.IN_PROGRESS);
    }

    @Test
    public void testNoSuchJob() throws Exception {
        Assert.assertEquals(manager.getStatus(-1), Status.NO_SUCH_JOB);
    }

    @Test
    public void testAllJobs() throws Exception {
        int jobID1 = manager.submitJob(resizer);
        int jobID2 = manager.submitJob(resizer);
        HashMap<Integer, Status> jobs = new HashMap<Integer, Status>();
        jobs.put(jobID1, Status.IN_PROGRESS);
        jobs.put(jobID2, Status.IN_PROGRESS);
        Assert.assertEquals(manager.getAllJobs(), jobs);
    }

    @Test
    public void testUpdateStatus() throws Exception {
        int jobID = manager.submitJob(resizer);
        manager.updateStatus(jobID, Status.COMPLETED);
        Assert.assertEquals(manager.getStatus(jobID), Status.COMPLETED);
    }

    @Test
    public void testErrorString() throws Exception {
        int jobID = manager.submitJob(resizer);
        Assert.assertEquals(manager.getError(-1), "");
        Assert.assertEquals(manager.getError(jobID), "");
        manager.setError(jobID, "Test Error");
        Assert.assertEquals(manager.getError(jobID), "Test Error");
        Assert.assertEquals(manager.getStatus(jobID), Status.FAILED);
    }
}
