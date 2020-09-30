package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.JobStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;

@Entity(name = "JOBS")
// NOTE would prefer to use @Value but Hibernate forces presence of default constructor
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDTO {
    @Id
    @NonNull
    String id;

    @NonNull
    JobStatus status;

    @NonNull
    OffsetDateTime submitDate;

    @NonNull
    String script;

    @NonNull
    String owner;
}
