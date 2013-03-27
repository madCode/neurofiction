package com.github.fommil.outsight

import com.github.fommil.emokit.{Packet, Emotiv, EmotivListener}
import com.github.fommil.jpa.CrudDao
import com.github.fommil.emokit.jpa.{EmotivSession, EmotivJpaController}
import akka.contrib.jul.JavaLogging
import java.util.UUID
import com.codeminders.hidapi.HIDDeviceNotFoundException
import java.io.IOException

// Finite state machine {stopped, started}.
// A started state can be restarted.
// Only a started state will have data associated to `response`.
//
// https://github.com/fommil/outsight/issues/31
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

      if (emotiv != null) stop()

      try {
        emotiv = new Emotiv
        emotiv.addEmotivListener(db)
        emotiv.start()
      } catch {
        case t: HIDDeviceNotFoundException =>
          log.error(t, "Starting Emotiv")
          stop()
      }
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

      if (emotiv == null) return

      emotiv.removeEmotivListener(db)
      try emotiv.close()
      catch {
        case t: IOException => log.error(t, "Closing Emotiv")
      }
      emotiv = null
    }
  }

  def receivePacket(p: Packet) {}

  def connectionBroken() {
    this.synchronized {
      if (stopped) return

      log.info("Emotiv connection was broken, trying to reconnect")
      if (emotiv != null) emotiv.removeEmotivListener(db)
      Thread.sleep(1000) // limits busy waiting when the device is off
      restart()
    }
  }

  def response(journey: Journey, scene: Scene) = this.synchronized {
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

  def reassign(response: EmotivResponse) {
    this.synchronized {
      db.setSession(response.session)
    }
  }

}
