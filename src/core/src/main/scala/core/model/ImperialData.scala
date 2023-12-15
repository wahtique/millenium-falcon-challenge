package core.model

import com.github.dwickern.macros.NameOf.*
import io.circe.Decoder
import io.circe.Encoder
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

type Positive    = GreaterEqual[0]
type MissionDays = Int :| Positive
object MissionDays extends RefinedTypeOps[Int, Positive, MissionDays]:
  given decoder: Decoder[MissionDays] = Decoder.decodeInt.emap(MissionDays.either)
  given encoder: Encoder[MissionDays] = Encoder.encodeInt.contramap(_.value)

/** Imperial data intercepted by the rebels.
  *
  * @param countDown
  *   number of days before the Death Star annihilates Endor
  * @param bountyHunters
  *   list of all locations where Bounty Hunter are scheduled to be present
  */
final case class ImperialData(countdown: MissionDays, bountyHunters: Set[BountyHuntersLocation]):
  def isBountyHunterPresent(planet: Planet, day: MissionDays): Boolean =
    bountyHunters.exists(bhl => bhl.planet == planet && bhl.day == day)

object ImperialData:
  given Decoder[ImperialData] =
    Decoder.forProduct2(nameOf[ImperialData](_.countdown), "bounty_hunters")(
      ImperialData.apply
    )(using MissionDays.decoder, Decoder.decodeSet(BountyHuntersLocation.decoder))

  given Encoder[ImperialData] = Encoder.forProduct2(
    nameOf[ImperialData](_.countdown),
    "bounty_hunters"
  )((id: ImperialData) => (id.countdown, id.bountyHunters))(using
    MissionDays.encoder,
    Encoder.encodeSet(BountyHuntersLocation.encoder)
  )

// given Decoder[ImperialData] = deriveDecoder[ImperialData]

/** Bounty hunter location and time of presence
  *
  * @param planet
  *   the planet where the bounty hunters will be present
  * @param day
  *   day the bounty hunters are on the planet. 0 represents the first day of the mission, i.e. today
  */

final case class BountyHuntersLocation(planet: Planet, day: MissionDays)
object BountyHuntersLocation:

  given decoder: Decoder[BountyHuntersLocation] =
    Decoder.forProduct2(nameOf[BountyHuntersLocation](_.planet), nameOf[BountyHuntersLocation](_.day))(
      BountyHuntersLocation.apply
    )(using Planet.decoder, MissionDays.decoder)

  given encoder: Encoder[BountyHuntersLocation] = Encoder.forProduct2(
    nameOf[BountyHuntersLocation](_.planet),
    nameOf[BountyHuntersLocation](_.day)
  )((bhl: BountyHuntersLocation) => (bhl.planet, bhl.day))(using Planet.encoder, MissionDays.encoder)
