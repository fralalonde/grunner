package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.JobStatus;
import ca.rbon.grunner.scripting.ScriptEngine;
import org.springframework.stereotype.Component;

import javax.script.ScriptException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

@Component
public class JobDAO {

    /**
     * For CRUD operations
     */
    final JobRepository repository;

    /**
     * Used to validate script syntax before job creation
     */
    final ScriptEngine scriptEngine;

    public JobDAO(JobRepository repository, ScriptEngine scriptEngine) {
        this.repository = repository;
        this.scriptEngine = scriptEngine;
    }

    //    private final Job jobs = new Job();

//    private final DSLContext dsl;
//
//    private final TransactionTemplate transactionTemplate;
//
//    final Map<UUID, JobInfo> jobs = new ConcurrentHashMap<>();

//    public JobRepository(DSLContext dsl, TransactionTemplate transactionTemplate) {
//        this.dsl = dsl;
//        this.transactionTemplate = transactionTemplate;
//    }

    public JobDTO appendJob(String owner, String script) throws ScriptException {
        scriptEngine.compile(script);
        var id = UUID.randomUUID();
        var job = JobDTO.builder()
                .id(id.toString())
                .owner(owner)
                .script(script)
                .submitDate(OffsetDateTime.now(UTC))
                .status(JobStatus.PENDING)
                .build();
        repository.save(job);
        return job;
    }

    public List<JobDTO> listAllUserJobs(String user) {
        return repository.findByOwner(user);
    }

    public Optional<JobDTO> waitForNextPendingJob() {
        return null;
    }



}
