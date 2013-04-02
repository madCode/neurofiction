package com.github.fommil.outsight

import scala.util.Random

trait Rules {

  def start = next(Journey(), Nil)

  def next(journey: Journey, variables: Seq[Variable]): Scene

  def extract(journey: Journey): Seq[Variable]

}


class SnowWhiteRules extends Rules {

  private def scene(name: String) = Scene(s"classpath:com/github/fommil/outsight/snowwhite/$name.md")

  private val endings = scene("life1") :: scene("death1") :: Nil

  private val dynamic = ("kiss" :: "queen" :: "dwarfs" :: "hunter" :: Nil).map(scene _)

  private val extractors = EmotivHistExtractor(endings) :: Nil

  def extract(journey: Journey) = extractors.flatMap(_.variables(journey))

  // quite a lot of abstraction is possible here for #4
  // 1. simple flow
  // 2. random flow with visited exclusion
  // 3. split on Variable
  def next(journey: Journey, variables: Seq[Variable]) =
    if (journey.history.isEmpty) scene("dreaming-princess")
    else journey.history.head.scene.title match {
      case "dreaming-princess" => scene("life1")
      case "life1" => scene("death1")
      case "death1" | "kiss" | "queen" | "dwarfs" | "hunter" =>
        val seen = journey.history.map(_.scene)
        val available = dynamic.filterNot(seen.contains _)

        if (available.isEmpty) {
          variables.collect {
            case EmotivHistVariable(like) if like == endings(0) => scene("ending-life")
            case EmotivHistVariable(like) if like == endings(1) => scene("ending-death")
          }.head
        } else available(new Random().nextInt(available.length))

      case "ending-death" => Fin
      case "ending-life" => Fin
      case _ => throw new IllegalArgumentException
    }

}
