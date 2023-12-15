package backend

import cats.effect.IO
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

trait Routes:
  def serverEndpoints: List[ServerEndpoint[Any, IO]]
  def routes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(serverEndpoints)
