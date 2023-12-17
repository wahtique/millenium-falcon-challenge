package backend

import backend.api.NavigationEndpoints
import backend.routes.DocsRoutes
import backend.routes.NavigationRoutes
import backend.service.NavigationService
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.foldable.*
import core.io.GalacticMapLoader
import core.io.MissionParametersLoader
import java.nio.file.Path
import org.http4s.HttpApp
import org.http4s.implicits.*
import org.http4s.server.middleware.CORS
import scala.util.chaining

object App:
  def make(missionParamsPath: String): Resource[IO, HttpApp[IO]] =
    for
      // inputs
      missionParamsLoader <- MissionParametersLoader.make(Path.of(missionParamsPath))
      missionParameters   <- missionParamsLoader.load.foldF(IO.raiseError, IO.pure).toResource
      galaxyMapLoader     <- GalacticMapLoader.make(missionParameters.routes_db)
      galaxy              <- galaxyMapLoader.load.foldF(IO.raiseError, IO.pure).toResource
      // service
      navigationService <- NavigationService.make(galaxy, missionParameters)
      // routes
      navigationRoutes <- NavigationRoutes.make(navigationService)
      swagger          <- DocsRoutes.make(List(NavigationEndpoints.giveMeTheOdds))
      routes = List(navigationRoutes, swagger).map(_.routes).foldK
      app    = routes.orNotFound.pipe(CORS.policy.withAllowOriginAll.apply)
    yield app
