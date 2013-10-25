package com.tilofy.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.tilofy.image.Resizer;

import java.io.File;
import java.util.concurrent.*;
import java.util.Map;

/**
 *  An in memory implementation of a Manager.  This should be treated as a Singleton class.  It uses Guice
 *  to get it's dependencies.  It will keep track of status and errors with ConcurrentHashMaps because
 *  multiple threads could access these objects at the same time.
 */
@Singleton
public class ImageJobManager implements Manager {
    private File outputDirectory;
    private int currentJobID = 0;
    // These maps keep track of all of the information about a job
    private ConcurrentHashMap<Integer, Status> jobMap = new ConcurrentHashMap<Integer, Status>();
    private ConcurrentHashMap<Integer, String> jobFailures = new ConcurrentHashMap<Integer, String>();
    private ConcurrentHashMap<Integer, File> jobFiles = new ConcurrentHashMap<Integer, File>();
    private ExecutorService executor;
    private TimeoutManager timeoutManager;

    @Inject
    public ImageJobManager(@Named("Image Directory") File outputDirectory,
                           @Named("Executor") ExecutorService executor) {
        this.outputDirectory = outputDirectory;
        this.executor = executor;
        if (!outputDirectory.exists() || !outputDirectory.isDirectory())
            outputDirectory.mkdirs();
    }

    public void setTimeoutManager(TimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
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
        if (executor != null) {
            Future job = executor.submit(resizer);
            if (timeoutManager == null)
                updateStatus(jobID, Status.IN_PROGRESS);
            else
                timeoutManager.setInProgress(jobID, job);
        }
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
        if (timeoutManager != null && (status == Status.FAILED || status == Status.COMPLETED || status == Status.TIMED_OUT))
            timeoutManager.cleanUpJob(jobID);
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
        if (timeoutManager != null)
            timeoutManager.cleanUpJob(jobID);
    }

    @Override
    public Map<Integer, Status> getAllJobs() {
        return jobMap;
    }

    @Override
    public File getOutputFile(int jobID) {
        return jobFiles.get(jobID);
    }

    @Override
    public void setOutputFile(int jobID, File file) {
        jobFiles.put(jobID, file);
        jobMap.put(jobID, Status.COMPLETED);
        if (timeoutManager != null)
            timeoutManager.cleanUpJob(jobID);
    }
}
