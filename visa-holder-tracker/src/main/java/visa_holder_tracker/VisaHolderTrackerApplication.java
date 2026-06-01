package visa_holder_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Main entry point for the Visa Holder Tracker application.
 *
 * <p>
 * This class:
 * <ul>
 *     <li>Bootstraps the Spring Boot application.</li>
 *     <li>Initializes the Spring application context.</li>
 *     <li>Enables scheduled background tasks using Spring Scheduling.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Scheduled tasks include:
 * <ul>
 *     <li>Notification scheduling</li>
 *     <li>Monthly report generation</li>
 * </ul>
 * </p>
 */
@SpringBootApplication()
@EnableScheduling
public class VisaHolderTrackerApplication {


	/**
	 * Application startup method.
	 *
	 * <p>
	 * Launches the Spring Boot application
	 * and initializes all configured beans,
	 * services, controllers, repositories,
	 * and scheduled tasks.
	 * </p>
	 *
	 * @param args command-line startup arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(VisaHolderTrackerApplication.class, args);
	}

}