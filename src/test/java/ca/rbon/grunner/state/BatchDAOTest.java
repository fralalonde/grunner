package ca.rbon.grunner.state;

import static ca.rbon.grunner.db.Tables.BATCH;
import static ca.rbon.grunner.db.Tables.BATCH_EVENT;
import static ca.rbon.grunner.db.enums.BatchEventStatus.CANCELLED;
import static ca.rbon.grunner.db.enums.BatchEventStatus.PENDING;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

import ca.rbon.grunner.Integration;
import ca.rbon.grunner.db.tables.records.BatchEventRecord;
import ca.rbon.grunner.db.tables.records.BatchRecord;
import ca.rbon.grunner.scripting.ScriptMachine;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import javax.script.ScriptException;
import org.jooq.DSLContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@JooqTest
@Category(Integration.class)
@ComponentScan(basePackageClasses = BatchDAO.class)
@RunWith(SpringRunner.class)
public class BatchDAOTest {

  static final UUID[] KEYS = {
      UUID.fromString("d105b70b-0401-4f5c-943e-653365f3858b"),
      UUID.fromString("a9070f59-6c32-48b8-9bb4-54df0aad2b37"),
      UUID.fromString("9ddcec07-540d-462c-b1ec-1e32c8d67de9"),
  };

  static final OffsetDateTime[] TIMES = {
      Instant.ofEpochMilli(0).atZone(UTC).toOffsetDateTime(),
      Instant.ofEpochMilli(1000000).atZone(UTC).toOffsetDateTime(),
      Instant.ofEpochMilli(2000000).atZone(UTC).toOffsetDateTime(),
  };

  @MockBean
  ScriptMachine scriptMachine;

  @MockBean
  Transients transients;

  @Autowired
  BatchDAO batchDAO;

  @Autowired
  DSLContext db;

  @Test
  public void appendBatch() throws ScriptException {
    when(transients.uuid()).thenReturn(KEYS[0]);
    when(transients.now()).thenReturn(TIMES[0]);

    batchDAO.appendBatch("user1", "script2");

    Assert.assertEquals(List.of(new BatchRecord(KEYS[0], "script2", "user1")), db.selectFrom(BATCH).stream().collect(toList()));
    Assert.assertEquals(db.selectFrom(BATCH_EVENT).fetchOne(), new BatchEventRecord(KEYS[0], PENDING, TIMES[0], null));
  }

  // TODO complete these tests
  @Test
  public void listUserBatchLastStatus() throws ScriptException {

    var batch0 = new BatchRecord(KEYS[0], "script", "user1");
    db.executeInsert(batch0);
    var event0 = new BatchEventRecord(KEYS[0], PENDING, TIMES[0], null);
    db.executeInsert(event0);

    var batch1 = new BatchRecord(KEYS[1], "script", "user1");
    db.executeInsert(batch1);
    var event1 = new BatchEventRecord(KEYS[1], PENDING, TIMES[1], null);
    db.executeInsert(event1);

    var batch2 = new BatchRecord(KEYS[2], "script", "user2");
    db.executeInsert(batch2);
    var event2 = new BatchEventRecord(KEYS[2], PENDING, TIMES[2], null);
    db.executeInsert(event2);

    var events = batchDAO.listUserBatchLastStatus("user1").collect(toList());
    assertEquals(List.of(event0, event1), events);
  }

  @Test
  public void batchStatusFromLatest() throws ScriptException {
    var batch = new BatchRecord(KEYS[0], "script", "user1");
    db.executeInsert(batch);
    var pending = new BatchEventRecord(KEYS[0], PENDING, TIMES[0], null);
    db.executeInsert(pending);
    var cancelled = new BatchEventRecord(KEYS[0], CANCELLED, TIMES[1], null);
    db.executeInsert(cancelled);

    var lastEvent = batchDAO.batchEventsFromLatest(KEYS[0]);

    assertEquals(cancelled, lastEvent.stream().findFirst().get());
  }

  @Test
  public void cancelUserBatch() {
    var batch0 = new BatchRecord(KEYS[0], "script", "user1");
    db.executeInsert(batch0);
    var event0 = new BatchEventRecord(KEYS[0], PENDING, TIMES[0], null);
    db.executeInsert(event0);

    when(transients.now()).thenReturn(TIMES[1]);

    batchDAO.cancelUserBatch("user1", KEYS[0]);

    var lastEvent = batchDAO.batchEventsFromLatest(KEYS[0]);

    assertEquals(new BatchEventRecord(KEYS[0], CANCELLED, TIMES[1], null), lastEvent.stream().findFirst().get());
  }
}
