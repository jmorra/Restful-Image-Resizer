package com.tilofy.image;

/**
 * An abstract class to represent something that can resize an image.
 */
public abstract class Resizer implements Runnable {
    protected int targetWidth;
    protected int targetHeight;
    protected int jobID;

    public Resizer(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }
}
