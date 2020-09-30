package ca.rbon.grunner.api;

import ca.rbon.grunner.api.model.ErrorDetail;
import ca.rbon.grunner.api.model.JobStatus;
import ca.rbon.grunner.api.model.JobSummary;
import ca.rbon.grunner.state.JobDAO;
import ca.rbon.grunner.state.JobMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import javax.script.ScriptException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.ok;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-09-29T13:12:41.426889-04:00[America/Montreal]")

@Controller
@RequestMapping("${openapi.grunner.base-path:}")
public class JobsApiController implements JobsApi {

    final NativeWebRequest request;

    final JobDAO jobDAO;

    final JobMapper jobMapper;

    public JobsApiController(NativeWebRequest request, JobDAO jobDAO, JobMapper jobMapper) {
        this.request = request;
        this.jobDAO = jobDAO;
        this.jobMapper = jobMapper;
    }

    @Override
    public ResponseEntity<JobSummary> createJob(@Valid String body) {
        var user = request.getRemoteUser();
        try {
            var newJob = jobDAO.appendJob(user, body);
            return ResponseEntity.accepted().body(jobMapper.jobDTOToJobSummary(newJob));
        } catch (ScriptException e) {
            var error = new ErrorDetail();
            error.setMessage(e.getMessage());
            // TODO return ErrorDetail
            return ResponseEntity.badRequest()/*.body(e.getMessage())*/.build();
        }
    }

    @Override
    public ResponseEntity<List<JobSummary>> listJobs(@Valid Optional<JobStatus> status) {
        var user = request.getRemoteUser();
        return ok(jobDAO.listAllUserJobs(user).stream().map(jobMapper::jobDTOToJobSummary).collect(toList()));
    }
}
