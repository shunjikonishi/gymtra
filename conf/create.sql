DROP TABLE UPLOADED_VIDEOS;
CREATE TABLE UPLOADED_VIDEOS(
  ID                SERIAL PRIMARY KEY,
  FACEBOOK_ID       BIGINT NOT NULL,
  PUBLISH_SCOPE     INT NOT NULL,
  TITLE             VARCHAR(256) NOT NULL,
  STATUS            INT NOT NULL,
  ORIGINAL_FILENAME VARCHAR(256) NOT NULL,
  S3_FILENAME       VARCHAR(64) NOT NULL,
  YOUTUBE_ID        VARCHAR(256),
  VIDEO_KIND        INT,
  GAME_KIND         INT,
  VIDEO_DATE        DATE,
  DESCRIPTION       TEXT,
  INSERT_DATE       TIMESTAMP,
  UPDATE_DATE       TIMESTAMP
);
