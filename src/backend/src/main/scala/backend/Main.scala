package backend

import cats.effect.IO
import cats.effect.ResourceApp
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import scala.concurrent.ExecutionContext

object Main extends ResourceApp.Forever:
  def run(args: List[String]) =
    // todo maybe use decline to have a cleaner parsing experience
    args match
      case Nil => IO.raiseError(new IllegalArgumentException("Missing input 'millennium-falcon.json'.")).toResource
      case pathToMillenniumFalconJson :: _ =>
        for
          app <- App.make(pathToMillenniumFalconJson)
          _ <- EmberServerBuilder
            .default[IO]
            .withHost(ipv4"0.0.0.0")
            .withPort(port"8080")
            .withHttpApp(app)
            .build
            .evalOn(ExecutionContext.global)
        yield ()
