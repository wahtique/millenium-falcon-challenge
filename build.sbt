import Dependencies._
import Dependencies.{io => ioo}

ThisBuild / organization := "ioo.github.wahtique"
ThisBuild / scalaVersion := "3.3.1"

lazy val commonSettings = {
  lazy val commonScalacOptions = Seq(
    Compile / console / scalacOptions := {
      (Compile / console / scalacOptions)
        .value
        .filterNot(_.contains("wartremover"))
        .filterNot(Scalac.Lint.toSet)
        .filterNot(Scalac.FatalWarnings.toSet) :+ "-Wconf:any:silent"
    },
    // Compile / run / fork := true,
    Test / console / scalacOptions :=
      (Compile / console / scalacOptions).value
  )

  lazy val otherCommonSettings = Seq(
    update / evictionWarningOptions := EvictionWarningOptions.empty
    // cs launch scalac:3.3.1 -- -Wconf:help
    // src is not yet available for Scala3
    // scalacOptions += s"-Wconf:src=${target.value}/.*:s",
  )

  Seq(
    commonScalacOptions,
    otherCommonSettings
  ).reduceLeft(_ ++ _)
}

lazy val autoImportSettings = Seq(
  scalacOptions +=
    Seq(
      "java.lang",
      "scala",
      "scala.Predef",
      "scala.annotation",
      "scala.util.chaining"
    ).mkString(start = "-Yimports:", sep = ",", end = ""),
  Test / scalacOptions +=
    Seq(
      "org.scalacheck",
      "org.scalacheck.Prop"
    ).mkString(start = "-Yimports:", sep = ",", end = "")
)

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    org.typelevel.catsEffect,        // effect system
    ioo.github.iltotore.iron,        // refined types
    ioo.github.iltotore.ironCats,    // config <-> refined types
    com.outr.scribe,                 // logging
    com.outr.scribeCats,             // logging <-> cats effect
    com.github.dwickern.scalaNameof, // nameof stuff at compile time
    ioo.crashbox.simplesql,          // sql
    org.xerial.sqliteJdbc,           // sqlite jdbc driver
    com.zaxxer.hikariCP              // connection pool
  ),
  libraryDependencies ++= Seq(
    org.scalameta.munit,               // test framework
    org.scalacheck.scalacheck,         // property testing
    org.scalameta.munitScalacheck,     // scalacheck <-> munit
    org.typelevel.munitCatsEffect,     // cats-effect <-> munit
    org.typelevel.scalacheckEffect,    // cats-effect <-> scalacheck
    ioo.github.iltotore.ironScalacheck // iron <-> scalacheck
  ).map(_ % Test)
)

lazy val core =
  project
    .in(file("src/core"))
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(commonDependencies)
    .enablePlugins(BuildInfoPlugin)
    .settings(
      buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "core"
    )
    .settings(
      libraryDependencies ++= Seq(
        org.typelevel.spire,           // math
        ioo.circe.circeCore,           // json
        ioo.circe.circeGeneric,        // json codecs derivation
        ioo.circe.circeParser,         // json parsing
        is.cir.ciris,                  // config
        is.cir.cirisCirce,             // config <-> json
        ioo.github.iltotore.ironCiris, // config <-> refined types
        ioo.github.iltotore.ironCirce  // json <-> refined types
      )
    )

lazy val cli =
  project
    .in(file("src/cli"))
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(commonDependencies)
    .settings(
      // JAR assembly
      assembly / mainClass       := Some("cli.R2D2"),
      assembly / assemblyJarName := "r2d2.jar",
      // deps
      libraryDependencies ++= Seq(
        com.monovore.decline,      // cli
        com.monovore.declineEffect // cli <-> cats effect
      )
    )
    .dependsOn(core % "test->test;compile->compile")

lazy val backend =
  project
    .in(file("src/backend"))
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(commonDependencies)
    .settings(
      // build info
      buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "backend",
      // JAR assembly
      assembly / mainClass       := Some("backend.Main"),
      assembly / assemblyJarName := "navicore.jar",
      assembly / assemblyMergeStrategy := {
        case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
          MergeStrategy.singleOrError
        case PathList("META-INF", "resources", "webjars", "swagger-ui", _*) =>
          MergeStrategy.singleOrError
        // magic incantation to make the jdbc driver stick
        case PathList("META-INF", xs @ _*) =>
          xs.map(_.toLowerCase) match {
            case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) |
                ("license" :: Nil) | ("notice" :: Nil) =>
              MergeStrategy.discard
            case _ => MergeStrategy.first // was 'discard' previousely
          }
        case PathList("META-INF", _*) => MergeStrategy.discard // Optional, but usually required
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      // assembly / assemblyExcludedJars := {
      //   val cp: Classpath = (assembly / fullClasspath).value
      //   cp.filter(jar => jar.data.getName == "snakeyaml-2.2.jar")
      // },
      // deps
      libraryDependencies ++= Seq(
        com.softwaremill.sttp.tapir.tapirCore,           // type safe api definition
        com.softwaremill.sttp.tapir.tapirCirce,          // json
        com.softwaremill.sttp.tapir.tapirHttp4sServer,   // api definitions <-> http4s server
        com.softwaremill.sttp.tapir.tapirIron,           // refined types
        org.http4s.http4sEmberServer,                    // http4s server
        com.softwaremill.sttp.tapir.tapirSwaggerUiBundle // swagger ui
      )
    )
    .dependsOn(core)

lazy val frontend =
  project
    .in(file("src/frontend"))
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(commonDependencies)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "tyrian-io" % "0.8.0"
      ),
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
