package ca.rbon.grunner.state;

import static org.junit.Assert.*;

import ca.rbon.grunner.api.model.BatchStatus;
import org.junit.Test;

public class BatchMapperTest {

  final BatchMapper mapper = new BatchMapperImpl();

  @Test
  public void statusTwoWay() {
    for (var orig : BatchStatus.values()) {
      var db = mapper.fromAPIStatus(orig);
      var api = mapper.fromDBStatus(db);
      assertEquals(api, orig);
    }
  }

  // TODO test all mappings

}
