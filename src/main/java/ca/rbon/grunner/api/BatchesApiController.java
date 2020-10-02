package ca.rbon.grunner.api;

import ca.rbon.grunner.api.model.BatchStatus;
import ca.rbon.grunner.api.model.BatchStatusUpdate;
import ca.rbon.grunner.state.BatchDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

import javax.script.ScriptException;
import javax.validation.Valid;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequestMapping("${openapi.grunner.base-path:}")
public class BatchesApiController implements BatchesApi {

  static final Logger LOG = LoggerFactory.getLogger(BatchesApiController.class);

  final NativeWebRequest request;

  final BatchDAO batchDAO;

  public BatchesApiController(NativeWebRequest request, BatchDAO batchDAO/* , BatchMapper batchMapper */) {
    this.request = request;
    this.batchDAO = batchDAO;
  }

  @Override
  public ResponseEntity<Void> cancelBatch(String batchId) {
    var user = request.getRemoteUser();
    var cancelResult = batchDAO.cancelUserBatch(user, batchId);
    return switch (cancelResult) {
    case OK_JOB_CANCELLED -> ResponseEntity.noContent().build();
    case ERR_JOB_NOT_FOUND -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    case ERR_JOB_NOT_PENDING -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    };
  }

  @Override
  public ResponseEntity<String> enqueueBatch(@Valid String body) {
    try {
      var user = request.getRemoteUser();
      var newBatch = batchDAO.appendBatch(user, body);
      return accepted().body(newBatch);
    } catch (ScriptException se) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("Script Error at Line [%d] Column [%d]:\n\t%s",
              se.getLineNumber(), se.getColumnNumber(), se.getLocalizedMessage()));
    }
  }

  @Override
  public ResponseEntity<List<BatchStatusUpdate>> listBatchs(@Valid Optional<BatchStatus> status) {
    var user = request.getRemoteUser();
    return ok(batchDAO.listUserBatchLastStatus(user, status).flatMap(rec -> {
      var recstatus = BatchStatus.valueOf(rec.component2());
      if (recstatus == null) {
        LOG.error("Unmapped status {} for batch {}", rec.component2(), rec.component1());
        return Stream.empty();
      }
      var upd = new BatchStatusUpdate();
      upd.setBatchId(rec.component1());
      upd.setStatus(recstatus);
      upd.setTimestamp(rec.component3().atOffset(ZoneOffset.UTC));
      return Stream.of(upd);
    }).collect(toList()));
  }
}
