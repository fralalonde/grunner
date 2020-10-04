package ca.rbon.grunner.state;

import ca.rbon.grunner.db.enums.BatchEventStatus;
import ca.rbon.grunner.db.tables.records.BatchRecord;
import ca.rbon.grunner.db.tables.records.BatchEventRecord;
import ca.rbon.grunner.scripting.ScriptMachine;
import org.jooq.DSLContext;
import org.jooq.SelectSeekStep1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static ca.rbon.grunner.db.Tables.BATCH;
import static ca.rbon.grunner.db.Tables.BATCH_EVENT;
import static ca.rbon.grunner.db.enums.BatchEventStatus.*;

@Component
public class BatchDAO {

  static final Logger LOG = LoggerFactory.getLogger(BatchDAO.class);

  // TODO make configurable
  private static final int MAX_RESULT_SIZE = 4096;


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
  public UUID appendBatch(String owner, String script) throws ScriptException {
    scriptMachine.compile(script);
    var batchId = transients.uuid();
    db.executeInsert(new BatchRecord(batchId, script, owner));
    db.executeInsert(new BatchEventRecord(batchId, PENDING, transients.now(), null));
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
  public Stream<BatchEventRecord> listUserBatchLastStatus(String user, Optional<BatchEventStatus> status) {
    // FIXME O(n) alert! SQL LATERAL might do it in one swoop but requires Postgres
    return db.select(BATCH.BATCH_ID)
        .from(BATCH)
        .where(BATCH.OWNER.eq(user)).stream()
        .flatMap(rec -> batchEventsFromLatest(rec.component1()).limit(1).stream());
  }

  /**
   * Return a query of a batch's status starting with the latest and going back to
   * the oldest
   */
  SelectSeekStep1<BatchEventRecord, LocalDateTime> batchEventsFromLatest(UUID batchId) {
    return db.selectFrom(BATCH_EVENT)
        .where(BATCH_EVENT.BATCH_ID.eq(batchId))
        .orderBy(BATCH_EVENT.EVENT_TIME.desc());
  }

  /**
   * For executor to find next batch to run
   * @return the batchId or Optional.empty() if no batch is pending
   */
  public Optional<BatchEventRecord> nextPendingBatch(LocalDateTime previous) {
    // TODO add "restart" mode that validates the latest status of jobs
    return db.selectFrom(BATCH_EVENT)
            .where(BATCH_EVENT.STATUS.eq(PENDING))
            .and(BATCH_EVENT.EVENT_TIME.gt(previous))
            // start from oldest
            .orderBy(BATCH_EVENT.EVENT_TIME.asc())
            .stream().findFirst();
  }

  public Optional<BatchRecord> getBatch(UUID batchId) {
    return db.selectFrom(BATCH)
            .where(BATCH.BATCH_ID.eq(batchId))
            .stream().findFirst();
  }

  public void addBatchResult(UUID batchId, BatchEventStatus status, String results) {
    if (results.length() > MAX_RESULT_SIZE) {
      LOG.warn("Max result size exceeded for batch {} truncating to {}", batchId, MAX_RESULT_SIZE);
      results = results.substring(0, MAX_RESULT_SIZE);
    }
    var event = new BatchEventRecord(batchId, status, transients.now(), status.name());
    event.setResults(results);
    db.executeInsert(event);
  }

  public Optional<BatchEventRecord> batchResult(UUID batchId) {
    return db.selectFrom(BATCH_EVENT)
            .where(BATCH_EVENT.BATCH_ID.eq(batchId))
            .and(BATCH_EVENT.STATUS.in(COMPLETED, FAILED))
            .stream()
            .findFirst();
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
  public CancelResult cancelUserBatch(String user, UUID batchId) {
    if (db.selectFrom(BATCH).where(BATCH.OWNER.eq(user)).fetch().isEmpty()) {
      return CancelResult.ERR_JOB_NOT_FOUND;
    }
    var latestStatus = batchEventsFromLatest(batchId).stream().findFirst();
    return latestStatus.map(status ->
      switch (status.getStatus()) {
        case PENDING -> {
          db.executeInsert(new BatchEventRecord(batchId,BatchEventStatus.CANCELLED, transients.now(), null));
          yield CancelResult.OK_JOB_CANCELLED;
        }
        // already cancelled, just ignore for idempotence
        case CANCELLED -> CancelResult.OK_JOB_CANCELLED;
        default -> CancelResult.ERR_JOB_NOT_PENDING;
      }
    ).orElse(CancelResult.ERR_JOB_NOT_FOUND);
  }
}
