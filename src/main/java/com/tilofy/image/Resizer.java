package com.tilofy.image;

/**
 * An abstract class to represent something that can resize an image.
 */
public abstract class Resizer implements Runnable {
    protected int targetWidth;
    protected int targetHeight;
    protected int jobID;

    public void setDimensions(int targetWidth, int targetHeight) {
        this.targetHeight = targetHeight;
        this.targetWidth = targetWidth;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public abstract void setTargetImage(Object image);
}
