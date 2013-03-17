package com.github.fommil.outsight

import scala.util.Random

trait TransitionCalculator {

  def next(journey: Journey): (Journey, Scene)

}

case class SnowWhiteRules() extends TransitionCalculator with EmotivHistRestriction {

  private def scene(name: String) = Scene(s"classpath:com/github/fommil/outsight/snowwhite/$name.md")

  private def nameFor(pair: (Scene, Set[Response])) = pair._1.resource.split("/").last.replace(".md", "")

  def restriction = Set() + scene("life1") + scene("death1")

  private val dynamic = ("kiss" :: "queen" :: "dwarfs" :: "hunter" :: Nil).map(scene _)

  def next(journey: Journey) =
    journey.scenes.reverse match {
      case Nil => (journey, scene("dreaming-princess"))
      case last :: before => nameFor(last) match {
        case "dreaming-princess" => (journey, scene("life1"))
        case "life1" => (journey, scene("death1"))
        case "death1" | "kiss" | "queen" | "dwarfs" | "hunter" =>
          val seen = journey.scenes.map(_._1)
          val available = dynamic.filterNot(seen.contains _)

          val classifier = EmotivHistExtractor(this)
          val variables = classifier.variables(journey)
          val update = journey.copy(variables = variables)

          (update,
            if (available.isEmpty) scene("ending-death")
            else available(new Random().nextInt(available.length))
          )

        case "ending-death" => (journey, Fin)
        case "ending-life" => (journey, Fin)
        case _ => throw new IllegalArgumentException
      }
    }

}
