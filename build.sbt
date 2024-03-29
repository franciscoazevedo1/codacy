name := "codacy"
 
version := "1.0" 
      
lazy val `codacy` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies ++= Seq( "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % "test")