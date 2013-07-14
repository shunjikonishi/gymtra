import play.api.Application;
import play.api.mvc.WithFilters;
import play.api.db.DB;
import play.api.Play.current;

import jp.co.flect.play2.filters.SessionIdFilter;
import jp.co.flect.util.ResourceGen;
import jp.co.flect.rdb.RunScript;
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
		val mode = sys.props.get("gymtra.mode").getOrElse("web");
		mode match {
			case "setup" =>
				val file = new File("conf/create.sql");
				DB.withTransaction { con =>
					val script = new RunScript(con);
					script.setIgnoreDdlError(true);
					script.run(file);
				}
				System.exit(0);
			case _ =>
		}
	}
	
}