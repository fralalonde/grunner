package ca.rbon.grunner;

import org.junit.experimental.categories.Category;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Category(Integration.class)
class GrunnerAppTests {

  @Test
  void contextLoads() {
    // this smoke test starts the app and initializes the spring context
  }

}
