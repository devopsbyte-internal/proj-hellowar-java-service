-- HelloWar telemetry schema (PostgreSQL)
-- Safe to apply multiple times.

CREATE TABLE IF NOT EXISTS version_hit (
  id BIGSERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version INT NOT NULL,
  app_version VARCHAR(128) DEFAULT '',
  release_number INT NOT NULL,
  request_id VARCHAR(64) DEFAULT '',
  user_agent TEXT DEFAULT ''
);

-- Legacy table used by /hello logging (kept for backward compatibility).
CREATE TABLE IF NOT EXISTS request_log (
  id BIGSERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  path VARCHAR(255),
  remote_addr VARCHAR(64),
  app_env VARCHAR(64),
  message TEXT
);
