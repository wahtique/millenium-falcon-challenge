package core.computer

import cats.data.NonEmptySeq
import core.computer.Routing.*
import core.computer.Routing.Trajectory.Action
import core.model.*
import core.model.Error.MissionFailure
import io.github.iltotore.iron.*
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll

trait RoutingTestFixtures:
  val strictlyPositiveDays: Gen[MissionDays] = Gen.posNum[Int].suchThat(_ > 0).map(_.refine)

  val origin      = Planet("A")
  val destination = Planet("B")

  val trap = Planet("Trap")
  val safe = Planet("Safe")

  def imperialData(countDown: MissionDays, trapDays: Seq[MissionDays]): ImperialData =
    val traps = trapDays.map(BountyHuntersLocation(trap, _)).toSet
    ImperialData(countDown, traps)

  // 3 planets galaxy
  def galaxy(
      originToMiddle: MissionDays,
      middleToDestination: MissionDays,
      middlePlanet: Planet = safe
  ): Galaxy =
    Galaxy(
      Map(
        origin -> Set(Route(origin, middlePlanet, originToMiddle)),
        middlePlanet -> Set(
          Route(middlePlanet, destination, middleToDestination),
          Route(origin, middlePlanet, originToMiddle)
        ),
        destination -> Set(Route(destination, middlePlanet, middleToDestination))
      )
    )

  // 4 planets galaxy
  def galaxy(
      originToSafe: MissionDays,
      safeToDestination: MissionDays,
      originToTrap: MissionDays,
      trapToDestination: MissionDays
  ): Galaxy =
    Galaxy(
      Map(
        origin      -> Set(Route(origin, trap, originToTrap), Route(origin, safe, originToSafe)),
        trap        -> Set(Route(trap, destination, trapToDestination), Route(origin, trap, originToTrap)),
        safe        -> Set(Route(safe, destination, safeToDestination), Route(origin, safe, originToSafe)),
        destination -> Set(Route(destination, safe, safeToDestination), Route(destination, trap, trapToDestination))
      )
    )

class RoutingTests extends ScalaCheckSuite with RoutingTestFixtures:

  // trivial cases = mission parameters are enough to deduce the result

  test("origin not in galaxy => 0% chances of success"):
    forAll(strictlyPositiveDays, strictlyPositiveDays): (countDown: MissionDays, autonomy: MissionDays) =>
      val galaxy       = Galaxy(Map(destination -> Set.empty))
      val imperialData = ImperialData(countDown, Set.empty)
      assertEquals(
        findBestOdds(galaxy, imperialData, autonomy)(origin, destination),
        Left(MissionFailure.UnreachablePlanet)
      )

  test("destination not in galaxy => 0% chances of success"):
    forAll(strictlyPositiveDays, strictlyPositiveDays): (countDown: MissionDays, autonomy: MissionDays) =>
      val galaxy       = Galaxy(Map(origin -> Set.empty))
      val imperialData = ImperialData(countDown, Set.empty)
      assertEquals(
        findBestOdds(galaxy, imperialData, autonomy)(origin, destination),
        Left(MissionFailure.UnreachablePlanet)
      )

  test("origin == destination => 100% chances of success"):
    forAll(strictlyPositiveDays, strictlyPositiveDays): (countDown: MissionDays, autonomy: MissionDays) =>
      val galaxy       = Galaxy(Map(origin -> Set.empty))
      val imperialData = ImperialData(countDown, Set.empty)
      assertEquals(findBestOdds(galaxy, imperialData, autonomy)(origin, origin), Right[MissionFailure, SuccessOdds](1d))

  test("countDown == 0 && origin != destination => 0% chances of success"):
    forAll(strictlyPositiveDays): (autonomy: MissionDays) =>
      val galaxy       = Galaxy(Map(origin -> Set(Route(origin, destination, 1)), destination -> Set.empty))
      val imperialData = ImperialData(0, Set.empty)
      assertEquals(
        findBestOdds(galaxy, imperialData, autonomy)(origin, destination),
        Left(MissionFailure.MissionAlreadyFailed)
      )

  test("autonomy == 0 && origin != destination => 0% chances of success"):
    forAll(strictlyPositiveDays): (countDown: MissionDays) =>
      val galaxy = Galaxy(
        Map(
          origin      -> Set(Route(origin, destination, 1)),
          destination -> Set(Route(destination, origin, 1))
        )
      )
      val imperialData = ImperialData(countDown, Set.empty)
      assertEquals(
        findBestOdds(galaxy, imperialData, 0)(origin, destination),
        Left(MissionFailure.MilleniumFalconCantFly)
      )

  // non trivial but simple cases : the galaxy is a very constrained graph, no bounty hunters, no refueling

  test("next trajectory from origin should contain destination"):
    forAll(
      for
        // min distance / automy is 1, countdown should be gt 1
        countDown <- strictlyPositiveDays
        // destination can be reached before the countDown ends
        distance <- strictlyPositiveDays.suchThat(_ < countDown)
        // autonomy is sufficient to reach the destination
        autonomy <- strictlyPositiveDays.suchThat(_ >= distance)
      yield (countDown, distance, autonomy)
    ): (countDown: MissionDays, distance: MissionDays, autonomy: MissionDays) =>
      val route        = Route(origin, destination, distance)
      val imperialData = ImperialData(countDown, Set.empty)
      val trajectory   = Trajectory(NonEmptySeq.one(origin), Action.Noop, autonomy, 0, 0)
      val expected =
        Trajectory(NonEmptySeq.of(destination, origin), Action.Travel, (autonomy - distance).assume, distance, 0)
      assertEquals(trajectory.next(route, imperialData), Some(expected))

  test("destination rechable in one jump => 100% chances of success"):
    forAll(
      for
        // min distance / autonomy is 1, countdown should be gt 1
        countDown <- strictlyPositiveDays
        // destination can be reached before the countDown ends
        distance <- strictlyPositiveDays.suchThat(_ < countDown)
        // autonomy is sufficient to reach the destination
        autonomy <- strictlyPositiveDays.suchThat(_ >= distance)
      yield (countDown, distance, autonomy)
    ): (countDown: MissionDays, distance: MissionDays, autonomy: MissionDays) =>
      val galaxy = Galaxy(
        Map(
          origin      -> Set(Route(origin, destination, distance)),
          destination -> Set(Route(destination, origin, distance))
        )
      )
      val imperialData = ImperialData(countDown, Set.empty)
      val actual       = findBestOdds(galaxy, imperialData, autonomy.assume)(origin, destination)
      assertEquals(actual, Right[MissionFailure, SuccessOdds](1d))

  test("destination cannot be reachd no matter what => 0% chances of success"):
    forAll(
      for
        // min distance / autonomy is 1, countdown should be gt 1
        countDown <- strictlyPositiveDays
        // destination cannot be reached before the countDown ends
        distance <- strictlyPositiveDays.suchThat(_ > countDown)
        // autonomy is insufficient to reach the destination
        autonomy <- strictlyPositiveDays.suchThat(_ < distance)
      yield (countDown, distance, autonomy)
    ): (countDown: MissionDays, distance: MissionDays, autonomy: MissionDays) =>
      val galaxy = Galaxy(
        Map(
          origin      -> Set(Route(origin, destination, distance)),
          destination -> Set(Route(destination, origin, distance))
        )
      )
      val imperialData = ImperialData(countDown, Set.empty)
      val actual       = findBestOdds(galaxy, imperialData, autonomy.assume)(origin, destination)
      assertEquals(actual, Left(MissionFailure.UnreachablePlanet))

  // one path with a pit stop for refueling
  test("destination can be reached by refueling"):
    forAll(
      for
        // dist origin -> safe
        originToSafe <- strictlyPositiveDays
        // dist safe -> destination
        offset <- strictlyPositiveDays
        safeToDestination: MissionDays = (offset + originToSafe).refine
        // countDown must be juuuuust right to accomomadate a pitstop
        totalDist = originToSafe + safeToDestination
        countDown = totalDist + 2
        // autonomy is sufficient to cover the longest stretch
        autonomy = safeToDestination
      yield (
        galaxy(originToSafe, safeToDestination.refine),
        imperialData(countDown.refine, Seq.empty),
        autonomy
      )
    ): (galaxy: Galaxy, imperialData: ImperialData, autonomy: MissionDays) =>
      val actual = findBestOdds(galaxy, imperialData, autonomy)(origin, destination)
      assertEquals(actual, Right[MissionFailure, SuccessOdds](1d))

  test("refuel to avoid bounty hunters"):
    forAll(
      for
        // dist origin -> trap
        originToTrap <- strictlyPositiveDays
        // dist trap -> destination
        trapToDestination <- strictlyPositiveDays
        // countDown must be juuuuust right to accomomadate a pitstop
        totalDist              = originToTrap + trapToDestination
        countDown: MissionDays = (totalDist + 2).refine
        // autonomy is sufficient to cover everything
        // ie. refueling is not needed to reach the destination
        autonomy: MissionDays = totalDist.refine
      yield (
        galaxy(originToTrap, trapToDestination, trap),
        imperialData(countDown.refine, Seq(originToTrap)),
        autonomy
      )
    ): (galaxy: Galaxy, imperialData: ImperialData, autonomy: MissionDays) =>
      val actual = findBestOdds(galaxy, imperialData, autonomy)(origin, destination)
      assertEquals(actual, Right[MissionFailure, SuccessOdds](1d))

  // two paths including one with bounty hunters

  test("destination can be reached without encountering bounty hunters => 100% chances of success"):
    forAll(
      for
        // dist origin -> safe
        originToSafe <- strictlyPositiveDays
        // dist safe -> destination
        safeToDestination <- strictlyPositiveDays
        // dist origin -> trap
        originToTrap <- strictlyPositiveDays
        // dist trap -> destination
        trapToDestination <- strictlyPositiveDays
        // countdown is big enough to allow both paths
        maxDist = (originToSafe + safeToDestination).max(originToTrap + trapToDestination)
        // +1 because adding more generators makes the fuzzer break
        countDown: MissionDays = (maxDist + 1).assume
        // autonomy is sufficient to reach the destination without refueling
        autonomy: MissionDays = (maxDist + 1).assume
      yield (
        galaxy(originToSafe, safeToDestination, originToTrap, trapToDestination),
        imperialData(countDown, Seq(originToTrap)),
        autonomy
      )
    ): (galaxy: Galaxy, imperialData: ImperialData, autonomy: MissionDays) =>
      val actual = findBestOdds(galaxy, imperialData, autonomy)(origin, destination)
      assertEquals(actual, Right[MissionFailure, SuccessOdds](1d))

  test("destination cannot be reached witout encountering bounty hunters => 90% succcess chances"):
    forAll(
      for
        // dist origin -> trap
        originToTrap <- strictlyPositiveDays
        // dist trap -> destination
        trapToDestination <- strictlyPositiveDays
        // countdown is big enough to allow both paths
        onlyPathLen = originToTrap + trapToDestination
        // +1 because adding more generators makes the fuzzer break
        countDown: MissionDays = (onlyPathLen + 1).assume
        // autonomy is sufficient to reach the destination without refueling
        autonomy: MissionDays = (onlyPathLen + 1).assume
        // dist origin -> safe is ok
        originToSafe <- strictlyPositiveDays
        // dist safe -> destination MUST be impossible to reach to force the use of the trap
        safeToDestination: MissionDays = (autonomy + 1).assume
        // bounty hunters must be camping at the trap to ensure
        // the millenium falcon MUST encounter them
        trapDays: Seq[MissionDays] = (0 to originToTrap).toArray.toSeq.map(_.refine)
      yield (
        galaxy(originToSafe, safeToDestination, originToTrap, trapToDestination),
        imperialData(countDown, trapDays),
        autonomy
      )
    ): (galaxy: Galaxy, imperialData: ImperialData, autonomy: MissionDays) =>
      val actual = findBestOdds(galaxy, imperialData, autonomy)(origin, destination)
      assertEquals(actual, Right[MissionFailure, SuccessOdds](.9))