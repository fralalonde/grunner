package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.JobStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends CrudRepository<JobDTO, UUID> {
    List<JobDTO> findByOwner(String owner);
    List<JobDTO> findByOwnerAndStatus(String owner, JobStatus status);
}
