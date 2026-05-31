package visa_holder_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication()
@EnableScheduling
public class VisaHolderTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisaHolderTrackerApplication.class, args);
	}

}