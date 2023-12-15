package core.io

import core.model.*
import io.github.iltotore.iron.*
import munit.CatsEffectSuite
import scala.concurrent.duration.*

class GalacticMapTests extends CatsEffectSuite with TestResourceLoader:

  override def munitIOTimeout: Duration = 5.minutes

  test("Load galactic map"):
    val file   = testResource("examples/example1/universe.db")
    val map    = GalacticMapLoader.make(file.toAbsolutePath())
    val galaxy = map.use(_.load.value)
    assertIOBoolean(galaxy.map(_.isRight))

  test("load a non-empty galactic map"):
    val file   = testResource("examples/example1/universe.db")
    val map    = GalacticMapLoader.make(file.toAbsolutePath())
    val galaxy = map.use(_.load.value)
    assertIOBoolean(galaxy.map(_.exists(_.planets.nonEmpty)))

  test("load a galaxy containing Endor"):
    val file   = testResource("examples/example1/universe.db")
    val map    = GalacticMapLoader.make(file.toAbsolutePath())
    val galaxy = map.use(_.load.value)
    assertIOBoolean(galaxy.map(_.exists(_.planets.contains(Planet("Endor")))))

  test("planet Endor should have some routes"):
    val file         = testResource("examples/example1/universe.db")
    val map          = GalacticMapLoader.make(file.toAbsolutePath())
    val galaxy       = map.use(_.load.value)
    val actualRoutes = galaxy.map(_.map(_.routesFrom(Planet("Endor"))))
    val expectedRoutes =
      Set(
        Route(
          origin = Planet("Endor"),
          destination = Planet("Dagobah"),
          travelTime = 4
        ),
        Route(
          origin = Planet("Endor"),
          destination = Planet("Hoth"),
          travelTime = 1
        )
      )
    assertIO(actualRoutes, Right(expectedRoutes))
