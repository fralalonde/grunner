package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.JobSummary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobMapper {

    JobSummary jobDTOToJobSummary(JobDTO car);

}
