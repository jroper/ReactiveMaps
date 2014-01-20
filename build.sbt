import play.PlayTestAssetsCompiler
import WebKeys._

name := "snapapp"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-contrib" % "2.2.3",
  "com.typesafe.play.extras" %% "play-geojson" % "1.0.0",
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.0.0",
  "org.webjars" % "knockout" % "2.3.0",
  "org.webjars" % "requirejs" % "2.1.8",
  "org.webjars" % "leaflet" % "0.6.4",
  "org.webjars" % "amdefine" % "0.1.0" % "test"
)

play.Project.playScalaSettings

webSettings

includeWebJars in TestAssets += "amdefine"

resourceGenerators in Test <+= PlayTestAssetsCompiler.CoffeescriptCompiler

coffeescriptEntryPoints in Test <<= (sourceDirectory in Test)(base => base / "assets" ** "*.coffee")

val copyAllMochaSources = TaskKey[File]("copy-mocha-sources", "Copy all JavaScript to one location")

val mochaWorkingDir = SettingKey[File]("mocha-working-directory", "The Mocha working directory")

val mochaTests = TaskKey[Unit]("mocha-tests", "Run the mocha tests")

mochaWorkingDir <<= target / "mocha"

def copyJsFiles(sources: Seq[File], target: File): Seq[(File, File)] = {
  val copyDescs: Seq[(File, File)] = (for {
    source: File <- sources
  } yield {
    (source ** "*.js") x Path.rebase(source, target)
  }).flatten
  val toCopy = copyDescs.filter {
    case (s, t) => s.lastModified > t.lastModified
  }
  IO.copy(toCopy)
  copyDescs
}

copyAllMochaSources <<= (extractWebJars in Assets, extractWebJars in TestAssets, resourceManaged in Compile,
  baseDirectory, resourceManaged in Test, mochaWorkingDir, managedResources in Compile,
  managedResources in Test) map {
  (webJars, testWebJars, resources, base, testResources, workDir, _, _) =>
    copyJsFiles(Seq(webJars, testWebJars, resources, testResources), workDir)
    copyJsFiles(Seq(base / "public-test"), workDir / "public")
    workDir
  }

mochaTests <<= (copyAllMochaSources, resourceManaged in Test, managedResources in Test) map { (workDir, testDir, _) =>
  val tests = ((testDir ** "*Test.js") +++ (testDir ** "*Spec.js") x Path.rebase(testDir, workDir)).map(_._2)
  Process(Seq("mocha", "-R", "spec") ++ tests.map(_.getCanonicalPath), workDir, "NODE_PATH" -> (workDir / "lib").getCanonicalPath) !
}

test in Test <<= (test in Test).dependsOn(mochaTests)
