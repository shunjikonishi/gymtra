package models;

import play.api.cache.Cache;
import play.api.mvc.Request;
import play.api.mvc.AnyContent;
import play.api.Play.current;

import jp.co.flect.play2.utils.Params;

case class FacebookUser(accessToken: String, expiration: Int);

object FacebookManager {
	
	val APPID = sys.env("FACEBOOK_APPID");
	val APPSECRET = sys.env("FACEBOOK_APPSECRET");
	
	private val PERMISSIONS = "email,read_friendlists";
	
	def apply(request: Request[AnyContent]) = new FacebookManager(APPID, APPSECRET, request);
	
}

class FacebookManager(appId: String, appSecret: String, request: Request[AnyContent]) {
	
	lazy val sessionId = Params(request).sessionId;
	
	def cacheKey = sessionId + "-User";
	
	def login(accessToken: String, expiration: Int) = {
		val user = FacebookUser(accessToken, expiration);
		Cache.set(cacheKey, user, expiration);
		user;
	}
}