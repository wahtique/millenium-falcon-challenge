package core.model

import java.nio.file.Path

final case class MissionParameters(
    autonomy: MissionDays,
    departure: Planet,
    arrival: Planet,
    routes_db: Path
)
