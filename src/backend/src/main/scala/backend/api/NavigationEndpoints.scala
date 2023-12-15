package backend.api

import core.model.BountyHuntersLocation
import core.model.ImperialData
import core.model.ImperialData.given
import core.model.MissionDays
import core.model.Planet
import io.github.iltotore.iron.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

object NavigationEndpoints:

  given Schema[MissionDays]           = Schema.schemaForInt.map(MissionDays.option)(identity)
  given Schema[Planet]                = Schema.string.map(Planet.option)(_.value)
  given Schema[BountyHuntersLocation] = Schema.derived
  given Schema[ImperialData]          = Schema.derived

  val giveMeTheOdds: PublicEndpoint[ImperialData, Unit, Double, Any] =
    endpoint
      .tag("Pathfinding")
      .name("Compute Odds")
      .description("Compute the odds of success of the mission from the intelligence stolen from the Empire.")
      .in("givemetheodds")
      .post
      .in(
        jsonBody[ImperialData].description(
          """
          |Data stolen from the Empire containing the date of their attack which we MUST STOP AT ALL COST
          |and the location of bounty hunters sent to capture the crew of the Millennium Falcon.
          |""".stripMargin
        )
      )
      .out(plainBody[Double].description("The odds of success of the mission between 0 and 100."))
