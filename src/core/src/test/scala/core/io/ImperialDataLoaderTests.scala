package core.io

import cats.data.EitherT
import cats.effect.IO
import core.model.Error.IOFailure
import core.model.MissionDays
import io.github.iltotore.iron.*
import munit.CatsEffectSuite

class ImperialDataLoaderTests extends CatsEffectSuite, TestResourceLoader:

  test("load stolen imperial data => have expected countdown"):
    val file     = testResource("examples/example1/empire.json")
    val data     = ImperialDataLoader.make(file).mapK(EitherT.liftK[IO, IOFailure]).use(_.load)
    val actual   = data.map(_.countdown)
    val expected = MissionDays(7)
    assertIO(actual.value, Right(expected))
