# What is this? 
This homework is a part of the interview process for Tilofy. The idea is to take a real world problem we have at Tilofy and try to present to candidates. The candidate can complete the homework in their own time. The code should be your highest quality code that you would deliver to your team for testing in production. 

# What is the goal?
The goal is to create a web application that can resize images without blocking. This means you need to create a web application that can queue up images and later fetched when complete. Your web application should be able to do two things.

### Queuing Images
A client should be able to queue images by doing a POST to /queue.

	POST http://domain.com/queue/?url=http://example.com/test.jpg&size=800x600

The above would queue up test.jpg to be resized at a later time and return with a job id like

	{jobId: 41}
	
Later, you should be able to fetch the images by doing a GET 

	GET http://domain.com/queue/41
	
The above should return a list of all the images, in JSON, that had been queued for job id 41. These images should point to a url that can be fetched. 	

# What does production ready code mean? 
You should make all effort to test the code. At Tilofy, we are a small team. We don't have time for testing by clicking on every button. That's why we believe in automated testing with continuous integration and deployment. If you do everything right, your code will be deployed to production in 60 seconds or less. 

# What else do I need to know? 
This project is written in Java. It uses Maven, for compiling and testing. You should make sure that your tests run by doing 

	$ mvn clean verify 
	
If your tests don't run correclty or there is a compile error then the homework is rejected immedietly. 

After you have written code to test, you may run the web app by doing:

	$ mvn jetty:run
	
# Notes
Your homework will be submission will be graded on 

* Testing
* Documentation
* Code quality
* Threading or performance


