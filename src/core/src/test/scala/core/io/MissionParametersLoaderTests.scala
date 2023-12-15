package core.io

import cats.data.EitherT
import cats.effect.IO
import core.model.Error.IOFailure
import core.model.MissionDays
import core.model.Planet
import io.github.iltotore.iron.*
import munit.CatsEffectSuite

class MissionParametersTests extends CatsEffectSuite, TestResourceLoader:

  test("load mission parameters"):
    val file   = testResource("examples/example1/millennium-falcon.json")
    val params = MissionParametersLoader.make(file).mapK(EitherT.liftK[IO, IOFailure]).use(_.load)
    // tuple values to ignore the path as it will be replaced by the absolute path either way
    val expected = (MissionDays(6), Planet("Tatooine"), Planet("Endor"))
    val actual: EitherT[IO, IOFailure, (MissionDays, Planet, Planet)] =
      params.map(p => (p.autonomy, p.departure, p.arrival))
    assertIO(actual.value, Right(expected))
