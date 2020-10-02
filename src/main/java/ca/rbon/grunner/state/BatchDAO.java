package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.BatchStatus;
import ca.rbon.grunner.db.tables.records.BatchRecord;
import ca.rbon.grunner.db.tables.records.BatchEventRecord;
import ca.rbon.grunner.scripting.ScriptMachine;
import org.jooq.DSLContext;
import org.jooq.SelectSeekStep1;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptException;
import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static ca.rbon.grunner.db.Tables.BATCH;
import static ca.rbon.grunner.db.Tables.BATCH_EVENT;

@Component
public class BatchDAO {

  /**
   * JOOQ query builder root
   */
  final DSLContext db;

  /**
   * Used to validate script syntax before batch creation
   */
  final ScriptMachine scriptMachine;

  /**
   * Source of UUIDs and real time
   */
  final Transients transients;

  public BatchDAO(ScriptMachine scriptMachine, DSLContext db, Transients transients) {
    this.db = db;
    this.scriptMachine = scriptMachine;
    this.transients = transients;
  }

  /**
   * Create a new batch to track
   * 
   * @param owner  name of the new batch's owner
   * @param script the batch's script
   * @return The ID of the new batch
   * @throws ScriptException if the script does not compile
   */
  @Transactional
  public String appendBatch(String owner, String script) throws ScriptException {
    scriptMachine.compile(script);
    var batchId = transients.uuid().toString();
    db.executeInsert(new BatchRecord(batchId, script, owner));
    db.executeInsert(new BatchEventRecord(batchId, BatchStatus.PENDING.name(), transients.now(), null));
    return batchId;
  }

  /**
   * Returns the user's batchs latest status
   * 
   * @param user   The user
   * @param status If specified, only batchs with the matching latest status will be
   *               returned, otherwise status of all batchs are returned
   */
  @Transactional
  public Stream<BatchEventRecord> listUserBatchLastStatus(String user, Optional<BatchStatus> status) {
    // FIXME O(n) alert! SQL LATERAL might do it in one swoop but requires Postgres
    return db.select(BATCH.ID)
        .from(BATCH)
        .where(BATCH.OWNER.eq(user)).stream()
        .flatMap(rec -> batchStatusFromLatest(rec.component1()).limit(1).stream());
  }

  /**
   * Return a query of a batch's status starting with the latest and going back to
   * the oldest
   */
  SelectSeekStep1<BatchEventRecord, LocalDateTime> batchStatusFromLatest(String batchId) {
    return db.selectFrom(BATCH_EVENT)
        .where(BATCH_EVENT.BATCH_ID.eq(batchId))
        .orderBy(BATCH_EVENT.EVENT_TIME.desc());
  }

  /**
   * For executor to find next batch to run
   */
  public Optional<BatchRecord> nextPendingBatch() {
    return Optional.empty();
  }

  /**
   * Possible results of an attempted batch cancellation.
   */
  public enum CancelResult {
    OK_JOB_CANCELLED, ERR_JOB_NOT_FOUND, ERR_JOB_NOT_PENDING,
  }

  /**
   * Try to cancel a user's batch
   */
  @Transactional
  public CancelResult cancelUserBatch(String user, String batchId) {
    if (db.selectFrom(BATCH).where(BATCH.OWNER.eq(user)).fetch().isEmpty()) {
      return CancelResult.ERR_JOB_NOT_FOUND;
    }
    var lastStatus = BatchStatus.valueOf(batchStatusFromLatest(batchId).fetchAny().getNewStatus());
    return switch (lastStatus) {
    case PENDING -> {
      db.executeInsert(new BatchEventRecord(batchId, BatchStatus.CANCELLED.name(), transients.now(), null));
      yield CancelResult.OK_JOB_CANCELLED;
    }
    // already cancelled, ignore for idempotence
    case CANCELLED -> CancelResult.OK_JOB_CANCELLED;
    default -> CancelResult.ERR_JOB_NOT_PENDING;
    };
  }
}
