package com.github.fommil.outsight

import akka.contrib.jul.JavaLogging
import com.github.fommil.swing.SwingConvenience.fullscreen
import com.github.fommil.emokit.Emotiv
import com.github.fommil.emokit.jpa.{EmotivJpaController, EmotivSession}
import scala.collection.JavaConversions._
import com.github.fommil.jpa.CrudDao

object Main extends App with JavaLogging {

  val emf = CrudDao.createEntityManagerFactory("OutsightPU")

  val emotiv = new Emotiv
  val db = new EmotivJpaController(emf)
  emotiv.addEmotivListener(db)
  emotiv.start()
  db.setSession(new EmotivSession)
  db.setRecording(true)

  val rules = SnowWhiteRules()
  val story = Story(Set(EmotivHistExtractor(rules)), rules)


  val view = new StoryView(story, cut)
  val start = story.transitions.next(Journey(Nil, Nil))
  view.update(start._1, start._2)
  fullscreen(view)

  def cut(journey: Journey, scene: Scene) {
    val session = db.getSession
    session.setName(scene.toString)
    session.setNotes(journey.toString)
    db.updateSession(session)
    val response = EmotivResponse(session)
    val latest = (scene, Set[Response]() + response)
    val update: Journey = journey.copy(
      scenes = journey.scenes :+ latest
    )

    val newSession = new EmotivSession
    newSession.setSitting(session.getSitting)
    newSession.setSubject(session.getSubject)
    db.setSession(newSession)

    val next = story.transitions.next(update)
    view.update(next._1, next._2)
  }

}
