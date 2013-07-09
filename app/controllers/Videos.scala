package controllers

import java.net.URLEncoder;
import java.io.File;
import scala.concurrent.ExecutionContext.Implicits.global;
import play.api.mvc.Controller;
import play.api.mvc.Action;
import play.api.mvc.RequestHeader;
import play.api.libs.ws.WS;
import play.api.libs.json.Json;

import models.AWSManager;
import models.AWSManager.uploadInfoFormat;

import jp.co.flect.play2.utils.Params;

object Videos extends Controller {
	
	val man = AWSManager.apply("fullin-fullout");

	private def redirectUri(implicit request: RequestHeader) = {
		"http://" + request.host + "/main";
	}

	def prepareUpload = Action { implicit request =>
		val filename = Params(request).get("filename");
		filename match {
			case Some(s) =>
println("filename: " + s + ", " + new File(s).getName);
				Ok(Json.toJson(man.prepare(new File(s).getName, redirectUri)));
			case None =>
				BadRequest;
		}
	}
}
