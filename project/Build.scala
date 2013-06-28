import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "gymtra"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.facebook4j" % "facebook4j-core" % "1.1.9"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
