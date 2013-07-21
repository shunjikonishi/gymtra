package models;

import java.util.Date;
import java.sql.Connection;
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
		val gameKind = GameKind.fromCode(row[Int]("game_kind")).get;
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

	def download(key: String) = withTransaction { implicit con =>
		println("download: " + key);
		val video = SQL(SELECT_STATEMENT + " WHERE S3_FILENAME = {key}")
			.on("key" -> key)
			.apply().map(rowToVideo(_)).head;
		val status = VideoStatus.Download;
		updateStatus(con, video.id, status);
		video.copy(status=status);
	}
	
	def upload(video: VideoInfo) = withTransaction { implicit con =>
		val status = VideoStatus.Upload;
		updateStatus(con, video.id, status);
		video.copy(status=status);
	}
	
	def delete(video: VideoInfo) = withTransaction { implicit con =>
		val status = VideoStatus.Deleted;
		updateStatus(con, video.id, status);
		video.copy(status=status);
	}
	
	def finish(video: VideoInfo) = withTransaction { implicit con =>
		val status = VideoStatus.Ready;
		SQL("""
			UPDATE UPLOADED_VIDEOS
			   SET STATUS = {status},
			       YOUTUBE_ID = {youtube_id},
			       UPDATE_DATE = {update_date}
			 WHERE ID = {id}
			"""
			).on(
				"id" -> video.id,
				"status" -> status.code,
				"youtube_id" -> video.youtubeId,
				"update_date" -> new java.sql.Timestamp(System.currentTimeMillis)
			).executeUpdate()(con);
		video.copy(status=status);
	}
	
	private def updateStatus(con: Connection, id: Int, status: VideoStatus) = {
		SQL("""
			UPDATE UPLOADED_VIDEOS
			   SET STATUS = {status},
			       UPDATE_DATE = {update_date}
			 WHERE ID = {id}
			"""
			).on(
				"id" -> id,
				"status" -> status.code,
				"update_date" -> new java.sql.Timestamp(System.currentTimeMillis)
			).executeUpdate()(con);
	}
	
	def getVideoCount(facebookId: Long) = withConnection { implicit con =>
		SQL("SELECT COUNT(*) AS CNT FROM UPLOADED_VIDEOS WHERE FACEBOOK_ID = {facebookId} AND STATUS <> {status}")
			.on(
				"facebookId" -> facebookId,
				"status" -> VideoStatus.Deleted.code
			)
			.apply().map(row => row[Long]("CNT").toInt)
			.head;
	}
	
	def getVideoList(facebookId: Long, offset: Int = 0, limit: Int = 10) = withConnection { implicit con =>
		SQL(SELECT_STATEMENT + " WHERE FACEBOOK_ID = {facebookId} AND STATUS <> {status}" +
				"ORDER BY UPDATE_DATE DESC LIMIT {limit} OFFSET {offset}"
			).on(
				"facebookId" -> facebookId,
				"status" -> VideoStatus.Deleted.code,
				"offset" -> offset,
				"limit" -> limit
			)
			.apply().map(rowToVideo(_)).toList;
	}
	
	def getVideo(id: Int) = withConnection { implicit con =>
		SQL(SELECT_STATEMENT + " WHERE ID = {id}")
			.on("id" -> id)
			.apply().map(rowToVideo(_)).headOption;
	}
}
