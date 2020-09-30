package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.JobInfo;
import ca.rbon.grunner.api.model.JobStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.ZoneOffset.UTC;

@Component
public class JobRepository {

    final Map<UUID, JobInfo> jobs = new ConcurrentHashMap<>();

    public UUID appendJob(String user, String script) {
        var id = UUID.randomUUID();
        var  job = new JobInfo();
        job.setId(id.toString());
        job.setStatus(JobStatus.PENDING);
        job.setScript(script);
        job.setOwner(user);
        job.setSubmitDate(OffsetDateTime.now(UTC));
        jobs.put(id, job);
        return id;
    }




}
