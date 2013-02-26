package com.github.fommil.outsight

// TODO: ideally this would be a DSL reader, not a trait/implementation combo
trait TransitionCalculator {

  def next(journey: Journey): Scene

}

class SnowWhiteTransitions extends TransitionCalculator {

  private def scene(name: String) = Scene(s"classpath:com/github/fommil/outsight/snowwhite/$name.md}")

  def next(journey: Journey) =
    if (journey.scenes.isEmpty) scene("intro")
    else journey.scenes.last._1.id match {
      case "intro" => scene("dreaming-princess")
      case "dreaming-princess" => scene("life1")
      case "life1" => scene("death1")
      case "death1" => scene("kiss")
      case "kiss" => scene("queen")
      case "queen" => scene("dwarfs") // [sic]
      case "dwarfs" => scene("hunter")
      case "hunter" =>
        // TODO: decide if snow white lives or dies
        val last = journey.scenes.last
        ???

      case "ending-death" => Fin
      case "ending-life" => Fin
      case _ => throw new IllegalArgumentException
    }

}
