// Comment to get more information during initialization
logLevel := Level.Warn

resolvers ++= Seq(
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.        ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.mavenLocal
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.1")

addSbtPlugin("com.typesafe.sbt" %% "sbt-js-engine" % "1.0.0-SNAPSHOT")

libraryDependencies += "org.webjars" % "mocha" % "1.17.1-SNAPSHOT"
