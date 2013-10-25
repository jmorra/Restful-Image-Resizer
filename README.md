# What is this? 
This is a very simple backend service written in Java that handles image resizing.  It is designed as a toy project
using the following libraries

* JBoss 7.1.1.Final -- Gotten from [here](http://www.jboss.org/jbossas/downloads/).  The version is important.
* RestEasy
* Guice
* Guava
* JUnit
* Jackson
* ImgScalr
* Maven

## Build Instructions
In order to build the project, navigate to the project directory and type `mvn clean verify` (this assumes `mvn` is
on your path).  This will also run the full test suite included.

## Deploy Instructions
This application was written for JBoss 7.1.1.  In order to deploy, copy the `target/homework-1.0.war` file to
`<jboss_home>/standalone/deployments`.  From there, start the JBoss server by running standalone from the
`<jboss_home>/bin` directory.

## Usage
This program only has a backend and communicates via JSON.  The program responds to 4 REST commands

* `homework-1.0/queue` -- index
* `homework-1.0/queue/<job_id>` -- show
* `homework-1.0/queue/<job_id>.jpg` -- show the image
* `homework-1.0/queue/?url=<some_image_url>&size=<width>x<height>` -- create

The flow is that the user navigates to the create action and supplies the URL of an image to be resized and the
new size.  The user then gets a job ID for reference.  At some point in the future the user can check the job ID
using either the show page to get the status, or, if the job is finished, the show.jpg page to see the result.  At
any time, the user can navigate to the index to check the status of all their jobs.


