package core.model

sealed trait Error(message: String) extends Exception:
  override def getMessage: String = message

object Error:
  sealed trait MissionFailure extends Error
  object MissionFailure:
    object UnreachablePlanet      extends Error("Planet is unreachable"), MissionFailure
    object MissionAlreadyFailed   extends Error("Mission already failed"), MissionFailure
    object MilleniumFalconCantFly extends Error("Millenium Falcon can't fly"), MissionFailure

  sealed trait IOFailure extends Error
  object IOFailure:
    final case class InvalidGalacticMap(inner: String) extends Error(s"Invalid routes database : $inner"), IOFailure
