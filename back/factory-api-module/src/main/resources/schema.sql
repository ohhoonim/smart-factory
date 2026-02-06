-- =============== modulith
CREATE TABLE IF NOT EXISTS event_publication
(
  id                     UUID NOT NULL,
  listener_id            TEXT NOT NULL,
  event_type             TEXT NOT NULL,
  serialized_event       TEXT NOT NULL,
  publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date        TIMESTAMP WITH TIME ZONE,
  status                 TEXT,
  completion_attempts    INT,
  last_resubmission_date TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);


-- =============== 첨부파일
CREATE TABLE if not exists SYSTEM_ATTACH_FILE (
    id VARCHAR(26) PRIMARY KEY, 
    is_linked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(100),
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_by VARCHAR(100)
);

CREATE TABLE if not exists SYSTEM_FILE_ITEM (
    id VARCHAR(26) PRIMARY KEY,
    attach_file_id VARCHAR(26) REFERENCES SYSTEM_ATTACH_FILE(id),
    origin_name TEXT NOT NULL,
    uploaded_path TEXT NOT NULL,
    capacity BIGINT NOT NULL,
    extension VARCHAR(10),
    is_removed BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_system_attach_file FOREIGN KEY (attach_file_id) REFERENCES SYSTEM_ATTACH_FILE(id) ON DELETE CASCADE
);

CREATE INDEX if not exists idx_file_item_attach_file_id ON SYSTEM_FILE_ITEM(attach_file_id);



-- =============== <some>