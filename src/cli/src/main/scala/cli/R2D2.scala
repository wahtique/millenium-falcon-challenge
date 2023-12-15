package cli

import cats.data.EitherT
import cats.effect.*
import cats.implicits.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import core.BuildInfo
import core.computer.Pathfinder
import core.io.GalacticMapLoader
import core.io.ImperialDataLoader
import core.io.MissionParametersLoader
import core.model.Error.IOFailure
import java.nio.file.Path

object R2D2
    extends CommandIOApp(
      name = "r2d2",
      header = "A simple CLI to find the best odds for the Millenium Falcon to complete a mission.",
      version = BuildInfo.version
    ):

  override def main: Opts[IO[ExitCode]] =
    val milleniumFalconPathArg =
      Opts.argument[Path](metavar = "MISSION_PARAMETERS").withDefault(Path.of("millennium-falcon.json"))
    val empirePathArg =
      Opts.argument[Path](metavar = "STOLEN_IMPERIAL_DATA").withDefault(Path.of("empire.json"))

    (milleniumFalconPathArg, empirePathArg).mapN: (missionParamsInput, imperialDataInput) =>
      program(missionParamsInput, imperialDataInput).foldF(
        e => IO.println(s"Error: $e").as(ExitCode.Error),
        v => IO.println(s"Success: $v").as(ExitCode.Success)
      )

  def program(missionParamsInput: Path, imperialDataInput: Path) =
    for
      missionParameters <- MissionParametersLoader
        .make(missionParamsInput)
        .mapK(EitherT.liftK[IO, IOFailure])
        .use(_.load)
      galaxy <- GalacticMapLoader.make(missionParameters.routes_db).mapK(EitherT.liftK[IO, IOFailure]).use(_.load)
      imperialData <- ImperialDataLoader
        .make(imperialDataInput)
        .mapK(EitherT.liftK[IO, IOFailure])
        .use(_.load)
      odds <- EitherT.fromEither(Pathfinder.findBestOdds(galaxy, imperialData, missionParameters))
    yield odds
