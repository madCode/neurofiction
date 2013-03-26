package com.github.fommil.outsight

import akka.contrib.jul.JavaLogging
import com.github.fommil.swing.SwingConvenience.fullscreen
import com.github.fommil.emokit.Emotiv
import com.github.fommil.emokit.jpa.{EmotivJpaController, EmotivSession}
import com.github.fommil.jpa.CrudDao
import java.util.UUID

object Main extends App with JavaLogging {

  val emf = CrudDao.createEntityManagerFactory("OutsightPU")

  val sitting = UUID.randomUUID()
  val subject = "Test Subject" // TODO #9

  val db = new EmotivJpaController(emf)
  val rules: Rules = new SnowWhiteRules
  val view = new StoryView(cut, rules.start)

  startEmotivRecording()
  fullscreen(view)

  def cut(journey: Journey, scene: Scene) {
    val session = getAndRecreateSession(journey, scene)
    // could be abstracted for multiple response types
    val current = journey.add(scene, EmotivResponse(session))
    val variables = rules.extract(current)
    val next = rules.next(current, variables)

    view.updateModel(current, next, variables)
  }

  def getAndRecreateSession(journey: Journey, scene: Scene) = {
    val session = db.getSession
    session.setName(scene.toString)
    session.setNotes(journey.toString)
    session.setSitting(sitting)
    session.setSubject(subject)
    db.updateSession(session)

    val newSession = new EmotivSession
    db.setSession(newSession)

    session
  }

  def startEmotivRecording() {
    val emotiv = new Emotiv
    emotiv.addEmotivListener(db)
    emotiv.start()
    db.setSession(new EmotivSession)
    db.setRecording(true)
  }
}
