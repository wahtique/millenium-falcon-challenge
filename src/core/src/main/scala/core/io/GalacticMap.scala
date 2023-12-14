package core.io

import simplesql as sq
import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.instances.either.*
import cats.syntax.either.*
import cats.syntax.monoid.*
import cats.syntax.traverse.*
import com.zaxxer.hikari.HikariDataSource
import core.model.Error.IOFailure
import core.model.Galaxy
import core.model.MissionDays
import core.model.Planet
import core.model.Route

trait GalacticMap:
  def load: EitherT[IO, IOFailure, Galaxy]

object GalacticMap:

  final case class GalaxyLine(ORIGIN: String, DESTINATION: String, TRAVEL_TIME: Int) derives sq.Reader

  def make(location: String): Resource[IO, GalacticMap] =

    val ds = Resource.pure[IO, HikariDataSource]:
      val hds = HikariDataSource()
      hds.setJdbcUrl(s"jdbc:sqlite:$location")
      hds

    ds.map: hds =>
      new GalacticMap:
        def load: EitherT[IO, IOFailure, Galaxy] =
          val galaxyOrThrowable = for
            lines <- EitherT[IO, Throwable, List[GalaxyLine]](
              IO.delay(
                sq.transaction(hds)(sq.read[GalaxyLine](sql"SELECT * FROM ROUTES"))
              ).attempt
            )
            routes <- EitherT.fromEither[IO](
              lines
                .map: line =>
                  for
                    origin      <- Planet.either(line.ORIGIN)
                    destination <- Planet.either(line.DESTINATION)
                    travelTime  <- MissionDays.either(line.TRAVEL_TIME)
                  yield Route(origin, destination, travelTime)
                .sequence
                .leftMap(new Exception(_))
            )
            galaxy = routes.foldLeft(Galaxy.empty) { (galaxy, route) =>
              val newRoutes = Galaxy(
                Map(
                  route.origin      -> Set(route),
                  route.destination -> Set(route.inverse)
                )
              )
              galaxy |+| newRoutes
            }
          yield galaxy
          galaxyOrThrowable.leftMap(t => IOFailure.InvalidGalacticMap(t.getMessage))
