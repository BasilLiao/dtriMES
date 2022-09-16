package dtri.com.tw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DtriPmsApplication {
	public static void main(String[] args) {
		SpringApplication.run(DtriPmsApplication.class, args);
	}
}
