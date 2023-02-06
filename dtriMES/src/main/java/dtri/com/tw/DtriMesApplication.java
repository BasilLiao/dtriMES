package dtri.com.tw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DtriMesApplication {
	public static void main(String[] args) {
		SpringApplication.run(DtriMesApplication.class, args);
	}
}
