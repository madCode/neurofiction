package com.github.fommil.outsight

import org.specs2.mutable.Specification


class SnowWhiteSpecs extends Specification {

  "The chapters" should {
    "appear in the correct order" in {

      val rules = new SnowWhiteRules
      def next(journey: Journey) = journey + rules.next(journey, Nil)

      val chapter1 = next(Journey())
      val chapter2 = next(chapter1)
      val chapter3 = next(chapter2)
      val chapter4 = next(chapter3)
      val chapter5 = next(chapter4)
      val chapter6 = next(chapter5)
      val chapter7 = next(chapter6)
      val chapter8 = next(chapter7)

      chapter1.history.head.scene.title === "dreaming-princess"
      chapter2.history.head.scene.title === "life1"
      chapter3.history.head.scene.title must beOneOf("death1", "kiss", "queen", "dwarfs", "hunter")
      chapter4.history.head.scene.title must beOneOf("death1", "kiss", "queen", "dwarfs", "hunter")
      chapter5.history.head.scene.title must beOneOf("death1", "kiss", "queen", "dwarfs", "hunter")
      chapter6.history.head.scene.title must beOneOf("death1", "kiss", "queen", "dwarfs", "hunter")
      chapter7.history.head.scene.title must beOneOf("death1", "kiss", "queen", "dwarfs", "hunter")
      chapter8.history.head.scene.title must beOneOf("ending-death", "ending-life")
    }
  }

  "Snow White" should {
     "live" in {
       todo
     }
    "die" in {
      todo
    }
  }
}
