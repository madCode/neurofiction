package com.github.fommil.outsight

import akka.contrib.jul.JavaLogging
import com.github.fommil.swing.SwingConvenience.fullscreen
import Eeg._

object Main extends App with JavaLogging {

  val subject = "Test Subject" // TODO #9

  val rules: Rules = new SnowWhiteRules
  val view = new StoryView(cut)
  view.setModel(Journey(), rules.start)

  start(subject)
  fullscreen(view)

  def cut(journey: Journey, scene: Scene) {
    val current = journey +(scene, response(journey, scene))
    val variables = rules.extract(current)
    val next = rules.next(current, variables)

    view.setModel(current, next, variables)
  }
}
