package ca.rbon.grunner.api;

import ca.rbon.grunner.api.model.JobStatus;
import ca.rbon.grunner.api.model.JobSummary;
import ca.rbon.grunner.state.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-09-29T13:12:41.426889-04:00[America/Montreal]")

@Controller
@RequestMapping("${openapi.grunner.base-path:}")
public class JobsApiController implements JobsApi {

    final NativeWebRequest request;

    final JobRepository jobRepository;

    public JobsApiController(NativeWebRequest request, JobRepository jobRepository) {
        this.request = request;
        this.jobRepository = jobRepository;
    }

    @Override
    public ResponseEntity<String> createJob(@Valid String body) {
        var user = request.getRemoteUser();
        var id = jobRepository.appendJob(user, body);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(id.toString());
    }

    @Override
    public ResponseEntity<List<JobSummary>> listJobs(@Valid Optional<JobStatus> status) {
        return null;
    }
}
