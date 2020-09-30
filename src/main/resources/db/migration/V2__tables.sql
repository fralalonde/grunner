CREATE TABLE JOBS (
  -- TODO ? UUID string is 36 chars long - use binary representation
  id            VARCHAR(36) NOT NULL PRIMARY KEY,
  -- TODO ? use INT type and foreign key to const table of possible statuses
  status        VARCHAR(10) NOT NULL,
  submit_date   TIMESTAMP NOT NULL,
  script        VARCHAR(4096) NOT NULL,
  owner         VARCHAR(32) NOT NULL,
);