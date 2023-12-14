package core.computer

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*
import spire.implicits.*

type EncounterCount = Int :| GreaterEqual[0]
type SuccessOdds    = Double :| Interval.Closed[0, 1]

object SuccessOdds:
  /** Compute the odds of success given the number of encounters with bounty hunters.
    *
    * @param encounters
    *   number of encounters with bounty hunters = number of days spent on the same planet as bounty huntersf=
    * @return
    *   odds of success in [0.0 ,1.0]
    */
  def compute(encounters: EncounterCount): SuccessOdds =
    if encounters == 0 then 1
    else (1 - captureChances(encounters)).assume // safe because maths

  private[computer] def captureChances(encounters: EncounterCount): SuccessOdds =
    (1 to encounters).foldLeft(0.toDouble)((acc, k) => acc + ((9 ** (k - 1)).toDouble / (10 ** k).toDouble)).assume
