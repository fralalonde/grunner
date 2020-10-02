package ca.rbon.grunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GrunnerApp {

  public static void main(String[] args) {
    SpringApplication.run(GrunnerApp.class);
  }

}
