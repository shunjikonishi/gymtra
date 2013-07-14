package models;

import jp.co.flect.play2.utils.DatabaseUtility;
import anorm._;

object DatabaseManager {
	
}

class DatabaseManager(val databaseName: String) extends DatabaseUtility {
	
	def start(info: UploadInfo): Int = withTransaction { implicit con =>
		val now = new java.sql.Timestamp(System.currentTimeMillis);
		/*
		val id = SQL("""
				INSERT INTO  (
				       FACEBOOK_ID,
				       PUBLISH_SCOPE,
				       TITLE,
				       STATUS,
				       S3_FILENAME,
				       VIDEO_KIND,
				       GAME_KIND,
				       VIDEO_DATE,
				       DESCRIPTION,
				       INSERT_DATE,
				       UPDATE_DATE
				VALUES({facebook_id},
				       {publish_scope},
				       {title},
				       {status},
				       {s3_filename},
				       {video_kind},
				       {game_kind},
				       {video_date},
				       {description},
				       {insert_date},
				       {update_date})
			"""
			).on(
				"facebook_id" -> info.kind.code,
				"name" -> info.name, 
				"groupname" -> info.group, 
				"sqltext" -> info.sql, 
				"description" -> info.description,
				"setting" -> info.setting,
				"insert_date" -> now,
				"update_date" -> now
			).executeInsert();
		*/
		0;
	}
}
