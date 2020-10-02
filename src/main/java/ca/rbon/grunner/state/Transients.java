package ca.rbon.grunner.state;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Externalize transients (clock, random, etc.) to enable reproducible tests
 * with mocks
 */
@Component
public class Transients {

  /**
   * @return the current time
   */
  public LocalDateTime now() {
    return LocalDateTime.now();
  }

  /**
   * @return a fresh UUID
   */
  public UUID uuid() {
    return UUID.randomUUID();
  }

}
