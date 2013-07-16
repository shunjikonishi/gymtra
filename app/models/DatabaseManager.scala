package models;

import jp.co.flect.play2.utils.DatabaseUtility;
import anorm._;

object DatabaseManager {
	
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
			"facebook_id" -> user.id,
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
}
