package com.tilofy.manager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tilofy.image.Resizer;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.Executor;


/**
 *  An in memory implementation of a Manager.  This should be treated as a Singleton class.  It uses Guice
 *  to get it's dependencies.  It will keep track of status and errors with ConcurrentHashMaps because
 *  multiple threads could access these objects at the same time.
 */
public class ImageJobManager implements Manager {
    private File outputDirectory;
    private int currentJobID = 0;
    private ConcurrentHashMap<Integer, Status> jobMap = new ConcurrentHashMap<Integer, Status>();
    private ConcurrentHashMap<Integer, String> jobFailures = new ConcurrentHashMap<Integer, String>();
    private Executor executor;

    @Inject
    public ImageJobManager(@Named("Image Directory") File outputDirectory, @Named("Executor") Executor executor) {
        this.outputDirectory = outputDirectory;
        this.executor = executor;
        if (!outputDirectory.exists() || !outputDirectory.isDirectory())
            outputDirectory.mkdirs();
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Submits the resizer job and returns immediately.  This also sets the job status as in progress
     * and increments the current job counter.  This method is synchronized so that two threads don't
     * increment the currentJobID at the same time.
     * @param resizer The resizer job to execute
     * @return The jobID of the new job
     */
    @Override
    public synchronized int submitJob(Resizer resizer) {
        int jobID = currentJobID;
        currentJobID++;
        resizer.setJobID(jobID);
        if (executor != null)
            executor.execute(resizer);
        updateStatus(jobID, Status.IN_PROGRESS);
        return jobID;
    }

    @Override
    public Status getStatus(int jobID) {
        if (!jobMap.containsKey(jobID))
            return Status.NO_SUCH_JOB;
        return jobMap.get(jobID);
    }

    @Override
    public void updateStatus(int jobID, Status status) {
        jobMap.put(jobID, status);
    }

    @Override
    public String getError(int jobID) {
        if (!jobFailures.containsKey(jobID))
            return "";
        return jobFailures.get(jobID);
    }

    @Override
    public void setError(int jobID, String error) {
        jobFailures.put(jobID, error);
        jobMap.put(jobID, Status.FAILED);
    }

    @Override
    public Map<Integer, Status> getAllJobs() {
        return jobMap;
    }
}
