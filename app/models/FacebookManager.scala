package models;

import play.api.cache.Cache;
import play.api.mvc.Request;
import play.api.mvc.AnyContent;
import play.api.Play.current;
import play.api.libs.json.Json;
import play.api.libs.json.JsSuccess;

import jp.co.flect.play2.utils.Params;
import facebook4j.FacebookFactory;
import facebook4j.Facebook;
import facebook4j.auth.AccessToken;

case class UserKey(accessToken: String, id: String, name: String) extends java.io.Serializable

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
	
	implicit val UserKeyFormat = Json.format[UserKey];
}

class FacebookManager(appId: String, appSecret: String, permissions: String, request: Request[AnyContent]) {
	
	import FacebookManager._;
	
	lazy val sessionId = Params(request).sessionId;
	private def cacheKey = sessionId + "-User";
	
	
	private def getFacebook(accessToken: String): Facebook = {
		val ret = new FacebookFactory().getInstance();
		ret.setOAuthAppId(appId, appSecret);
		ret.setOAuthPermissions(permissions);
		ret.setOAuthAccessToken(new AccessToken(accessToken, null));
		ret;
	}
	
	def getUser = {
		val ret = Cache.getAs[String](cacheKey);
		ret.flatMap{json =>
			Json.fromJson[UserKey](Json.parse(json)) match {
				case JsSuccess(key, path) =>
					Some(new FacebookUser(key, getFacebook(key.accessToken)))
				case _ =>
					None;
			}
		};
	}
	
	def login(accessToken: String, expiration: Int) = {
		val facebook = getFacebook(accessToken);
		val me = facebook.getMe;
		val key = UserKey(accessToken, me.getId, me.getName);
		val str = Json.toJson(key).toString;
		Cache.set(cacheKey, str);
		new FacebookUser(key, facebook);
	}

}