package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
	
	private val FACEBOOK_APPID = sys.env.get("FACEBOOK_APPID");
	private val FACEBOOK_APPSECRET = sys.env.get("FACEBOOK_APPSECRET");
	
	def index = Action {
		println(FACEBOOK_APPID + ", " +  FACEBOOK_APPSECRET);
		(FACEBOOK_APPID, FACEBOOK_APPSECRET) match {
			case (Some(id), Some(secret)) => Ok(views.html.index(id));
			case _ => Ok("Setup FACEBOOK_APPID and FACEBOOK_APPSECRET");
		}
	}
}