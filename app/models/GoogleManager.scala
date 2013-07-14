package models;

import com.google.gdata.client.youtube.YouTubeService;

object GoogleManager {
	
	val USERNAME     = sys.env("GOOGLE_USERNAME");
	val PASSWORD     = sys.env("GOOGLE_PASSWORD");
	val APIKEY       = sys.env("GOOGLE_APIKEY");
	val DEVELOPERKEY = sys.env("GOOGLE_DEVELOPERKEY");
	
	def apply() {
		val service = new YouTubeService(APIKEY, DEVELOPERKEY);
		service.setUserCredentials(USERNAME, PASSWORD);
		new GoogleManager(service);
	}
}

class GoogleManager(service: YouTubeService) {
}
