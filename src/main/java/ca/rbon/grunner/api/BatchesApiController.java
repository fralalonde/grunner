package ca.rbon.grunner.api;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.http.ResponseEntity.ok;

import ca.rbon.grunner.api.model.BatchResult;
import ca.rbon.grunner.api.model.BatchStatusUpdate;
import ca.rbon.grunner.state.BatchDAO;
import ca.rbon.grunner.state.BatchMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.script.ScriptException;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("${openapi.grunner.base-path:}")
public class BatchesApiController implements BatchesApi {

  static final Logger LOG = LoggerFactory.getLogger(BatchesApiController.class);

  final NativeWebRequest request;

  final BatchDAO batchDAO;

  final BatchMapper batchMapper;

  public BatchesApiController(NativeWebRequest request, BatchDAO batchDAO, BatchMapper batchMapper) {
    this.request = request;
    this.batchDAO = batchDAO;
    this.batchMapper = batchMapper;
  }

  @Override
  public ResponseEntity<BatchResult> batchResults(UUID batchId) {
    return ok(batchDAO.batchResult(batchId)
        .map(batchMapper::fromDBResult)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
  }

  @Override
  public ResponseEntity<Void> cancelBatch(UUID batchId) {
    var user = request.getRemoteUser();
    var cancelResult = batchDAO.cancelUserBatch(user, batchId);
    return switch (cancelResult) {
    case OK_JOB_CANCELLED -> ResponseEntity.noContent().build();
    case ERR_JOB_NOT_FOUND -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    case ERR_JOB_NOT_PENDING -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    };
  }

  @Override
  public ResponseEntity<UUID> enqueueBatch(@Valid String body) {
    try {
      var user = request.getRemoteUser();
      var newBatchId = batchDAO.appendBatch(user, body);
      return accepted().body(newBatchId);
    } catch (ScriptException se) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("Script Error at Line [%d] Column [%d]:\n\t%s",
              se.getLineNumber(), se.getColumnNumber(), se.getLocalizedMessage()));
    }
  }

  @Override
  public ResponseEntity<List<BatchStatusUpdate>> listBatches() {
    var user = request.getRemoteUser();
    return ok(batchDAO.listUserBatchLastStatus(user)
        .flatMap(rec -> Stream.of(batchMapper.fromDBEvent(rec)))
        .collect(toList()));
  }
}
