package com.github.fommil.outsight

// TODO: ideally this would be a DSL reader, not a trait/implementation combo
trait TransitionCalculator {

  def next(journey: Journey): Scene

}

case class SnowWhiteRules() extends TransitionCalculator with EmotivHistRestriction {

  def scene(name: String) = Scene(s"classpath:com/github/fommil/outsight/snowwhite/$name.md")

  def restriction = Set() + scene("life1") + scene("death1")

  def next(journey: Journey) =
    journey.scenes.reverse match {
      case Nil => scene("intro")
      case last :: before => last._1.id match {
        case "intro" => scene("dreaming-princess")
        case "dreaming-princess" => scene("life1")
        case "life1" => scene("death1")
        case "death1" => scene("kiss")
        case "kiss" => scene("queen")
        case "queen" => scene("dwarfs") // [sic]
        case "dwarfs" => scene("hunter")
        case "hunter" =>
          // TODO: decide if snow white lives or dies
          ???

        case "ending-death" => Fin
        case "ending-life" => Fin
        case _ => throw new IllegalArgumentException
      }
    }

}
