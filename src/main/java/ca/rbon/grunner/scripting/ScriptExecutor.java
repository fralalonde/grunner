package ca.rbon.grunner.scripting;

import ca.rbon.grunner.api.BatchesApiController;
import ca.rbon.grunner.state.BatchDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.script.ScriptException;

import static ca.rbon.grunner.api.model.BatchStatus.PENDING;

@Component
public class ScriptExecutor {

  static final Logger LOG = LoggerFactory.getLogger(BatchesApiController.class);

  final ScriptMachine scriptMachine;

  final BatchDAO batchDAO;

  final TaskExecutor pool;

  public ScriptExecutor(ScriptMachine scriptMachine, BatchDAO batchDAO, TaskExecutor pool) {
    this.scriptMachine = scriptMachine;
    this.batchDAO = batchDAO;
    this.pool = pool;
  }

  @Scheduled(fixedDelay = 3000)
  public void run() throws InterruptedException {
    while (true) {
      var nextPending = batchDAO.nextPendingBatch();
      if (!nextPending.isPresent()) {
        LOG.debug("No pending batch, waiting");
      } else {
        while (nextPending.isPresent() && !Thread.interrupted()) {
          var batch = nextPending.get();
          pool.execute(() -> {
            try {
              LOG.info("Executing batch '{}' for owner '{}'", batch.getId(), batch.getOwner());
              scriptMachine.eval(batch.getScript());
              // TODO capture & save result
            } catch (ScriptException e) {
              LOG.info("Executing batch '{}' for owner '{}'", batch.getId(), batch.getOwner());
              // TODO capture & save error to rec
            }
          });
          nextPending = batchDAO.nextPendingBatch();
        }
      }
      // TODO make configurable
      Thread.sleep(1000);
    }
  }
}
