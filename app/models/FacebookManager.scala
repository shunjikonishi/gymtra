package models;

import play.api.cache.Cache;
import play.api.mvc.Request;
import play.api.mvc.AnyContent;
import play.api.Play.current;

import jp.co.flect.play2.utils.Params;
import facebook4j.FacebookFactory;
import facebook4j.Facebook;
import facebook4j.auth.AccessToken;

case class UserKey(accessToken: String, id: String, name: String);

class FacebookUser(key: UserKey, facebook: Facebook) {
	def name = key.name;
	def id = key.id;
	
}

object FacebookManager {
	
	val APPID = sys.env("FACEBOOK_APPID");
	val APPSECRET = sys.env("FACEBOOK_APPSECRET");
	val PERMISSIONS = "email,read_friendlists";
	
	private def cacheKey(request: Request[AnyContent]) = {
		val sessionId = Params(request).sessionId;
		sessionId + "-User";
	}
	
	def apply(request: Request[AnyContent]) = new FacebookManager(APPID, APPSECRET, PERMISSIONS, request);
	
}

class FacebookManager(appId: String, appSecret: String, permissions: String, request: Request[AnyContent]) {
	
	lazy val sessionId = Params(request).sessionId;
	private def cacheKey = {
		val ret = sessionId + "-User";
		println("CacheKey: " + ret);
		ret;
	}
	
	private def getFacebook(accessToken: String): Facebook = {
		val ret = new FacebookFactory().getInstance();
		ret.setOAuthAppId(appId, appSecret);
		ret.setOAuthPermissions(permissions);
		ret.setOAuthAccessToken(new AccessToken(accessToken, null));
		ret;
	}
	
	def getUser = {
		val ret = Cache.getAs[UserKey](cacheKey);
println("getUser: " + ret);
println("getUser2: " + Cache.get(cacheKey));
println("getUser3: " + Cache.get(sessionId + "-Test"));
		ret.map(key =>
				new FacebookUser(key, getFacebook(key.accessToken))
			);
	}
	
	def login(accessToken: String, expiration: Int) = {
println("login: " + expiration);
		val facebook = getFacebook(accessToken);
		val me = facebook.getMe;
		val key = UserKey(accessToken, me.getId, me.getName);
		Cache.set(cacheKey, key, expiration);
		new FacebookUser(key, facebook);
	}

}