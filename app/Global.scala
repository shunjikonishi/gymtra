import play.api.Application;
import play.api.mvc.WithFilters;

import jp.co.flect.play2.filters.SessionIdFilter;
import jp.co.flect.util.ResourceGen;
import java.io.File;

import models.FacebookManager;

object Global extends WithFilters(SessionIdFilter) {
	
	override def onStart(app: Application) {
		//Generate messages and messages.ja
		val defaults = new File("conf/messages");
		val origin = new File("conf/messages.origin");
		if (origin.lastModified > defaults.lastModified) {
			val gen = new ResourceGen(defaults.getParentFile(), "messages");
			gen.process(origin);
		}
	}
	
}