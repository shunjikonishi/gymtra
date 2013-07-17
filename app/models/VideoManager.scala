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
		
	}
}
