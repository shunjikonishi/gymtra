package models;

import java.util.Date;
import jp.co.flect.play2.utils.DatabaseUtility;
import anorm._;

object DatabaseManager {
	
	private val SELECT_STATEMENT = """
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

	private def rowToVideo(row: Row) = {
		val id = row[Int]("id");
		val facebookId = row[Long] ("facebook_id");
		val status = VideoStatus.fromCode(row[Int]("status")).get;
		val title = row[String]("title");
		val publishScope = PublishScope.fromCode(row[Int]("publish_scope")).get;
		val videoKind = VideoKind.fromCode(row[Int]("video_kind")).get;
		val gameKind = GameKind.fromCode(row[Int]("gameKind")).get;
		val videoDate = row[Option[Date]]("video_date");
		val description = row[Option[String]]("description");
		val s3filename = row[String]("s3_filename");
		val youtubeId = row[Option[String]]("youtube_id");
		
		VideoInfo(
			id,
			facebookId,
			status,
			title,
			publishScope,
			videoKind,
			gameKind,
			videoDate,
			description,
			s3filename,
			youtubeId
		);
	}
	
	def apply() = new DatabaseManager("default");
}

class DatabaseManager(val databaseName: String) extends DatabaseUtility {
	
	import DatabaseManager._;
	
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
		val video = SQL(SELECT_STATEMENT + " WHERE S3_FILENAME = {key}")
			.on("key" -> key)
			.apply().map(rowToVideo(_)).head;
		SQL("""
			UPDATE UPLOADED_VIDEOS
			   SET STATUS = {status},
			       UPDATE_DATE = {update_date}
			 WHERE ID = {id}
			"""
			).on(
				"id" -> video.id,
				"status" -> VideoStatus.Upload.code,
				"update_date" -> new java.sql.Timestamp(System.currentTimeMillis)
			).executeUpdate;
		video.copy(status=VideoStatus.Upload);
	}
}
