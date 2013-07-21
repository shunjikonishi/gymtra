package models;

import play.api.cache.Cache;
import play.api.mvc.Request;
import play.api.mvc.AnyContent;
import play.api.Play.current;
import play.api.libs.json.Json;
import play.api.libs.json.JsSuccess;
import play.api.libs.ws.WS;
import scala.concurrent.ExecutionContext.Implicits.global;
import scala.concurrent.duration._;
import scala.concurrent.Await;

import jp.co.flect.play2.utils.Params;
import facebook4j.FacebookFactory;
import facebook4j.Facebook;
import facebook4j.auth.AccessToken;

case class UserKey(accessToken: String, id: String, name: String) 

class FacebookUser(key: UserKey, facebook: Facebook) {
	def name = key.name;
	def id = key.id.toLong;
	
}

object FacebookManager {
	
	val APPID = sys.env("FACEBOOK_APPID");
	val APPSECRET = sys.env("FACEBOOK_APPSECRET");
	val PERMISSIONS = "email,read_friendlists";
	
	val ONE_MONTH = 60 * 60 * 24 * 30;
	val ONE_WEEK  = 60 * 60 * 24 * 7;
	
	
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
		val ret = Cache.getAs[UserKey](cacheKey);
		ret.map(key => new FacebookUser(key, getFacebook(key.accessToken)));
	}
	
	def login(code: String, redirectUri: String): Either[String, FacebookUser] = {
		val url = "https://graph.facebook.com/oauth/access_token?client_id=" +
			APPID + "&redirect_uri=" +
			redirectUri +"&client_secret=" +
			APPSECRET + "&code=" +
			code;
		val future = WS.url(url).get().map { response =>
			val r = "access_token=(.+)&expires=([0-9]+)".r;
			response.body match {
				case r(accessToken, expires) =>
					Right(doLogin(accessToken, expires.toInt));
				case _ =>
					Left(response.body);
			}
		}
		Await.result(future, 10 seconds);
	}
	
	private def doLogin(accessToken: String, expiration: Int) = {
		val facebook = getFacebook(accessToken);
		val me = facebook.getMe;
		val key = UserKey(accessToken, me.getId, me.getName);
		val cacheExp = if (expiration > ONE_MONTH) ONE_WEEK else expiration;
		Cache.set(cacheKey, key, cacheExp);
		new FacebookUser(key, facebook);
	}

}