package com.tilofy.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.concurrent.*;

/**
 * This class will manage all the jobs and delete those that timeout.
 */
@Singleton
public class TimeoutManager {
    private ConcurrentHashMap<Integer, Future> runningJobs = new ConcurrentHashMap<Integer, Future>();
    private ConcurrentHashMap<Integer, Long> jobStarts = new ConcurrentHashMap<Integer, Long>();
    private long timeout;
    private Manager manager;

    @Inject
    public TimeoutManager(@Named("Timeout") long timeout) {
        this.timeout = timeout;
        setupCleanupService();
    }

    @Inject
    public void setManager(Manager manager) {
        this.manager = manager;
    }

    private void setupCleanupService() {
        // This will make sure any long running tasks stop.  We'll run this every 1 second
        ScheduledExecutorService cleanUp = Executors.newScheduledThreadPool(1);
        Runnable cleaner = new Runnable() {
            @Override
            public void run() {
                // We don't want to do any cleanup if we haven't assigned the manager yet
                if (manager == null)
                    return;
                for (int jobID : runningJobs.keySet()) {
                    if (jobStarts.containsKey(jobID) && System.currentTimeMillis() - jobStarts.get(jobID) > timeout) {
                        runningJobs.get(jobID).cancel(true);
                        manager.updateStatus(jobID, Status.TIMED_OUT);
                    }
                }
            }
        };
        cleanUp.scheduleAtFixedRate(cleaner, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * This will remove the jobID from the maps which no longer need it.
     * @param jobID The job ID to be cleaned.
     */
    public void cleanUpJob(int jobID) {
        runningJobs.remove(jobID);
        jobStarts.remove(jobID);
    }

    /**
     * Sets the supplied jobID's future.  This will also set the status to in progress.
     * @param jobID The job ID in question
     * @param job The future for the jobIDs
     */
    public void setInProgress(int jobID, Future job) {
        runningJobs.put(jobID, job);
        jobStarts.put(jobID, System.currentTimeMillis());
        manager.updateStatus(jobID, Status.IN_PROGRESS);
    }
}
