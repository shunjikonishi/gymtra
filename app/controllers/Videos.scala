package controllers

import java.net.URLEncoder;
import java.io.File;
import scala.concurrent.ExecutionContext.Implicits.global;
import play.api.mvc.Controller;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.api.mvc.Result;
import play.api.mvc.Request;
import play.api.mvc.RequestHeader;
import play.api.Logger;
import play.api.libs.ws.WS;
import play.api.libs.json.Json;

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
import models.AWSManager;
import models.DatabaseManager;
import models.PublishScope;
import models.VideoKind;
import models.GameKind;
import models.PrepareInfo;
import models.Implicits._;

import jp.co.flect.play2.utils.Params;

object Videos extends Controller {
	
	val log = Logger(getClass);
	val s3man = AWSManager("fullin-fullout");
	val dbman = DatabaseManager();
	
	private def filterAction(f: (FacebookUser, Request[AnyContent]) => Result): Action[AnyContent] = Action { request =>
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
			val prepareInfo = formData.get;
			val uploadInfo = s3man.prepare(new File(prepareInfo.filename).getName, redirectUri);
			dbman.start(user, prepareInfo, uploadInfo);
			Ok(Json.toJson(uploadInfo));
		}
	}
	
	def s3uploaded = Action { implicit request =>
		Ok(request.path);
	}
}
