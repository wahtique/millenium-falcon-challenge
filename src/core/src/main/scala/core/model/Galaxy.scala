package core.model

import cats.kernel.Monoid
import cats.syntax.monoid.*
import io.circe.Decoder
import io.circe.Encoder
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.*

type NotBlank      = Not[Blank]
opaque type Planet = String :| NotBlank
object Planet extends RefinedTypeOps[String, NotBlank, Planet]:
  given decoder: Decoder[Planet] = Decoder.decodeString.emap(Planet.either)
  given encoder: Encoder[Planet] = Encoder.encodeString.contramap(_.value)

/** Millenias of astrophysics summed up in one line.
  *
  * A galaxy far far away represented by its known planets. Because if you can't visit it, it might as well not exist.
  *
  * @param planets
  *   a lagaxy is just a bunch of planets ( I KNOW this is not true ) connected by routes
  */
final case class Galaxy(planets: Map[Planet, Set[Route]]):
  def contains(planet: Planet): Boolean      = planets.contains(planet)
  def routesFrom(planet: Planet): Set[Route] = planets.getOrElse(planet, Set.empty)
object Galaxy:
  def empty: Galaxy = Galaxy(Map.empty)
  given Monoid[Galaxy] with
    def empty: Galaxy                         = Galaxy.empty
    def combine(x: Galaxy, y: Galaxy): Galaxy = Galaxy(x.planets |+| y.planets)

/** A stable hyperspace lane between two planets.
  *
  * A route is represented as a uni-directional connection between two systems even though travel is bi-directional.
  * Hence R === inverse(R) must be assumed.
  *
  * @param origin
  *   starting point ( or endpoint of the inverse route )
  * @param destination
  *   end point ( or starting point of the inverse route )
  * @param travelTime
  *   in days
  */
final case class Route(origin: Planet, destination: Planet, travelTime: MissionDays):
  def inverse: Route = copy(origin = destination, destination = origin)
