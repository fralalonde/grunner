package ca.rbon.grunner.api;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ca.rbon.grunner.Integration;
import ca.rbon.grunner.api.model.BatchResult;
import ca.rbon.grunner.api.model.BatchStatus;
import ca.rbon.grunner.db.enums.BatchEventStatus;
import ca.rbon.grunner.db.tables.records.BatchEventRecord;
import ca.rbon.grunner.state.BatchDAO;
import ca.rbon.grunner.state.BatchMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.script.ScriptException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;

@RunWith(SpringRunner.class)
@Category(Integration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BatchesApiControllerTest {

  static final UUID[] KEYS = {
      UUID.fromString("d105b70b-0401-4f5c-943e-653365f3858b"),
      UUID.fromString("a9070f59-6c32-48b8-9bb4-54df0aad2b37"),
  };

  static final OffsetDateTime[] TIMES = {
      Instant.ofEpochMilli(0).atZone(UTC).toOffsetDateTime(),
      Instant.ofEpochMilli(1000000).atZone(UTC).toOffsetDateTime(),
  };

  @Autowired
  private TestRestTemplate template;

  @MockBean
  BatchDAO batchDAO;

  @MockBean
  BatchMapper batchMapper;

  @Test(expected = ResourceAccessException.class)
  public void noAuth() throws Exception {
    var response = template.postForEntity("/batches", "script", String.class);
    // FIXME prevent throwing, validate actual response
    // assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  public void enqueueBatch() throws ScriptException {
    when(batchDAO.appendBatch(eq("user"), eq("script"))).thenReturn(KEYS[0]);
    var response = template.withBasicAuth("user", "secret").postForEntity("/batches", "script", UUID.class);
    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertEquals(KEYS[0], response.getBody());
  }

  @Ignore("not debugged yet")
  @Test
  public void batchResults() {
    var event = new BatchEventRecord(KEYS[0], BatchEventStatus.COMPLETED, TIMES[0], "bzzz");
    when(batchDAO.batchResult((eq(KEYS[0])))).thenReturn(Optional.of(event));

    var response = template.withBasicAuth("user", "secret").getForEntity("/batches/" + KEYS[0] + "/results", BatchResult.class);

    var exp = new BatchResult();
    exp.setStatus(BatchStatus.PENDING);
    exp.setTimestamp(TIMES[0]);
    exp.setBatchId(KEYS[0]);
    exp.setResults("bzzz");
    assertEquals(exp, response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void cancelBatch() {
  }

  @Test
  public void listBatches() {
  }
}
