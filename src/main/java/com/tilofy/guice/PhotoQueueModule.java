package com.tilofy.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.tilofy.manager.Manager;
import com.tilofy.manager.ImageJobManager;
import com.tilofy.manager.TimeoutManager;
import com.tilofy.rest.PhotoQueueController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;

/**
 * This is used with Guice to inject all our dependencies for our PhotoQueueController.
 */
public class PhotoQueueModule extends AbstractModule {
    @Override
    public void configure() {
        // We're going to keep at least 1 thread available for the webserver at all times.
        // A better implementation would be to spawn these processes on other machines.
        int numResizeThreads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        bind(ExecutorService.class).annotatedWith(Names.named("Executor")).toInstance(Executors.newFixedThreadPool(numResizeThreads));

        bind(TimeoutManager.class).in(Singleton.class);
        bind(Manager.class).to(ImageJobManager.class).in(Singleton.class);
        bind(File.class).annotatedWith(Names.named("Image Directory")).toInstance(new File("/images"));
        bind(Long.class).annotatedWith(Names.named("Timeout")).toInstance(30000l);

        bind(PhotoQueueController.class);
    }
}
