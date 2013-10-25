package com.tilofy.guice;

import com.google.inject.Module;
import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.tilofy.image.Resizer;
import com.tilofy.image.URLResizer;
import com.tilofy.manager.Manager;
import com.tilofy.manager.ImageJobManager;
import com.tilofy.manager.TimeoutManager;
import com.tilofy.rest.PhotoQueueController;

import java.util.concurrent.Executors;
import java.util.concurrent.Executor;
import java.io.File;

/**
 * This is used with Guice to inject all our dependencies for our PhotoQueueController.
 */
public class PhotoQueueModule implements Module {
    @Override
    public void configure(final Binder binder) {
        binder.bind(TimeoutManager.class).in(Singleton.class);
        binder.bind(Manager.class).to(ImageJobManager.class).in(Singleton.class);
        binder.bind(Resizer.class).to(URLResizer.class);
        binder.bind(File.class).annotatedWith(Names.named("Image Directory")).toInstance(new File("/images"));
        binder.bind(Long.class).annotatedWith(Names.named("Timeout")).toInstance(30000l);

        // We're going to keep at least 1 thread available for the webserver at all times.
        // A better implementation would be to spawn these processes on other machines.
        int numResizeThreads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        binder.bind(Executor.class).annotatedWith(Names.named("Executor")).toInstance(Executors.newFixedThreadPool(numResizeThreads));
        binder.bind(PhotoQueueController.class);
    }
}
