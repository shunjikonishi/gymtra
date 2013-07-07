package controllers

import java.net.URLEncoder;
import scala.concurrent.ExecutionContext.Implicits.global;
import play.api.mvc.Controller;
import play.api.mvc.Action;
import play.api.mvc.RequestHeader;
import play.api.libs.ws.WS;

import models.FacebookManager;

object Application extends Controller {
	
	private def redirectUri(implicit request: RequestHeader) = {
		val url = "http://" + request.host + "/login";
		URLEncoder.encode(url, "utf-8");
	}
	
	def root = Action { implicit request =>
		FacebookManager(request).getUser match {
			case Some(x) => Redirect("/main");
			case None => Redirect("/index");
		}
	}
	
	def index = Action { implicit request =>
		Ok(views.html.index(FacebookManager.APPID, redirectUri));
	}
	
	def login = Action { implicit request =>
		request.getQueryString("code") match {
			case Some(code) =>
				val url = "https://graph.facebook.com/oauth/access_token?client_id=" +
					FacebookManager.APPID + "&redirect_uri=" +
					redirectUri +"&client_secret=" +
					FacebookManager.APPSECRET + "&code=" +
					code;
				Async {
					WS.url(url).get().map { response =>
						val r = "access_token=(.+)&expires=([0-9]+)".r;
						response.body match {
							case r(accessToken, expires) =>
								FacebookManager(request).login(accessToken, expires.toInt);
								Redirect("/main");
							case _ =>
								Redirect("/").flashing("error" -> response.body);
						}
					}
				}
			case None =>
				Redirect("/").flashing(
					"error" -> "Failure login"
				);
		}
	}

	def main = Action { implicit request =>
		val man = FacebookManager(request);
		man.getUser match {
			case Some(user) => Ok("Welcome " + user.name);
				val url = "http://" + request.host + "/main";
				Ok(views.html.main(user, url);
			case _ => Redirect("/");
		}
	}
}

