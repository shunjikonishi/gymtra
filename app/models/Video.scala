package models;

object VideoKind {
	
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
	
}

sealed abstract class VideoKind(code: Int, name: String) 

object GameKind {
	
	case object Game extends GameKind(1);
	case object Practice extends GameKind(2);
}

sealed abstract class GameKind(code: Int)

