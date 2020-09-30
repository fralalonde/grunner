package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.JobStatus;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class JobModel {
    String id;

    JobStatus status;

    OffsetDateTime submitDate;

    String script;

    String owner;
}
