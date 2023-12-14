package core.io

import cats.syntax.either.*
import ciris.*
import ciris.circe.*
import com.github.dwickern.macros.NameOf.*
import core.model.MissionDays
import core.model.Planet
import io.circe.Decoder
import java.nio.file.Path
import scala.util.Try

final case class MissionParameters(
    autonomy: MissionDays,
    departure: Planet,
    arrival: Planet,
    routes_db: Path
)

object MissionParameters:
  given Decoder[Planet]      = Decoder.decodeString.emap(Planet.either)
  given Decoder[MissionDays] = Decoder.decodeInt.emap(MissionDays.either)
  given Decoder[Path]        = Decoder.decodeString.emap(s => Try(Path.of(s)).toEither.leftMap(_.getMessage))
  given Decoder[MissionParameters] = Decoder.forProduct4(
    nameOf[MissionParameters](_.autonomy),
    nameOf[MissionParameters](_.departure),
    nameOf[MissionParameters](_.arrival),
    nameOf[MissionParameters](_.routes_db)
  )(MissionParameters.apply)
  given ConfigDecoder[String, MissionParameters] = circeConfigDecoder(nameOfType[MissionParameters])

  def load(path: Path): ConfigValue[Effect, MissionParameters] =
    file(path)
      .as[MissionParameters]
      .map: mp =>
        // correct the path to the routes database
        mp.copy(routes_db = path.getParent().resolve(mp.routes_db))
