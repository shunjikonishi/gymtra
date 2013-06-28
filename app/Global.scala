import play.api.Application;
import play.api.GlobalSettings;

import models.FacebookManager;

object Global extends GlobalSettings {
	
	override def onStart(app: Application) {
		FacebookManager.setup;
	}
	
}