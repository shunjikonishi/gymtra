package controllers

import java.net.URLEncoder;

import play.api.Logger;
import play.api.Play.current;
import play.api.libs.concurrent.Akka;
import play.api.libs.concurrent.Execution.Implicits.defaultContext;
import play.api.libs.json.Json;
import play.api.libs.ws.WS;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.api.mvc.Controller;
import play.api.mvc.Request;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import scala.concurrent.duration.DurationInt;

import play.api.data.Form;
import play.api.data.Forms.of;
import play.api.data.Forms.mapping;
import play.api.data.Forms.text;
import play.api.data.Forms.date;
import play.api.data.Forms.nonEmptyText;
import play.api.data.Forms.number;
import play.api.data.Forms.optional;

import models.FacebookUser;
import models.FacebookManager;
import models.VideoManager;
import models.PublishScope;
import models.VideoKind;
import models.GameKind;
import models.PrepareInfo;
import models.Implicits._;

import jp.co.flect.play2.utils.Params;

object Videos extends Controller {
	
	val log = Logger(getClass);
	
	def filterAction(f: (FacebookUser, Request[AnyContent]) => Result): Action[AnyContent] = Action { request =>
		val man = FacebookManager(request);
		man.getUser match {
			case Some(user) => f(user, request);
			case None => Forbidden;
		}
	}
	
	
	private def redirectUri(implicit request: RequestHeader) = {
		"http://" + request.host + "/videos/s3uploaded";
	}
	
	private val prepareForm = Form(mapping(
		"title" -> nonEmptyText(maxLength=256),
		"publishScope" -> of[PublishScope],
		"videoKind" -> of[VideoKind],
		"gameKind" -> of[GameKind],
		"videoDate" -> optional(date("yyyy-MM-dd")),
		"description" -> optional(text),
		"filename" -> text
	)(PrepareInfo.apply)(PrepareInfo.unapply));
	
	def prepareUpload = filterAction { case (user, req) => implicit val request = req;
		val formData = prepareForm.bindFromRequest;
		if (formData.hasErrors) {
			println(formData.errors);
			BadRequest;
		} else {
			val man = VideoManager(user);
			val uploadInfo = man.start(formData.get, redirectUri);
			Ok(Json.toJson(uploadInfo));
		}
	}
	
	def s3uploaded = filterAction { case (user, req) => implicit val request = req;
		request.getQueryString("key") match {
			case Some(key) if key.startsWith("videos/") =>
				try {
					Akka.system.scheduler.scheduleOnce(0 seconds) {
						VideoManager(user).uploadYoutube(key);
					}
					Redirect("/main");
				} catch {
					case e: Exception =>
						e.printStackTrace;
						InternalServerError(e.toString)
				}
			case None =>
				BadRequest;
		}
	}
	
	def deleteVideo = filterAction { case (user, req) => implicit val request = req;
		Params(request).get("videoId") match {
			case Some(id) =>
				if (VideoManager(user).delete(id.toInt)) {
					Redirect("/main");
				} else {
					Ok("Can not delete video: " + id);
				}
			case None => BadRequest;
		}
	}
}
