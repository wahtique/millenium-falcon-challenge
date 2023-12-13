import Dependencies.*

ThisBuild / organization := "io.github.wahtique"
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
    org.typelevel.catsEffect, // effect system
    `io.github`.iltotore.iron // refined types
  ),
  libraryDependencies ++= Seq(
    org.scalameta.munit,                // test framework
    org.scalacheck.scalacheck,          // property testing
    org.scalameta.munitScalacheck,      // scalacheck <-> munit
    org.typelevel.munitCatsEffect,      // cats-effect <-> munit
    org.typelevel.scalacheckEffect,     // cats-effect <-> scalacheck
    `io.github`.iltotore.ironScalacheck // iron <-> scalacheck
  ).map(_ % Test)
)

lazy val core =
  project
    .in(file("modules/core"))
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(commonDependencies)
    .settings(
      libraryDependencies ++= Seq(
        org.typelevel.spire // math
      )
    )

lazy val backend =
  project
    .in(file("modules/backend"))
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(commonDependencies)
    .enablePlugins(BuildInfoPlugin)
    .settings(
      buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "backend",
      libraryDependencies ++= Seq(
        com.softwaremill.sttp.tapir.tapirCore,           // type safe api definition
        com.softwaremill.sttp.tapir.tapirJsonPickler,    // type safe json
        com.softwaremill.sttp.tapir.tapirHttp4sServer,   // api definitions <-> http4s server
        org.http4s.http4sEmberServer,                    // http4s server
        com.softwaremill.sttp.tapir.tapirSwaggerUiBundle // swagger ui
      )
    )
    .dependsOn(core)
