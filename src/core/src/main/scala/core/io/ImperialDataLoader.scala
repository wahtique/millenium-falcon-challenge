package core.io

import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.*
import ciris.circe.*
import com.github.dwickern.macros.NameOf.*
import core.model.Error.IOFailure
import core.model.ImperialData
import java.nio.file.Path

trait ImperialDataLoader:
  def load: EitherT[IO, IOFailure, ImperialData]

object ImperialDataLoader:
  given ConfigDecoder[String, ImperialData] = circeConfigDecoder(nameOfType[ImperialData])

  def make(path: Path) = Resource.pure[IO, ImperialDataLoader]:
    new ImperialDataLoader:
      def load: EitherT[IO, IOFailure, ImperialData] =
        EitherT(file(path).as[ImperialData].attempt[IO]).leftMap(e =>
          IOFailure.InvalidImperialData(e.messages.toString)
        )
