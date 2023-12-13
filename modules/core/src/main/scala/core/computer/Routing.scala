package core.computer

import cats.data.NonEmptySeq
import cats.kernel.Order
import core.computer.Routing.Trajectory.Action
import core.model.Galaxy
import core.model.ImperialData
import core.model.MissionDays
import core.model.Planet
import core.model.Route
import io.github.iltotore.iron.*

sealed trait MissionFailure extends Exception

object MissionFailure:
  object UnreachablePlanet extends MissionFailure:
    override def getMessage(): String | Null = "Planet is unreachable"
  object MissionAlreadyFailed extends MissionFailure:
    override def getMessage(): String | Null = "Mission already failed"
  object MilleniumFalconCantFly extends MissionFailure:
    override def getMessage(): String | Null = "Millenium Falcon can't fly"

object Routing:

  def findBestOdds(galaxy: Galaxy, imperialData: ImperialData, autonomy: MissionDays)(
      origin: Planet,
      destination: Planet
  ): Either[MissionFailure, SuccessOdds] =
    val bestTrajetoryOrError = findBestTrajectory(galaxy, imperialData, autonomy)(origin, destination)
    val oddsOrError: Either[MissionFailure, SuccessOdds] = bestTrajetoryOrError.map: t =>
      if t.head == destination then SuccessOdds.compute(t.encounters)
      else 0.toDouble.assume
    oddsOrError

  // todo use config for max autonomy
  /** Find the best odds to reach a destination from an origin.
    *
    * In practice the destination would always be Endor.
    *
    * @param galaxy
    *   the galaxy to search in
    * @param imperialData
    *   bounty hunters locations
    * @param autonomy
    *   the number of days the Millenium Falcon can travel before refueling
    * @param origin
    *   the starting point
    * @param destination
    *   the destination
    * @return
    *   trajectory minimizing risk of failure, or an error
    */
  def findBestTrajectory(galaxy: Galaxy, imperialData: ImperialData, autonomy: MissionDays)(
      origin: Planet,
      destination: Planet
  ): Either[MissionFailure, Trajectory] =
    // some trivial cases
    // if either the origin or destination is unreachable, the odds are 0
    if !(galaxy.contains(origin) && galaxy.contains(destination)) then Left(MissionFailure.UnreachablePlanet)
    // if the origin is the destination then the mission is already complete
    else if origin == destination then
      Right(Trajectory(NonEmptySeq.one(origin), Trajectory.Action.Noop, autonomy, 0, 0))
    // if the origin is not the same as the destination and the count down is 0 then the mission is already failed
    else if imperialData.countDown == 0 then Left(MissionFailure.MissionAlreadyFailed)
    // if the millenium falcon can't fly then the mission will fail
    else if autonomy == 0 then Left(MissionFailure.MilleniumFalconCantFly)
    else
      // exploration loop
      @tailrec
      def explore(trajectories: Seq[Trajectory]): Option[Trajectory] =
        trajectories.minOption match
          case None => None // no more trajectories to explore => mission failed
          case Some(trajectory) =>
            val pivot = trajectory.head
            // stop condition : the destination is reached
            if pivot == destination then Some(trajectory)
            else
              // explore the next possible trajectories
              // first option is to travel
              // second option is to refuel in place
              explore(expand(galaxy, imperialData, autonomy)(trajectory) ++ trajectories.filterNot(_ == trajectory))
      // start with the origin
      val firstTrajectory = Trajectory(
        steps = NonEmptySeq.one(origin),
        latestAction = Trajectory.Action.Noop,
        remainingFuel = autonomy,
        daysIntoMission = 0,
        encounters = 0
      )
      explore(Seq(firstTrajectory)).fold(Left(MissionFailure.UnreachablePlanet))(Right(_))

  private[computer] def expand(galaxy: Galaxy, imperialData: ImperialData, maxAutonomy: MissionDays)(
      trajectory: Trajectory
  ): Seq[Trajectory] =
    val head   = trajectory.head
    val routes = galaxy.routesFrom(head)
    val travels = routes
      // avoid cycles
      .filter: r =>
        trajectory.steps match
          case NonEmptySeq(head, previous :: _) => !(r.origin == previous || r.destination == previous)
          case _                                => true
      .map: r =>
        trajectory.next(r, imperialData)
    val refuel = trajectory.refuel(imperialData, maxAutonomy)
    (travels + refuel)
      .collect:
        case Some(t) => t
      .toSeq

  final case class Trajectory(
      steps: NonEmptySeq[Planet],
      latestAction: Trajectory.Action,
      remainingFuel: MissionDays,
      daysIntoMission: MissionDays,
      encounters: EncounterCount
  ):
    /** End position of the Millenium Falcon if it follows this trajectory
      *
      * @return
      *   the last planet of the trajectory
      */
    def head: Planet = steps.head

    /** The next possible trajectory from this one if the Millenium Falcon follows the given route.
      *
      * @param route
      *   next segment of the trajectory
      * @param imperialData
      *   mission constraints
      * @return
      *   the next possible trajectory, or nothing if the mission would fail
      */
    def next(route: Route, imperialData: ImperialData): Option[Trajectory] =
      val daysIntoMissionAfter = this.daysIntoMission + route.travelTime
      val isBountyHunterPresentAfter =
        imperialData.isBountyHunterPresent(route.destination, daysIntoMissionAfter.assume)
      val hasEnoughFuel          = remainingFuel >= route.travelTime
      val wouldFailMission       = daysIntoMissionAfter >= imperialData.countDown
      val isRouteConnectedToHead = route.origin == head || route.destination == head
      if !hasEnoughFuel || wouldFailMission || !isRouteConnectedToHead then None
      else
        val nextPlanet = if route.origin == head then route.destination else route.origin
        Some(
          Trajectory(
            steps = nextPlanet +: steps, // prepend so that head is always the current position
            latestAction = Trajectory.Action.Travel,
            remainingFuel = (remainingFuel - route.travelTime).assume,     // safe because of the check above
            daysIntoMission = (daysIntoMission + route.travelTime).assume, // safe because of additivity and check above
            encounters = (encounters + (if isBountyHunterPresentAfter then 1 else 0)).assume // always positive
          )
        )

    /** The next possible trajectory from this one if the Millenium Falcon refuels.
      *
      * Basically like the previous method but this one add fuel instead of consuming it.
      *
      * @param imperialData
      *   mission constraints
      * @param maxAutonomy
      *   fuel levevel after refueling
      * @return
      *   the next possible trajectory, or nothing if the mission would fail
      */
    def refuel(imperialData: ImperialData, maxAutonomy: MissionDays): Option[Trajectory] =
      val daysIntoMissionAfter       = (daysIntoMission + 1).assume
      val wouldFailMission           = daysIntoMissionAfter >= imperialData.countDown
      val isBountyHunterPresentAfter = imperialData.isBountyHunterPresent(head, daysIntoMissionAfter)
      if wouldFailMission then None
      else
        Some(
          Trajectory(
            steps = steps, // add again the current position
            latestAction = Trajectory.Action.Refuel,
            remainingFuel = maxAutonomy,                    // safe because of the check above
            daysIntoMission = (daysIntoMission + 1).assume, // safe because of additivity and check above
            encounters = (encounters + (if isBountyHunterPresentAfter then 1 else 0)).assume // always positive
          )
        )

  object Trajectory:
    enum Action:
      case Travel, Refuel, Noop
    object Action:
      given Ordering[Action] with
        // priority should be given to travel instead of refueling
        def compare(x: Action, y: Action): Int = (x, y) match
          case (Noop, _)        => -1
          case (_, Noop)        => 1
          case (Travel, Refuel) => -1
          case (Refuel, Travel) => 1
          case _                => 0

    // simple heuristic to determine the best path
    // the best path is the one with the less encounters as they affect the success rate of the mission
    // then the path which favorise traveling instead of refueling ( avoid infinite refuelin loops )
    // then the path with the less days into the mission as it gives more time to complete the mission
    given Ordering[Trajectory] =
      Ordering.by[Trajectory, (Int, Action, Int)](gp => (gp.encounters, gp.latestAction, gp.daysIntoMission))

    // same as above for cats ecosystem
    given Order[Trajectory] = Order.fromOrdering
