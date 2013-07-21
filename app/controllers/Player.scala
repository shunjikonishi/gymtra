package controllers

import Application.filterAction;
import play.api.mvc.Controller;

object Player extends Controller {
	
	def player(id: Int) = filterAction { case (user, req) => implicit val request = req;
		val subId = request.getQueryString("subid").getOrElse("0").toInt;
		Ok("OK");
	}
}
