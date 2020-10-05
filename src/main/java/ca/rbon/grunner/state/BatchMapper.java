package ca.rbon.grunner.state;

import ca.rbon.grunner.api.model.BatchResult;
import ca.rbon.grunner.api.model.BatchStatus;
import ca.rbon.grunner.api.model.BatchStatusUpdate;
import ca.rbon.grunner.db.enums.BatchEventStatus;
import ca.rbon.grunner.db.tables.records.BatchEventRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This interface is implemented by MapStruct as {@link BatchMapperImpl}
 */
@Mapper(componentModel = "spring")
public interface BatchMapper {

  BatchEventStatus fromAPIStatus(BatchStatus status);

  BatchStatus fromDBStatus(BatchEventStatus status);

  @Mapping(source = "eventTime", target = "timestamp")
  BatchStatusUpdate fromDBEvent(BatchEventRecord status);

  @Mapping(source = "eventTime", target = "timestamp")
  BatchResult fromDBResult(BatchEventRecord status);

}
