package com.tilofy.manager;

import java.io.File;
import java.util.Map;
import com.tilofy.image.Resizer;

/**
 * A Manager manages all Resizer jobs.  It stores the job status and submission.
 */
public interface Manager {
    /**
     * Sets the timeout manager
     * @param timeoutManager The TimeoutManager
     */
    public void setTimeoutManager(TimeoutManager timeoutManager);

    /**
     * Submits the supplied resizer job and returns the status.  This method should return immediately.
     * @param resizer A resizer to do the resizing
     * @return The job ID
     */
    public int submitJob(Resizer resizer);

    /**
     * Gets the status of the supplied jobID.
     * @param jobID The job ID of the status in question
     * @return The status of the supplied jobID
     */
    public Status getStatus(int jobID);

    /**
     * Updats the status of the supplied jobID to the supplied status.
     * @param jobID The job ID of the status in question
     * @param status The new status for the jobID
     */
    public void updateStatus(int jobID, Status status);

    /**
     * Sets the supplied jobID's error string.  This will also set the status to failed.
     * @param jobID The job ID in question
     * @param error The error string for the jobID
     */
    public void setError(int jobID, String error);

    /**
     * Gets the error string for the supplied jobID.  If there is none, returns an empty string.
     * @param jobID The job ID in question
     * @return The error for the supplied jobID, or an empty string
     */
    public String getError(int jobID);

    /**
     * Gets the output directory where the images are written to.
     * @return The image output directory
     */
    public File getOutputDirectory();

    /**
     * Gets a map of all jobs.  This method could potentially be costly as the number of jobs grows.
     * @return A map of all jobs and their statuses
     */
    public Map<Integer, Status> getAllJobs();

    /**
     * Returns the file that the supplied job ID corresponds to, if it's done.
     * @param jobID The job ID in question
     * @return The file for the image, or null if the job isn't finished
     */
    public File getOutputFile(int jobID);

    /**
     * Sets the supplied jobID's output file.  This will also set the status to completed.
     * @param jobID The job ID in question
     * @param file The file for the jobID
     */
    public void setOutputFile(int jobID, File file);
}
