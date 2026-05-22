package visa_holder_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"visa_holder_tracker", "Controller", "Authentication"})
public class VisaHolderTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisaHolderTrackerApplication.class, args);
	}

}
