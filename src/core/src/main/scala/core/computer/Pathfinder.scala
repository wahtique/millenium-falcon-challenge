package core.computer

import cats.kernel.Order
import cats.syntax.order.*
import core.computer.Pathfinder.Trajectory.Action
import core.model.*
import core.model.Error.MissionFailure
import io.github.iltotore.iron.*

object Pathfinder:

  def findBestOddsStrict(
      galaxy: Galaxy,
      imperialData: ImperialData,
      missionParameters: MissionParameters
  ): SuccessOdds =
    findBestOdds(galaxy, imperialData, missionParameters).getOrElse(0.toDouble.assume)

  def findBestOdds(
      galaxy: Galaxy,
      imperialData: ImperialData,
      missionParameters: MissionParameters
  ): Either[MissionFailure, SuccessOdds] =
    val bestTrajetoryOrError = findBestTrajectory(galaxy, imperialData, missionParameters)
    val oddsOrError: Either[MissionFailure, SuccessOdds] = bestTrajetoryOrError.map: t =>
      if t.head == missionParameters.arrival then SuccessOdds.compute(t.encounters)
      else 0.toDouble.assume
    oddsOrError

  def findBestTrajectory(
      galaxy: Galaxy,
      imperialData: ImperialData,
      missionParameters: MissionParameters
  ): Either[MissionFailure, Trajectory] =
    val origin      = missionParameters.departure
    val destination = missionParameters.arrival
    val autonomy    = missionParameters.autonomy
    // some trivial cases
    // if either the origin or destination is unreachable, the odds are 0
    if !(galaxy.contains(origin) && galaxy.contains(destination)) then Left(MissionFailure.UnreachablePlanet)
    // if the origin is the destination then the mission is already complete
    else if origin == destination then
      Right(
        Trajectory(origin, Seq.empty, Trajectory.Action.Noop, autonomy, 0, 0)
      )
    // if the origin is not the same as the destination and the count down is 0 then the mission is already failed
    else if imperialData.countdown == 0 then Left(MissionFailure.MissionAlreadyFailed)
    // if the millenium falcon can't fly then the mission will fail
    else if autonomy == 0 then Left(MissionFailure.MilleniumFalconCantFly)
    else
      @tailrec
      def explore(trajectories: Seq[Trajectory]): Option[Trajectory] =
        trajectories.minOption(using Trajectory.ordering) match
          case None => None // no more trajectories to explore => mission failed
          case Some(trajectory) =>
            val pivot = trajectory.head
            if pivot == destination then Some(trajectory)
            else
              // todo maybe optimize even further by trimming all other Travels where head == pivot
              val newTrajectories  = expand(galaxy, imperialData, autonomy)(trajectory)
              val nextTrajectories = newTrajectories ++ trajectories.filterNot(_ == trajectory)
              explore(nextTrajectories)
      val firstTrajectory = Trajectory(
        head = origin,
        tail = Seq.empty,
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
      // avoid back and forth
      .filter: r =>
        trajectory.tail.toList match
          case previous :: _ if trajectory.latestAction == Action.Travel =>
            r.origin != previous.head && r.destination != previous.head
          case _ => true
      .map: r =>
        trajectory.next(r, imperialData)
    // waiting multiple days seem unlikely to be a good strategy
    // ? I might be wrong tho : this is a heuristics deduced from
    val refuel = if trajectory.latestAction == Action.Refuel then None else trajectory.refuel(imperialData, maxAutonomy)
    (travels + refuel)
      .collect:
        case Some(t) => t
      .toSeq

  final case class Trajectory(
      head: Planet,
      tail: Seq[Trajectory],
      latestAction: Trajectory.Action,
      remainingFuel: MissionDays,
      daysIntoMission: MissionDays,
      encounters: EncounterCount
  ):
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
      val wouldFailMission       = daysIntoMissionAfter > imperialData.countdown
      val isRouteConnectedToHead = route.origin == head || route.destination == head
      if !hasEnoughFuel || wouldFailMission || !isRouteConnectedToHead then None
      else
        val nextPlanet = if route.origin == head then route.destination else route.origin
        Some(
          Trajectory(
            head = nextPlanet,
            tail = this +: tail,
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
      val wouldFailMission           = daysIntoMissionAfter >= imperialData.countdown
      val isBountyHunterPresentAfter = imperialData.isBountyHunterPresent(head, daysIntoMissionAfter)
      if wouldFailMission then None
      else
        Some(
          Trajectory(
            head = head,
            tail = this +: tail, // add again the current position
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
      given Order[Action] = Order.fromOrdering

    // simple heuristic to determine the best path
    // the best path is the one with the less encounters as they affect the success rate of the mission
    // then the path which favorise traveling instead of refueling ( avoid infinite refuelin loops )
    // then the path with the less days into the mission as it gives more time to complete the mission
    given ordering: Ordering[Trajectory] =
      Ordering.by[Trajectory, (Int, Action, Int)](gp => (gp.encounters, gp.latestAction, gp.daysIntoMission))

    // same as above for cats ecosystem
    given Order[Trajectory] = Order.fromOrdering
