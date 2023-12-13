package core.model

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

type MissionDays = Int :| GreaterEqual[0]

/** Imperial data intercepted by the rebels.
  *
  * @param countDown
  *   number of days before the Death Star annihilates Endor
  * @param bountyHunters
  *   list of all locations where Bounty Hunter are scheduled to be present
  */
final case class ImperialData(countDown: MissionDays, bountyHunters: Set[BountyHuntersLocation]):
  def isBountyHunterPresent(planet: Planet, day: MissionDays): Boolean =
    bountyHunters.exists(bhl => bhl.planet == planet && bhl.day == day)

/** Bounty hunter location and time of presence
  *
  * @param planet
  *   the planet where the bounty hunters will be present
  * @param day
  *   day the bounty hunters are on the planet. 0 represents the first day of the mission, i.e. today
  */
final case class BountyHuntersLocation(planet: Planet, day: MissionDays)
