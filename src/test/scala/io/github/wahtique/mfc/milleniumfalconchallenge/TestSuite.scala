package io.github.wahtique.mfc
package milleniumfalconchallenge

export org.scalacheck.Arbitrary
export org.scalacheck.Cogen
export org.scalacheck.Gen

trait TestSuite extends munit.DisciplineSuite, Expectations
