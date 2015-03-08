
import sbt._
import Keys._
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.SonatypeKeys._

object Build extends Build {

  lazy val commonSettings = Defaults.defaultSettings ++
    Seq(
      organization := "org.scalafx",
      version := "0.2.2-SNAPSHOT",
      crossScalaVersions := Seq("2.10.5", "2.11.6"),
      scalacOptions ++= Seq("-deprecation"),
      resolvers += Resolver.sonatypeRepo("releases"),
      libraryDependencies ++= Seq(
	"org.scalafx" %% "scalafx" % "8.0.31-R7",
	"org.scalatest" %% "scalatest" % "2.2.2" % "test"),

      unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/jfxrt.jar")),
      fork := true,
      exportJars := true,

      addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),

      pomExtra :=
        <url>https://github.com/vigoo/scalafxml</url>
        <scm>
          <url>github.com:vigoo/scalafxml.git</url>
          <connection>scm:git@github.com:vigoo/scalafxml.git</connection>
        </scm>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
           </license>
        </licenses>
        <developers>
          <developer>
            <id>vigoo</id>
            <name>Daniel Vigovszky</name>
            <url>https://github.com/vigoo</url>
          </developer>
          <developer>
            <id>jpsacha</id>
            <name>Jarek Sacha</name>
            <url>https://github.com/jpsacha</url>
          </developer>
        </developers>
    ) ++ sonatypeSettings

  lazy val root: Project = Project("root", file("."),
    settings = commonSettings ++ Seq(
      run <<= run in Compile in core,
      publishArtifact := false
    )) aggregate("scalafxml-core-macros-sfx8", "scalafxml-core-sfx8", "scalafxml-subcut-sfx8", "scalafxml-macwire-sfx8", "scalafxml-guice-sfx8", "scalafxml-demo-sfx8")

  lazy val core = Project("scalafxml-core-sfx8", file("core"),
    settings = commonSettings ++ Seq(
      description := "ScalaFXML core module"
    ))
    .dependsOn(coreMacros)

  lazy val coreMacros = Project("scalafxml-core-macros-sfx8", file("core-macros"),
    settings = commonSettings ++ Seq(
      description := "ScalaFXML macros",
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),

      libraryDependencies := {
        CrossVersion.partialVersion(scalaVersion.value) match {
          // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
          case Some((2, scalaMajor)) if scalaMajor >= 11 =>
            libraryDependencies.value
          // in Scala 2.10, quasiquotes are provided by macro paradise
          case Some((2, 10)) =>
            libraryDependencies.value ++ Seq(
              compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
              "org.scalamacros" %% "quasiquotes" % "2.0.1" cross CrossVersion.binary)
        }
      }

    ))

  lazy val subcutSettings = commonSettings ++ Seq(
    description := "SubCut based dependency resolver for ScalaFXML",
    libraryDependencies += "com.escalatesoft.subcut" %% "subcut" % "2.1")

  lazy val subcut = Project("scalafxml-subcut-sfx8", file("subcut"),
    settings = subcutSettings)
    .aggregate(core)
    .dependsOn(core)

  lazy val guiceSettings = commonSettings ++ Seq(
    description := "Guice based dependency resolver for ScalaFXML",
    libraryDependencies += "com.google.inject" % "guice" % "3.0"
  )

  lazy val guice = Project("scalafxml-guice-sfx8", file("guice"),
    settings = guiceSettings)
    .aggregate(core)
    .dependsOn(core)

  lazy val macwireSettings = commonSettings ++ Seq(
    description := "MacWire based dependency resolver for ScalaFXML",
    libraryDependencies += "com.softwaremill.macwire" %% "macros" % "0.7.3",
    libraryDependencies += "com.softwaremill.macwire" %% "runtime" % "0.7.3"
  )
  lazy val macwire = Project("scalafxml-macwire-sfx8", file("macwire"), settings = macwireSettings)
    .aggregate(core)
    .dependsOn(core)

  lazy val demo = Project("scalafxml-demo-sfx8", file("demo"),
    settings = subcutSettings ++ Seq(
      description := "ScalaFXML demo applications",
      publishArtifact := false
    ))
    .dependsOn(core, subcut, guice, macwire)

}
