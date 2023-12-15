package core.io

import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.either.*
import ciris.*
import ciris.circe.*
import com.github.dwickern.macros.NameOf.*
import core.model.Error.IOFailure
import core.model.MissionDays
import core.model.MissionDays.given
import core.model.MissionParameters
import core.model.Planet
import io.circe.Decoder
import java.nio.file.Path
import scala.util.Try

trait MissionParametersLoader:
  def load: EitherT[IO, IOFailure, MissionParameters]

object MissionParametersLoader:
  given Decoder[Path] = Decoder.decodeString.emap(s => Try(Path.of(s)).toEither.leftMap(_.getMessage))
  given Decoder[MissionParameters] = Decoder.forProduct4(
    nameOf[MissionParameters](_.autonomy),
    nameOf[MissionParameters](_.departure),
    nameOf[MissionParameters](_.arrival),
    nameOf[MissionParameters](_.routes_db)
  )(MissionParameters.apply)
  given ConfigDecoder[String, MissionParameters] = circeConfigDecoder(nameOfType[MissionParameters])

  def make(path: Path): Resource[IO, MissionParametersLoader] = Resource.pure[IO, MissionParametersLoader]:
    new MissionParametersLoader:
      def load: EitherT[IO, IOFailure, MissionParameters] =
        EitherT:
          file(path)
            .as[MissionParameters]
            .map: mp =>
              // correct the path to the routes database
              mp.copy(routes_db = path.getParent().resolve(mp.routes_db))
            .attempt[IO]
        .leftMap(e => IOFailure.InvalidMissionParameters(e.messages.toString))
