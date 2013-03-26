package com.github.fommil.outsight

import akka.contrib.jul.JavaLogging
import com.github.fommil.swing.SwingConvenience.fullscreen
import Eeg._
import javax.swing.{SwingUtilities, SwingWorker}

object Main extends App with JavaLogging {

  val subject = "Test Subject" // TODO #9

  val rules: Rules = new SnowWhiteRules
  val view = new StoryView(cutscene, back)
  view.setModel(Journey(), rules.start)

  start(subject)
  fullscreen(view)

  def cutscene(journey: Journey, scene: Scene) {
    val current = journey +(scene, response(journey, scene))
    val variables = rules.extract(current)
    val next = rules.next(current, variables)

    view.setModel(current, next, variables)
  }

  def back(journey: Journey, variables: Seq[Variable]) {
    journey.history match {
      case x :: xs =>
        Eeg.reassign(x.responses.collect{case r: EmotivResponse => r}.head)
        // we retain the current variables, which could result
        // in temporal weirdness for some stories.
        view.setModel(Journey(xs), x.scene, variables)
      case _ =>
    }
  }

}
