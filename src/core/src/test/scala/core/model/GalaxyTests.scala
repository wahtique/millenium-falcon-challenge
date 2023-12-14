package core.model

import cats.syntax.monoid.*
import core.model.Planet
import io.github.iltotore.iron.*
import munit.FunSuite

class GalaxyTests extends FunSuite:

  private val A = Planet("A")
  private val B = Planet("B")
  private val C = Planet("C")
  private val D = Planet("D")

  test("Combine a non empty Galaxy with an empty yields the non empty Galaxy"):
    val galaxy1 = Galaxy(Map(A -> Set(Route(A, B, 1))))
    val galaxy2 = Galaxy(Map.empty)
    assertEquals(galaxy1 |+| galaxy2, galaxy1)

  test("combine two disjointed galaxies yields a new one with planets from both"):
    val galaxy1 = Galaxy(Map(A -> Set(Route(A, B, 1))))
    val galaxy2 = Galaxy(Map(C -> Set(Route(C, D, 1))))
    assertEquals(galaxy1 |+| galaxy2, Galaxy(Map(A -> Set(Route(A, B, 1)), C -> Set(Route(C, D, 1)))))

  test("combine two galaxies with common planets yields a new one with planets from both"):
    val commonPlanet = A -> Set(Route(A, B, 1))
    val otherPlanet  = B -> Set(Route(B, C, 1))
    val otherPlanet2 = C -> Set(Route(C, D, 1))
    val galaxy1      = Galaxy(Map(commonPlanet, otherPlanet))
    val galaxy2      = Galaxy(Map(commonPlanet, otherPlanet2))
    assertEquals(galaxy1 |+| galaxy2, Galaxy(Map(commonPlanet, otherPlanet, otherPlanet2)))
