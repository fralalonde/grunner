package ca.rbon.grunner.scripting;

import ca.rbon.grunner.state.BatchDAO;
import ca.rbon.grunner.state.Transients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.script.ScriptException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static ca.rbon.grunner.db.enums.BatchEventStatus.COMPLETED;
import static ca.rbon.grunner.db.enums.BatchEventStatus.FAILED;

@Component
public class ScriptExecutor {

  static final Logger LOG = LoggerFactory.getLogger(ScriptExecutor.class);

  final ScriptMachine scriptMachine;

  final BatchDAO batchDAO;

  final ThreadPoolTaskExecutor pool;

  private LocalDateTime prevBatchTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

  public ScriptExecutor(ScriptMachine scriptMachine, BatchDAO batchDAO, ThreadPoolTaskExecutor pool) {
    this.scriptMachine = scriptMachine;
    this.batchDAO = batchDAO;
    this.pool = pool;
  }

  @Scheduled(fixedDelay = 3000)
  public void run() throws InterruptedException {
    while (true) {
      var nextPending = batchDAO.nextPendingBatch(prevBatchTime);
      if (!nextPending.isPresent()) {
        // TODO make configurable
        Thread.sleep(1000);
        LOG.debug("No pending batch, waiting");
      } else {
        // TODO clean up optionals
        var event = nextPending.get();
        var batch = batchDAO.getBatch(event.getBatchId()).get();

        pool.execute(() -> {
          try {
            LOG.info("Executing batch '{}' for owner '{}'", batch.getBatchId(), batch.getOwner());
            Object results = scriptMachine.eval(batch.getScript());
            batchDAO.addBatchResult(batch.getBatchId(), COMPLETED, results == null ? null : results.toString());
          } catch (ScriptException e) {
            LOG.info("Executing batch '{}' for owner '{}'", batch.getBatchId(), batch.getOwner());
            batchDAO.addBatchResult(batch.getBatchId(), FAILED, e.getLocalizedMessage());
          }
        });
        prevBatchTime = event.getEventTime();
      }
    }
  }
}
