package com.github.fommil.outsight

import com.github.fommil.emokit.{Packet, Emotiv, EmotivListener}
import com.github.fommil.jpa.CrudDao
import com.github.fommil.emokit.jpa.{EmotivSession, EmotivJpaController}
import akka.contrib.jul.JavaLogging
import java.util.UUID

// Finite state machine {stopped, started}.
// A started state can be restarted and polled for session responses.
//
// https://github.com/fommil/outsight/issues/31
//
// We are essentially wrapping the mutable Java API for the EEG device.
//
// We could potentially use this to give subtle visual feedback
// (e.g. a broken connection symbol in the corner) when we have problems.
object Eeg extends EmotivListener with JavaLogging {

  private val emf = CrudDao.createEntityManagerFactory("OutsightPU")
  private val db = new EmotivJpaController(emf)

  private var stopped = true
  private var emotiv: Emotiv = null
  private var sitting: UUID = null
  private var subject: String = null

  def restart() {
    this.synchronized {
      require(!stopped)

      emotiv = new Emotiv
      emotiv.addEmotivListener(db)
      emotiv.start()
    }
  }

  def start(subject: String) {
    this.synchronized {
      require(stopped)

      sitting = UUID.randomUUID()
      this.subject = subject
      stopped = false
      restart()
      db.setSession(new EmotivSession)
      db.setRecording(true)
    }
  }

  def stop() {
    this.synchronized {
      stopped = true
      db.setRecording(false)
      emotiv.removeEmotivListener(db)
      emotiv.close()
    }
  }

  def receivePacket(p: Packet) {}

  def connectionBroken() {
    this.synchronized {
      if (stopped) return

      log.info("Emotiv connection was broken, trying to reconnect")
      emotiv.removeEmotivListener(db)
      restart()
    }
  }

  def response(journey: Journey, scene: Scene) = this.synchronized {
    require(!stopped)

    val session = db.getSession
    session.setName(scene.toString)
    session.setNotes(journey.toString)
    session.setSitting(sitting)
    session.setSubject(subject)
    db.updateSession(session)

    val newSession = new EmotivSession
    db.setSession(newSession)

    EmotivResponse(session)
  }

}

