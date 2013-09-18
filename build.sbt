
organization := "fr.geocite"

name := "marius-visu"

scalaVersion := "2.10.2"


resolvers += "ISC-PIF Release" at "http://maven.iscpif.fr/public"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.5"

libraryDependencies += "fr.geocite" %% "marius" % "0.1-SNAPSHOT"

libraryDependencies += "fr.geocite" %% "simpoplocal" % "0.1-SNAPSHOT"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.0"
