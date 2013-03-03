package com.github.fommil.outsight

trait TransitionCalculator {

  def next(journey: Journey): Scene

}

case class SnowWhiteRules() extends TransitionCalculator with EmotivHistRestriction {

  private def scene(name: String) = Scene(s"classpath:com/github/fommil/outsight/snowwhite/$name.md")

  private def nameFor(pair: (Scene, Set[Response])) = pair._1.resource.split("/").last.replace(".md", "")

  def restriction = Set() + scene("life1") + scene("death1")

  def next(journey: Journey) =
    journey.scenes.reverse match {
      case Nil => scene("intro")
      case last :: before => nameFor(last) match {
        case "intro" => scene("dreaming-princess")
        case "dreaming-princess" => scene("life1")
        case "life1" => scene("death1")
        case "death1" => scene("kiss")
        case "kiss" => scene("queen")
        case "queen" => scene("dwarfs") // [sic]
        case "dwarfs" => scene("hunter")
        case "hunter" => ??? // #5
        case "ending-death" => Fin
        case "ending-life" => Fin
        case _ => throw new IllegalArgumentException
      }
    }

}
