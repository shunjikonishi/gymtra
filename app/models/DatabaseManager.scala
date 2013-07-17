package models;

import jp.co.flect.play2.utils.DatabaseUtility;
import anorm._;

object DatabaseManager {
	
	val SELECT_STATEMENT = """
		SELECT ID,
		       FACEBOOK_ID,
		       PUBLISH_SCOPE,
		       TITLE,
		       STATUS,
		       ORIGINAL_FILENAME,
		       S3_FILENAME,
		       YOUTUBE_ID,
		       VIDEO_KIND,
		       GAME_KIND,
		       VIDEO_DATE,
		       DESCRIPTION
		  FROM UPLOADED_VIDEOS
	""";

	def apply() = new DatabaseManager("default");
}

class DatabaseManager(val databaseName: String) extends DatabaseUtility {
	
	def start(user: FacebookUser, prepareInfo: PrepareInfo, uploadInfo: UploadInfo): Long = withTransaction { implicit con =>
		val now = new java.sql.Timestamp(System.currentTimeMillis);
		SQL("""
			INSERT INTO UPLOADED_VIDEOS (
			       FACEBOOK_ID,
			       PUBLISH_SCOPE,
			       TITLE,
			       STATUS,
			       ORIGINAL_FILENAME,
			       S3_FILENAME,
			       VIDEO_KIND,
			       GAME_KIND,
			       VIDEO_DATE,
			       DESCRIPTION,
			       INSERT_DATE,
			       UPDATE_DATE
			) VALUES (
			       {facebook_id},
			       {publish_scope},
			       {title},
			       {status},
			       {original_filename},
			       {s3_filename},
			       {video_kind},
			       {game_kind},
			       {video_date},
			       {description},
			       {insert_date},
			       {update_date})
			"""
		).on(
			"facebook_id" -> user.id.toLong,
			"publish_scope" -> prepareInfo.publishScope.code, 
			"title" -> prepareInfo.title, 
			"status" -> VideoStatus.Start.code, 
			"original_filename" -> prepareInfo.filename,
			"s3_filename" -> uploadInfo.key,
			"video_kind" -> prepareInfo.videoKind.code,
			"game_kind" -> prepareInfo.gameKind.code,
			"video_date" -> prepareInfo.videoDate,
			"description" -> prepareInfo.description,
			"insert_date" -> now,
			"update_date" -> now
		).executeInsert().getOrElse(0);
	}

	def upload(key: String) = withTransaction { implicit con =>
		val id = SQL("SELECT ID FROM UPLOADED_VIDEOS WHERE S3_FILENAME = {key}")
			.on("key" -> key)
			.apply().map(row => row[Long]("ID")).head;
		SQL("""
			UPDATE UPLOADED_VIDEOS
			   SET STATUS = {status},
			       UPDATE_DATE = {update_date}
			 WHERE S3_FILENAME = {key}
			"""
			).on(
				"key" -> key,
				"status" -> VideoStatus.Upload.code,
				"update_date" -> new java.sql.Timestamp(System.currentTimeMillis)
			).executeUpdate;
		id;
	}
}
