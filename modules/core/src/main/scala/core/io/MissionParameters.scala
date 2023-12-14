package core.io

import ciris.*
import ciris.circe.*
import com.github.dwickern.macros.NameOf.*
import core.model.Planet
import io.circe.Decoder
import java.nio.file.Path

final case class MissionParameters(
    autonomy: Int,
    departure: Planet,
    destination: Planet,
    routes_db: String
)

object MissionParameters:
  given Decoder[Planet] = Decoder.decodeString.emap(Planet.either)
  given Decoder[MissionParameters] = Decoder.forProduct4(
    nameOf[MissionParameters](_.autonomy),
    nameOf[MissionParameters](_.departure),
    nameOf[MissionParameters](_.destination),
    nameOf[MissionParameters](_.routes_db)
  )(MissionParameters.apply)
  given ConfigDecoder[String, MissionParameters] = circeConfigDecoder(nameOfType[MissionParameters])

  def load(path: Path): ConfigValue[Effect, MissionParameters] = file(path).as[MissionParameters]
