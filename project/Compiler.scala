import sbt.Keys._
import sbt._

object Compiler {
  private def unmanaged(ver: String, base: File): Seq[File] =
    CrossVersion.partialVersion(ver) match {
      case Some((2, 13)) =>
        Seq(base / "scala-2.13+")

      case _ =>
        Seq(base / "scala-2.13-")

    }

  lazy val settings = Seq(
    unmanagedSourceDirectories in Compile ++= {
      unmanaged(scalaVersion.value, (sourceDirectory in Compile).value)
    },
    libraryDependencies in ThisBuild ++= {
      val silencerVer = "1.4.4"
      val v = scalaVersion.value

      Seq(
        compilerPlugin(
          ("com.github.ghik" %% "silencer-plugin" % silencerVer).
            cross(CrossVersion.full)),
        ("com.github.ghik" %% "silencer-lib" % silencerVer % Provided).
          cross(CrossVersion.full))
    },
    scalacOptions ++= Seq(
      "-encoding", "UTF-8", "-target:jvm-1.8",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xfatal-warnings",
      "-Xlint",
      "-g:vars"),
    scalacOptions ++= {
      if (scalaBinaryVersion.value != "2.13") {
        Seq(
          "-Ywarn-numeric-widen",
          "-Ywarn-dead-code",
          "-Ywarn-value-discard",
          "-Ywarn-infer-any",
          "-Ywarn-unused",
          "-Ywarn-unused-import")
      } else Nil
    },
    scalacOptions += { // Silencer
      "-P:silencer:globalFilters=Response\\ in\\ package\\ protocol\\ is\\ deprecated;killCursor;Use\\ \\`find\\`\\ with\\ optional\\ \\`projection\\`"
    },
    scalacOptions in Compile ++= {
      if (scalaBinaryVersion.value != "2.11") Nil
      else Seq(
        "-Yconst-opt",
        "-Yclosure-elim",
        "-Ydead-code",
        "-Yopt:_"
      )
    },
    scalacOptions in (Compile, doc) := (scalacOptions in Test).value,
    scalacOptions in (Compile, console) ~= {
      _.filterNot { opt =>
        opt.startsWith("-X") || opt.startsWith("-Y") || opt.startsWith("-P")
      }
    },
    scalacOptions in (Test, console) ~= {
      _.filterNot { opt =>
        opt.startsWith("-X") || opt.startsWith("-Y") || opt.startsWith("-P")
      }
    },
    scalacOptions in (Compile, doc) ++= Seq(
      "-unchecked", "-deprecation",
      /*"-diagrams", */"-implicits", "-skip-packages", "samples") ++
      Opts.doc.title("ReactiveMongo Streaming API") ++
      Opts.doc.version(Release.major.value)
  )
}
