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
      .in("givemetheodds")
      .post
      .in(jsonBody[ImperialData])
      .out(plainBody[Double])
