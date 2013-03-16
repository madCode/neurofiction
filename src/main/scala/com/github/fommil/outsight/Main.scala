package com.github.fommil.outsight

import java.awt.GraphicsEnvironment
import akka.contrib.jul.JavaLogging

object Main extends App with JavaLogging {

  val rules = SnowWhiteRules()
  val story = Story(Set(EmotivHistExtractor(rules)), rules)
  var journey = Journey(Nil, Nil)

  val view = new StoryView
  view.callback = {scene: Scene =>
    val latest = (scene, Set[Response]())
    journey = journey.copy(scenes = journey.scenes ::: latest :: Nil)
    val next = story.transitions.next(journey)
    view.update(next)
  }
  view.update(story.transitions.next(journey))
  GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.setFullScreenWindow(view)


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
