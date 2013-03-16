package com.github.fommil.outsight

import java.awt.GraphicsEnvironment
import akka.contrib.jul.JavaLogging

object Main extends App with JavaLogging {

  val rules = SnowWhiteRules()
  val story = Story(Set(EmotivHistExtractor(rules)), rules)
  val journey = Journey(Nil, Nil)

  val scene = story.transitions.next(journey)

  val view = new StoryView
  GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.setFullScreenWindow(view)

  view.setScene(scene)

  //  val emotiv = new Emotiv()
  //  import scala.collection.JavaConversions._
  //  val session = new EmotivSession()
  //  session.setName("My Session")
  //  for (packet <- emotiv) {
  //    val datum = EmotivDatum.fromPacket(packet)
  //    datum.setSession(session)
  //    log.info(datum.toString)
  //  }


}
