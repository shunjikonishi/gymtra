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

import models.VideoManager;
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
				FacebookManager(request).login(code, redirectUri) match {
					case Left(e) => Redirect("/").flashing("error" -> e);
					case Right(user) => Redirect("/main");
				}
			case None =>
				Redirect("/").flashing(
					"error" -> "Failure login"
				);
		}
	}

	def main = filterAction { case(user, req) => implicit val request = req;
		val offset = request.getQueryString("offset").getOrElse("0").toInt;
		val size = request.getQueryString("size").getOrElse("10").toInt;
		val list = VideoManager(user).getMyVideoList(offset, size);
		Ok(views.html.main(user, list));
	}

	def upload = filterAction { case (user, req) => implicit val request = req;
		val url = "http://" + request.host + "/videos/s3uploaded";
		Ok(views.html.upload(user, url))
	}
}

