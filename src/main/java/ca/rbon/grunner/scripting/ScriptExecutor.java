package ca.rbon.grunner.scripting;

import static ca.rbon.grunner.db.enums.BatchEventStatus.COMPLETED;
import static ca.rbon.grunner.db.enums.BatchEventStatus.FAILED;

import ca.rbon.grunner.state.BatchDAO;
import ca.rbon.grunner.state.Transients;
import java.time.OffsetDateTime;
import javax.annotation.PostConstruct;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Periodically scans the database for batches to execute and runs them on a
 * threadpool.
 */
@Component
public class ScriptExecutor {

  static final Logger LOG = LoggerFactory.getLogger(ScriptExecutor.class);

  final ScriptMachine scriptMachine;

  final BatchDAO batchDAO;

  final ThreadPoolTaskExecutor pool;

  final Transients transients;

  private OffsetDateTime prevBatchTime;

  public ScriptExecutor(ScriptMachine scriptMachine, BatchDAO batchDAO, ThreadPoolTaskExecutor pool, Transients transients) {
    this.scriptMachine = scriptMachine;
    this.batchDAO = batchDAO;
    this.pool = pool;
    this.transients = transients;
  }

  @PostConstruct
  public void init() {
    // FIXME this ignores batches that were still PENDING before last restart
    prevBatchTime = transients.now();
  }

  @Scheduled(fixedDelayString = "${grunner.loop.delay:3000}")
  public void run() {
    var nextPending = batchDAO.nextPendingBatch(prevBatchTime);
    while (nextPending.isPresent()) {
      var event = nextPending.get();
      // if there's an event there _has_ to be a batch
      var batch = batchDAO.getBatch(event.getBatchId()).get();

      pool.execute(() -> {
        try {
          LOG.info("Executing batch '{}' for owner '{}'", batch.getBatchId(), batch.getOwner());
          Object results = scriptMachine.eval(batch.getScript());
          LOG.info("Batch {} completed, results {}", batch.getBatchId(), results);
          batchDAO.addBatchResult(batch.getBatchId(), COMPLETED, results == null ? null : results.toString());

        } catch (ScriptException e) {
          LOG.debug("Execution of the script '{}' for owner '{}' resulted in an error", batch.getBatchId(), batch.getOwner(), e);
          batchDAO.addBatchResult(batch.getBatchId(), FAILED, e.getLocalizedMessage());

        } catch (SecurityException e) {
          LOG.warn("Script attempted operation requiring elevated privileges '{}' for owner '{}'", batch.getBatchId(), batch.getOwner(), e);
          // NOT reporting exact failure source for added security
          batchDAO.addBatchResult(batch.getBatchId(), FAILED, "The script attempted to perform an operation outside of its privileges.");

        } catch (Exception e) {
          LOG.warn("Script execution failed '{}' for owner '{}'", batch.getBatchId(), batch.getOwner(), e);
          // NOT reporting exact failure source for added security
          batchDAO.addBatchResult(batch.getBatchId(), FAILED, "Script execution failed for an unspecified reason.");
        }
      });
      prevBatchTime = event.getEventTime();
      nextPending = batchDAO.nextPendingBatch(prevBatchTime);
    }
  }
}
