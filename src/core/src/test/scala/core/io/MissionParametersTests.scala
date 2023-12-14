package core.io

import cats.effect.IO
import ciris.ConfigError
import core.model.MissionDays
import core.model.Planet
import io.github.iltotore.iron.*
import munit.CatsEffectSuite

class MissionParametersTests extends CatsEffectSuite, TestResourceLoader:

  test("load mission parameters"):
    val file   = testResource("examples/example1/millennium-falcon.json")
    val params = MissionParameters.load(file).attempt[IO]
    // tuple values to ignore the path as it will be replaced by the absolute path either way
    val expected = (MissionDays(6), Planet("Tatooine"), Planet("Endor"))
    val actual: IO[Either[ConfigError, (MissionDays, Planet, Planet)]] =
      params.map(_.map(p => (p.autonomy, p.departure, p.arrival)))
    assertIO(actual, Right(expected))
