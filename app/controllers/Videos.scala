package controllers

import java.net.URLEncoder;
import java.io.File;
import scala.concurrent.ExecutionContext.Implicits.global;
import play.api.mvc.Controller;
import play.api.mvc.Action;
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

import models.AWSManager;
import models.AWSManager.uploadInfoFormat;
import models.PublishScope;
import models.VideoKind;
import models.GameKind;
import models.PrepareInfo;

import jp.co.flect.play2.utils.Params;

object Videos extends Controller {
	
	val log = Logger(getClass);
	
	val s3man = AWSManager.apply("fullin-fullout");
	
	private def redirectUri(implicit request: RequestHeader) = {
		"http://" + request.host + "/videos/s3uploaded";
	}
	
	implicit val publishScopeFormat = PublishScope.format;
	implicit val videoKindFormat = VideoKind.format;
	implicit val gameKindFormat = GameKind.format;
	
	private val prepareForm = Form(mapping(
		"title" -> nonEmptyText(maxLength=256),
		"publishScope" -> of[PublishScope],
		"videoKind" -> of[VideoKind],
		"gameKind" -> of[GameKind],
		"videoDate" -> optional(date("yyyy-MM-dd")),
		"description" -> optional(text),
		"filename" -> text
	)(PrepareInfo.apply)(PrepareInfo.unapply));
	
	def prepareUpload = Action { implicit request =>
		val filename = Params(request).get("filename");
		filename match {
			case Some(s) =>
				val info = s3man.prepare(new File(s).getName, redirectUri);
				log.info("S3 Upload: key=" + info.key);
				Ok(Json.toJson(info));
			case None =>
				BadRequest;
		}
	}
	
	def s3uploaded = Action { implicit request =>
		Ok(request.path);
	}
}
