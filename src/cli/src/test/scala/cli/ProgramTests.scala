package cli

import _root_.cats.data.EitherT
import _root_.cats.effect.IO
import _root_.io.github.iltotore.iron.*
import cli.R2D2.program
import core.computer.SuccessOdds
import core.io.TestResourceLoader
import core.model.Error
import munit.CatsEffectSuite

class ProgramTests extends CatsEffectSuite, TestResourceLoader:

  private def exampleN(n: Int, expected: Either[Error, SuccessOdds]) =
    val imperialDataInput                       = testResource(s"examples/example$n/empire.json")
    val missionParamsInput                      = testResource(s"examples/example$n/millennium-falcon.json")
    val actual: EitherT[IO, Error, SuccessOdds] = program(missionParamsInput, imperialDataInput)
    assertIO(actual.value, expected)

  test("example 1 : need to refuel => cant make it in less than 7 days"):
    exampleN(1, Left(Error.MissionFailure.UnreachablePlanet))

  // todo this does not work at all !!!

  test("example 2 : at least 2 encounters with bounty hunters => 0.81"):
    exampleN(2, Right(0.81))

  test("example 3 : 1 encounter with bounty hunters => 0.9"):
    exampleN(3, Right(0.9))

  test("example 4 : MF can avoid all bounty hunters => 1.0"):
    exampleN(4, Right(1.0))
