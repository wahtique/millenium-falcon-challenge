package backend.routes

import backend.Routes
import cats.effect.IO
import cats.effect.kernel.Resource
import core.BuildInfo
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

trait DocsRoutes extends Routes

object DocsRoutes:
  def make(endpoints: List[AnyEndpoint]): Resource[IO, DocsRoutes] =
    Resource.pure:
      new DocsRoutes:
        override val serverEndpoints: List[ServerEndpoint[Any, IO]] =
          SwaggerInterpreter().fromEndpoints[IO](endpoints, BuildInfo.name, BuildInfo.version)
