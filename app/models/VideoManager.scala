package models;

import java.io.File;

object VideoManager {

	val s3man = AWSManager("fullin-fullout");
	val dbman = DatabaseManager();
	val gman = GoogleManager();

	def apply(user: FacebookUser) = new VideoManager(s3man, dbman, gman, user);
}

class VideoManager(s3man: AWSManager, dbman: DatabaseManager, gman: GoogleManager, user: FacebookUser) {

	def start(prepareInfo: PrepareInfo, redirectUri: String) = {
		val uploadInfo = s3man.prepare(new File(prepareInfo.filename).getName, redirectUri);
		dbman.start(user, prepareInfo, uploadInfo);
		uploadInfo;
	}

	def uploadYoutube(key: String) = {
		val video = dbman.download(key.substring("videos/".length));
		val file = s3man.download(key);
		
		dbman.upload(video);
		
		val newVideo = gman.upload(video, file);
		dbman.finish(newVideo);
		
		file.delete();
	}
	
	def getMyVideoList(offset: Int, size: Int) = {
		val count = dbman.getVideoCount(user.id);
		val list = dbman.getVideoList(user.id, offset, size);
		VideoList(count, offset, size, list);
	}
	
	def delete(id: Int) = {
		dbman.getVideo(id) match {
			case Some(video) if (video.facebookId == user.id) =>
				s3man.delete("videos/" + video.s3filename);
				gman.delete(video);
				dbman.delete(video);
				true;
			case _ => false;
		}
	}
}
