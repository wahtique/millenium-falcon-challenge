package core.computer

import io.github.iltotore.iron.*
import munit.FunSuite

class SuccessOddsTests extends FunSuite:

  // chances of capture

  test("no encounters => 0% capture chances"):
    assertEquals(SuccessOdds.captureChances(0), 0.0)

  test("1 encounter => 10% capture chances"):
    assertEquals(SuccessOdds.captureChances(1), .1)

  // success odds

  test("no encounters => 100% success odds"):
    assertEquals(SuccessOdds.compute(0), 1.0)

  test("1 encounter => 90% success odds"):
    assertEquals(SuccessOdds.compute(1), .9)

  test("2 encounters => 81% success odds"):
    assertEquals(SuccessOdds.compute(2), .81)

  test("3 encounters => ~ 73% success odds"):
    assertEquals(SuccessOdds.compute(3), .729)
