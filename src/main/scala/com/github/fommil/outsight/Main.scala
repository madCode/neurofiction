package com.github.fommil.outsight

import akka.contrib.jul.JavaLogging
import com.github.fommil.swing.SwingConvenience.fullscreen
import com.github.fommil.jpa.CrudDao
import com.github.fommil.emokit.jpa.{EmotivSession, EmotivJpaController}
import java.util.UUID
import javax.swing.UIManager
import java.awt.Color

object Main extends App with JavaLogging {

  @volatile var sitting: UUID = null
  @volatile var subject: String = null

  UIManager.put("Panel.background",  Color.WHITE)

  val rules = new SnowWhiteRules

  val emf = CrudDao.createEntityManagerFactory("OutsightPU")
  val db = new EmotivJpaController(emf)

  val intro = new IntroductionView(introduced)
  val view = new StoryView(cutscene, back)

  val eeg = new ResilientEmotiv(db, intro)
  eeg.start()

  val frame = new OutsightFrame
  fullscreen(frame)
  introduce()


  def introduce() {
    db.setRecording(false)
    intro.reset()
    frame.setCentre(intro)
    intro.next()
  }

  def introduced(subject: String) {
    this.subject = subject
    this.sitting = UUID.randomUUID()
    view.setModel(Journey(), rules.start)
    db.setSession(new EmotivSession)
    db.setRecording(true)
    frame.setCentre(view)
  }

  def cutscene(journey: Journey, scene: Scene) {
    val current = journey +(scene, response(journey, scene))
    val variables = rules.extract(current)
    rules.next(current, variables) match {
      case Fin => introduce()
      case next => view.setModel(current, next, variables)
    }
  }

  def back(journey: Journey, variables: Seq[Variable]) {
    journey.history match {
      case x :: xs =>
        val last = x.responses.collect {
          case r: EmotivResponse => r
        }.head
        db.setSession(last.session)
        // we retain the current variables, which could result
        // in temporal weirdness for some stories.
        view.setModel(Journey(xs), x.scene, variables)
      case _ =>
        introduce()
    }
  }

  def response(journey: Journey, scene: Scene) = {
    val session = db.getSession
    session.setName(scene.toString)
    session.setNotes(journey.toString)
    session.setSitting(sitting)
    session.setSubject(subject)
    db.updateSession(session)

    val newSession = new EmotivSession
    db.createSession(newSession)

    EmotivResponse(session)
  }

}
