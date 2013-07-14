package controllers

import java.net.URLEncoder;
import scala.concurrent.ExecutionContext.Implicits.global;
import play.api.mvc.Controller;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.api.mvc.RequestHeader;
import play.api.mvc.Request;
import play.api.mvc.Result;
import play.api.libs.ws.WS;

import models.FacebookManager;
import models.FacebookUser;

object Application extends Controller {
	
	def filterAction(f: (FacebookUser, Request[AnyContent]) => Result): Action[AnyContent] = Action { request =>
		val man = FacebookManager(request);
		man.getUser match {
			case Some(user) => f(user, request);
			case None => Redirect("/index");
		}
	}

	private def redirectUri(implicit request: RequestHeader) = {
		val url = "http://" + request.host + "/login";
		URLEncoder.encode(url, "utf-8");
	}
	
	def root = filterAction { case (user, request) =>
		Redirect("/main");
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

	def main = filterAction { case(user, req) => implicit val request = req;
		Ok(views.html.main(user));
	}

	def upload = filterAction { case (user, req) => implicit val request = req;
		val url = "http://" + request.host + "/videos/s3uploaded";
		Ok(views.html.upload(user, url))
	}
}

