package models;

import play.api.cache.Cache;
import play.api.mvc.Request;
import play.api.mvc.AnyContent;
import play.api.Play.current;

import jp.co.flect.play2.utils.Params;
import facebook4j.FacebookFactory;
import facebook4j.Facebook;
import facebook4j.auth.AccessToken;

case class FacebookUser(accessToken: String, name: String);

object FacebookManager {
	
	val APPID = sys.env("FACEBOOK_APPID");
	val APPSECRET = sys.env("FACEBOOK_APPSECRET");
	val PERMISSIONS = "email,read_friendlists";
	
	def apply(request: Request[AnyContent]) = new FacebookManager(APPID, APPSECRET, PERMISSIONS, request);
	
}

class FacebookManager(appId: String, appSecret: String, permissions: String, request: Request[AnyContent]) {
	
	lazy val sessionId = Params(request).sessionId;
	private def getFacebook(accessToken: String): Facebook = {
		val ret = new FacebookFactory().getInstance();
		ret.setOAuthAppId(appId, appSecret);
		ret.setOAuthPermissions(permissions);
		ret.setOAuthAccessToken(new AccessToken(accessToken, null));
		ret;
	}
	
	def cacheKey = sessionId + "-User";
	
	def login(accessToken: String, expiration: Int) = {
		val facebook = getFacebook(accessToken);
		val user = FacebookUser(accessToken, facebook.getMe.getName);
		Cache.set(cacheKey, user, expiration);
		user;
	}

	lazy val user = Cache.getAs[FacebookUser](cacheKey);
}