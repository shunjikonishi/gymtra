package models;

import play.api.data.format.Formatter;
import play.api.data.FormError;
import java.util.Date;

trait EnumObject[T <:EnumClass] {
	
	val values: Array[T];
	
	def fromCode(code: Int): Option[T] = values.filter(_.code == code).headOption;
	
	val format = new Formatter[T] {
		override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = 
			data.get(key)
				.flatMap(v => fromCode(v.toInt))
				.toRight(Seq(FormError(key, "invalidFormat: class=" + getClass.getSimpleName, Nil)));
		
		override def unbind(key: String, value: T) = Map(key -> value.code.toString);
	}
}

trait EnumClass {
	val code: Int;
	
}

object PublishScope extends EnumObject[PublishScope] {
	
	case object Private extends PublishScope(1, "private");
	case object Friends extends PublishScope(11, "friends");
	case object Public extends PublishScope(21, "public");
	
	val values: Array[PublishScope] = Array(
		Private,
		Friends,
		Public
	);
}

sealed abstract class PublishScope(val code: Int, val name: String) extends EnumClass;

object VideoKind extends EnumObject[VideoKind] {
	
	case object FloorM extends VideoKind(1, "Floor");
	case object PommelHorse extends VideoKind(2, "Pommel Horse");
	case object Rings extends VideoKind(3, "Rings");
	case object VaultM extends VideoKind(4, "Valut");
	case object HorizontalBars extends VideoKind(5, "Horizontal Bars");
	case object HighBar extends VideoKind(6, "High Bar");
	
	case object VaultW extends VideoKind(11, "Vault");
	case object UnevenBars extends VideoKind(12, "Uneven Bars");
	case object Beam extends VideoKind(13, "Beam");
	case object FloorW extends VideoKind(14, "Floor");
	
	case object Trampoline extends VideoKind(21, "Trampoline");
	case object Tumbling extends VideoKind(31, "Tumbling");
	case object DoubleMini extends VideoKind(41, "Double Mini");
	
	case object Other extends VideoKind(101, "Other");
	
	val values: Array[VideoKind] = Array(
		FloorM,
		PommelHorse,
		Rings,
		VaultM,
		HorizontalBars,
		HighBar,
		VaultW,
		UnevenBars,
		Beam,
		FloorW,
		Trampoline,
		Tumbling,
		DoubleMini,
		Other
	);
}

sealed abstract class VideoKind(val code: Int, val name: String) extends EnumClass;

object GameKind extends EnumObject[GameKind] {
	
	case object Practice extends GameKind(1);
	case object Game extends GameKind(2);
	
	val values: Array[GameKind] = Array(
		Practice,
		Game
	);
}

sealed abstract class GameKind(val code: Int) extends EnumClass;

object VideoStatus extends EnumObject[VideoStatus] {
	
	case object Start extends VideoStatus(1);
	case object Upload extends VideoStatus(2);
	case object Ready extends VideoStatus(3);
	
	val values: Array[VideoStatus] = Array(
		Start,
		Upload,
		Ready
	);
}

sealed abstract class VideoStatus(val code: Int) extends EnumClass;

case class VideoInfo(
	id: Int,
	facebookId: Int,
	status: VideoStatus,
	title: String,
	publishScope: PublishScope,
	videoKind: VideoKind,
	gameKind: GameKind,
	videoDate: Option[Date],
	description: Option[String],
	s3filename: String,
	youtubeId: Option[String]
);

case class PrepareInfo(
	title: String,
	publishScope: PublishScope,
	videoKind: VideoKind,
	gameKind: GameKind,
	videoDate: Option[Date],
	description: Option[String],
	filename: String
);
