CREATE TABLE GRUNNER.BATCH (
  batch_id      UUID NOT NULL PRIMARY KEY,
  script        VARCHAR(4096) NOT NULL,
  owner         VARCHAR(32) NOT NULL,
);

CREATE TABLE GRUNNER.BATCH_EVENT (
  batch_id      UUID NOT NULL,
  status        ENUM('PENDING', 'EXECUTING', 'COMPLETED', 'CANCELLED', 'FAILED') NOT NULL,
  event_time    TIMESTAMP WITH TIME ZONE NOT NULL,
  results       CLOB,

  CONSTRAINT fk_batch_event_id FOREIGN KEY (batch_id) REFERENCES GRUNNER.BATCH
);

CREATE INDEX batch_id ON GRUNNER.BATCH_EVENT(batch_id);
CREATE INDEX ix_event_time ON GRUNNER.BATCH_EVENT(event_time);
