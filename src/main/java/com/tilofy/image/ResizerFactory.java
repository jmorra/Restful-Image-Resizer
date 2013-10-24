package com.tilofy.image;

import com.tilofy.manager.Manager;

import java.net.URL;

/**
 * A factory for getting resizers.
 * TODO Replace this with Guice.
 */
public class ResizerFactory {
    public static Resizer getURLResizer(URL url, int targetWidth, int targetHeight, Manager manager) {
        return new URLResizer(url, targetWidth, targetHeight, manager);
    }
}
