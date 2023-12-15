package backend.routes

import backend.Routes
import backend.api.NavigationEndpoints
import backend.service.NavigationService
import cats.effect.IO
import cats.effect.kernel.Resource
import sttp.tapir.server.ServerEndpoint

trait NavigationRoutes extends Routes:
  def giveMeTheOdds: ServerEndpoint[Any, IO]
  override def serverEndpoints: List[ServerEndpoint[Any, IO]] = List(giveMeTheOdds)

object NavigationRoutes:
  def make(navigationService: NavigationService): Resource[IO, NavigationRoutes] =
    Resource.pure:
      new NavigationRoutes:
        def giveMeTheOdds: ServerEndpoint[Any, IO] =
          NavigationEndpoints
            .giveMeTheOdds
            .serverLogicSuccess: imperialData =>
              IO.pure(navigationService.computeOdds(imperialData))
