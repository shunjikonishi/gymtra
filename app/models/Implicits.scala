package models;

import play.api.libs.json.Json;

object Implicits {
	
	implicit val publishScopeFormat = PublishScope.format;
	implicit val videoKindFormat = VideoKind.format;
	implicit val gameKindFormat = GameKind.format;
	
	implicit val uploadInfoFormat = Json.format[UploadInfo];
}