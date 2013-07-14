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
    "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2",
    "postgresql" % "postgresql" % "9.2-1002.jdbc4",
    "org.facebook4j" % "facebook4j-core" % "1.1.9"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" 
  )

}
