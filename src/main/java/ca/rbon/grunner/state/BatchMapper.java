package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.BatchResult;
import ca.rbon.grunner.api.model.BatchStatus;
import ca.rbon.grunner.api.model.BatchStatusUpdate;
import ca.rbon.grunner.db.enums.BatchEventStatus;
import ca.rbon.grunner.db.tables.records.BatchEventRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BatchMapper {

    BatchEventStatus eventStatusFromStatus(BatchStatus status);

    BatchStatus statusFromEventStatus(BatchEventStatus status);

    BatchResult resultFromEventRecord(BatchEventRecord status);

    BatchStatusUpdate statusUpdateFromEventRecord(BatchEventRecord status);


}
