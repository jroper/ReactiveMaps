import com.typesafe.sbt.web.SbtWebPlugin._
import com.typesafe.sbt.jse._
import play.PlayTestAssetsCompiler
import spray.json.{JsString, JsArray}
import WebKeys._

name := "snapapp"

version := "1.0-SNAPSHOT"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-contrib" % "2.2.3",
  "com.typesafe.play.extras" %% "play-geojson" % "1.0.0",
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.0.0",
  "org.webjars" % "knockout" % "2.3.0",
  "org.webjars" % "requirejs" % "2.1.11-SNAPSHOT",
  "org.webjars" % "leaflet" % "0.7.1",
  "org.webjars" % "requirejs-node" % "2.1.11-SNAPSHOT" % "test",
  "org.webjars" % "squirejs" % "0.1.0-SNAPSHOT" % "test"
)

play.Project.playScalaSettings

webSettings

SbtJsEnginePlugin.jsEngineSettings

SbtJsEnginePlugin.JsEngineKeys.engineType := SbtJsEnginePlugin.JsEngineKeys.EngineType.Node

resourceGenerators in Test <+= PlayTestAssetsCompiler.CoffeescriptCompiler

coffeescriptEntryPoints in Test <<= (sourceDirectory in Test)(base => base / "assets" ** "*.coffee")

val copyAllMochaSources = TaskKey[File]("copy-mocha-sources", "Copy all JavaScript to one location")

val mochaWorkingDir = SettingKey[File]("mocha-working-directory", "The Mocha working directory")

val mochaTests = TaskKey[Unit]("mocha-tests", "Run the mocha tests")

mochaWorkingDir <<= target / "mocha"

def copyJsFiles(sources: Seq[File], target: File): Seq[(File, File)] = {
  val copyDescs: Seq[(File, File)] = for {
    source: File <- sources
    mapped <- (source ** "*.js") x Path.rebase(source, target)
  } yield mapped
  val toCopy = copyDescs.filter {
    case (s, t) => s.lastModified > t.lastModified
  }
  IO.copy(toCopy)
  copyDescs
}

copyAllMochaSources <<= (webJars in Assets, webJars in TestAssets, resourceManaged in Compile,
  baseDirectory, resourceManaged in Test, mochaWorkingDir, managedResources in Compile,
  managedResources in Test) map {
  (webJars, testWebJars, resources, base, testResources, workDir, _, _) =>
    copyJsFiles(Seq(webJars, testWebJars, resources / "public", testResources / "public"), workDir)
    copyJsFiles(Seq(base / "public-test"), workDir)
    workDir
  }

mochaTests := {
  val testDir: File = (resourceManaged in Test).value
  val workDir: File = copyAllMochaSources.value
  val tests = ((testDir / "public" ** "*Test.js") +++ (testDir / "public" ** "*Spec.js") x Path.rebase(testDir / "public", workDir)).map(_._2.getCanonicalPath)
  import scala.concurrent.duration._
  val pluginNodeModules: File = (nodeModules in Plugin).value
  val prodNodeModules: File = (nodeModules in Assets).value
  val testNodeModules: File = (nodeModules in TestAssets).value
  val baseDir: File = baseDirectory.value
  val options = s"""{"require":["${(workDir / "SetupMocha").getCanonicalPath}"]}"""
  val result = new SbtJsTaskPlugin() {}.executeJs(state.value, SbtJsEnginePlugin.JsEngineKeys.engineType.value,
    Seq(pluginNodeModules.getCanonicalPath, prodNodeModules.getCanonicalPath, testNodeModules.getCanonicalPath, (baseDir / "node_modules").getCanonicalPath),
    baseDir / "mocha.js", Seq(options, JsArray(tests.map(JsString.apply).toList).toString()), 1.hour)
  Unit
}

test in Test <<= (test in Test).dependsOn(mochaTests)
