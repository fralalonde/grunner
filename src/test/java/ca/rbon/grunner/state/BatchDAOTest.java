package ca.rbon.grunner.state;

import ca.rbon.grunner.Integration;
import ca.rbon.grunner.db.tables.records.BatchEventRecord;
import ca.rbon.grunner.db.tables.records.BatchRecord;
import ca.rbon.grunner.db.tables.records.BatchRecord;
import ca.rbon.grunner.scripting.ScriptMachine;
import org.jooq.DSLContext;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.script.ScriptException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static ca.rbon.grunner.api.model.BatchStatus.PENDING;
import static ca.rbon.grunner.db.Tables.BATCH;
import static ca.rbon.grunner.db.Tables.BATCH_EVENT;
import static org.mockito.Mockito.*;

@JooqTest
@Category(Integration.class)
@ComponentScan(basePackageClasses = BatchDAO.class)
@RunWith(SpringRunner.class)
class BatchDAOTest {

  static final UUID[] KEYS = {
      UUID.fromString("d105b70b-0401-4f5c-943e-653365f3858b"),
      UUID.fromString("a9070f59-6c32-48b8-9bb4-54df0aad2b37"),
      UUID.fromString("9ddcec07-540d-462c-b1ec-1e32c8d67de9")
  };

  static final LocalDateTime[] TIMES = {
      LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
      LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.UTC),
      LocalDateTime.ofEpochSecond(2, 2, ZoneOffset.UTC),
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
  void appendBatch() throws ScriptException {
    when(transients.uuid()).thenReturn(KEYS[0]);
    when(transients.now()).thenReturn(TIMES[0]);
    batchDAO.appendBatch("user1", "script2");
    Assert.assertEquals(db.selectFrom(BATCH).fetchOne(), new BatchRecord(KEYS[0].toString(), "script2", "user1"));
    Assert.assertEquals(db.selectFrom(BATCH_EVENT).fetchOne(), new BatchEventRecord(KEYS[0].toString(), PENDING.name(), TIMES[0], null));
  }

  @Test
  void listUserBatchLastStatus() throws ScriptException {
//    when(transients.uuid()).thenReturn(KEYS[0]).thenReturn(KEYS[1]);
//    when(transients.now()).thenReturn(TIMES[0]).thenReturn(TIMES[1]);
//    batchDAO.appendBatch("user1", "script2");
//    batchDAO.appendBatch("user4", "script3");
    batchDAO.listUserBatchLastStatus("user1", Optional.empty());
  }

  @Test
  void batchStatusFromLatest() {
    batchDAO.batchStatusFromLatest("batch1");
  }

  @Test
  void cancelUserBatch() {
    batchDAO.cancelUserBatch("user1", "batch1");
  }
}