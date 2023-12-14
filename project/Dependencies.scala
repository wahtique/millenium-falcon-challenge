import sbt._
object Dependencies {
  object com {

    object github {
      object dwickern {
        val scalaNameof = "com.github.dwickern" %% "scala-nameof" % "4.0.0" % "provided"
      }
    }

    object outr {
      val scribe     = "com.outr" %% "scribe"      % "3.13.0"
      val scribeCats = "com.outr" %% "scribe-cats" % "3.13.0"
    }

    object softwaremill {
      object sttp {
        object tapir {
          val tapirCore            = "com.softwaremill.sttp.tapir" %% "tapir-core"              % "1.9.5"
          val tapirJsonPickler     = "com.softwaremill.sttp.tapir" %% "tapir-json-pickler"      % "1.9.5"
          val tapirHttp4sServer    = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % "1.9.5"
          val tapirSwaggerUiBundle = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.9.5"
        }
      }
    }

    object zaxxer {
      val hikariCP = "com.zaxxer" % "HikariCP" % "5.1.0"
    }
  }

  object io {

    object circe {
      val circeCore    = "io.circe" %% "circe-core"    % "0.14.5"
      val circeGeneric = "io.circe" %% "circe-generic" % "0.14.5"
      val circeParser  = "io.circe" %% "circe-parser"  % "0.14.5"
    }

    object crashbox {
      val simplesql = "io.crashbox" %% "simplesql" % "0.2.2"
    }

    object github {

      object iltotore {
        val iron           = "io.github.iltotore" %% "iron"            % "2.3.0"
        val ironScalacheck = "io.github.iltotore" %% "iron-scalacheck" % "2.3.0"
        val ironCats       = "io.github.iltotore" %% "iron-cats"       % "2.3.0"
        val ironCiris      = "io.github.iltotore" %% "iron-ciris"      % "2.3.0"
        val ironCirce      = "io.github.iltotore" %% "iron-circe"      % "2.3.0"
      }
    }
  }

  object is {
    object cir {
      val ciris      = "is.cir" %% "ciris"       % "3.5.0"
      val cirisCirce = "is.cir" %% "ciris-circe" % "3.5.0"
    }
  }

  object org {

    object http4s {
      val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % "0.23.24"
    }
    object scalacheck {
      val scalacheck = "org.scalacheck" %% "scalacheck" % "1.17.0"
    }

    object scalameta {
      val munit           = moduleId("munit")
      val munitScalacheck = moduleId("munit-scalacheck")
      private def moduleId(artifact: String): ModuleID =
        "org.scalameta" %% artifact % "1.0.0-M10"
    }

    object typelevel {
      val catsEffect       = "org.typelevel" %% "cats-effect"       % "3.5.2"
      val munitCatsEffect  = "org.typelevel" %% "munit-cats-effect" % "2.0.0-M4"
      val scalacheckEffect = "org.typelevel" %% "scalacheck-effect" % "1.0.4"
      val spire            = "org.typelevel" %% "spire"             % "0.18.0"
    }

    object xerial {
      val sqliteJdbc = "org.xerial" % "sqlite-jdbc" % "3.44.1.0"
    }
  }
}
