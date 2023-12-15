package backend.service

import cats.effect.IO
import cats.effect.kernel.Resource
import core.computer.Pathfinder
import core.model.Galaxy
import core.model.ImperialData
import core.model.MissionParameters

trait NavigationService:
  def computeOdds(imperialData: ImperialData): Double

object NavigationService:
  def make(galaxy: Galaxy, missionParameters: MissionParameters): Resource[IO, NavigationService] =
    Resource.pure:
      new NavigationService:
        def computeOdds(imperialData: ImperialData): Double =
          Pathfinder.findBestOdds(galaxy, imperialData, missionParameters).map(_ * 100).getOrElse(0.toDouble)
