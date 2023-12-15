package core.computer

import cats.syntax.order.*
import core.computer.Routing.Trajectory
import core.computer.Routing.Trajectory.Action.*
import core.model.Planet
import io.github.iltotore.iron.*
import munit.ScalaCheckSuite

class TrajectoryTests extends ScalaCheckSuite:

  given Conversion[String, Planet] = s => Planet(s.assume)

  test("action ordering : Noop < Travel < Refuel"):
    assertEquals(Noop, Noop)
    assertEquals(Travel, Travel)
    assertEquals(Refuel, Refuel)
    assert(Noop < Travel)
    assert(Noop < Refuel)
    assert(Travel < Refuel)

  test("trajectory ordering : priority is given to less encounters with bounty hunters"):
    val t1 = Trajectory("A", Seq.empty, Noop, 0, 0, 0)
    val t2 = Trajectory("A", Seq.empty, Noop, 0, 0, 1)
    val t3 = Trajectory("A", Seq.empty, Refuel, 0, 0, 2)

    val sorted = Seq(t1, t2, t3)

    forAll(Gen.oneOf(sorted.permutations.toSeq)): trajectories =>
      assert(trajectories.sorted(using Trajectory.ordering) == sorted)
