CREATE TABLE GRUNNER.BATCH (
  -- TODO ? UUID string is 36 chars long - use binary representation
  id            VARCHAR(36) NOT NULL PRIMARY KEY,
  script        VARCHAR(4096) NOT NULL,
  owner         VARCHAR(32) NOT NULL,
);

CREATE TABLE GRUNNER.BATCH_EVENT (
  batch_id       VARCHAR(36) NOT NULL,
  -- TODO ? use INT type and foreign key to const table of possible statuses
  new_status   VARCHAR(10) NOT NULL,
  event_time   TIMESTAMP NOT NULL,
  results       VARCHAR(4096),

  CONSTRAINT fk_batch_id FOREIGN KEY (batch_id) REFERENCES GRUNNER.BATCH
);