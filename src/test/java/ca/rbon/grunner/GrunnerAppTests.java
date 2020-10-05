package ca.rbon.grunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Category(Integration.class)
public class GrunnerAppTests {

  @BeforeClass
  public static void init() {

  }

  @Test
  public void contextLoads() {
    // this smoke test starts the app and initializes the spring context
  }

}
