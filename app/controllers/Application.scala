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
println(url);
				Async {
					WS.url(url).get().map { response =>
						Ok(response.body);
					}
				}
			case None =>
				Redirect("/").flashing(
					"error" -> "Failure login"
				);
		}
	}
}

