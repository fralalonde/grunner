package ca.rbon.grunner.api;

import ca.rbon.grunner.api.model.JobStatus;
import ca.rbon.grunner.api.model.JobSummary;
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

    private final NativeWebRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public JobsApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public ResponseEntity<String> createJob() {
        return null;
    }

    @Override
    public ResponseEntity<List<JobSummary>> listJobs(@Valid Optional<JobStatus> status) {
        return null;
    }
}
