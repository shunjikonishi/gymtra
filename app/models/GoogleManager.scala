package models;

import java.io.File;
import java.net.URL;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.XmlBlob;
import com.google.gdata.util.ResourceNotFoundException;

object GoogleManager {
	
	private[this] val USERNAME     = sys.env("GOOGLE_USERNAME");
	private[this] val PASSWORD     = sys.env("GOOGLE_PASSWORD");
	private[this] val APIKEY       = sys.env("GOOGLE_APIKEY");
	private[this] val DEVELOPERKEY = sys.env("GOOGLE_DEVELOPERKEY");
	
	def apply() = {
		val service = new YouTubeService(APIKEY, DEVELOPERKEY);
		service.setUserCredentials(USERNAME, PASSWORD);
		new GoogleManager(service);
	}
	
	def contentType(filename: String) = {
		filename.substring(filename.lastIndexOf(".") + 1) match {
			case "mpeg" | "mpg" => "video/mpeg";
			case "mov" => "video/quicktime";
			case "avi" => "video/x-msvideo";
			case "swf" => "application/x-shockwave-flash";
			case "wmv" => "video/x-ms-wmv";
			case _ => "video/mpeg";
		}
	}
}

class GoogleManager(service: YouTubeService) {
	
	import GoogleManager._;
	
	val APP_NAME = "fullin-fullout";
	
	def upload(video: VideoInfo, file: File) = {
		val newEntry = new VideoEntry();

		val mg = newEntry.getOrCreateMediaGroup();
		mg.setTitle(new MediaTitle());
		mg.getTitle().setPlainTextContent(video.title);
		mg.addCategory(new MediaCategory(
		YouTubeNamespace.CATEGORY_SCHEME, "Sports"));
		mg.setKeywords(new MediaKeywords());
		mg.getKeywords().addKeyword("gymnastics");
		mg.getKeywords().addKeyword(video.videoKind.name);
		video.description.foreach { s =>
			mg.setDescription(new MediaDescription());
			mg.getDescription().setPlainTextContent(s);
		}
		mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, video.facebookId.toString));
		mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, APP_NAME));

		val ms = new MediaFileSource(file, contentType(video.s3filename));
		newEntry.setMediaSource(ms);

		val uploadUrl =
		  "http://uploads.gdata.youtube.com/feeds/api/users/default/uploads";

		val createdEntry = service.insert(new URL(uploadUrl), newEntry);
		val xmlBlob = new XmlBlob();
		xmlBlob.setBlob("<yt:accessControl action='list' permission='denied'/>");
		createdEntry.setXmlBlob(xmlBlob);
		createdEntry.update();
		video.copy(youtubeId = Some(createdEntry.getMediaGroup.getVideoId));
	}
	
	def getVideoEntry(video: VideoInfo) = {
		video.youtubeId match {
			case Some(s) =>
				try {
					Some(service.getEntry(new URL("http://gdata.youtube.com/feeds/api/users/default/uploads/" + s), classOf[VideoEntry]));
				} catch {
					case e: ResourceNotFoundException =>
						None;
				}
			case None =>
				None;
		}
	}
	
	def delete(video: VideoInfo) = {
		getVideoEntry(video).foreach(_.delete());
	}
}
