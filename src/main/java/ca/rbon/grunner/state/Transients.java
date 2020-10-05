package ca.rbon.grunner.state;

import static java.time.ZoneOffset.UTC;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Externalize transients (clock, random, etc.) to enable reproducible tests
 * with mocks
 */
@Component
public class Transients {

  /**
   * @return the current time
   */
  public OffsetDateTime now() {
    return OffsetDateTime.now(UTC);
  }

  /**
   * @return a fresh UUID
   */
  public UUID uuid() {
    return UUID.randomUUID();
  }

}
