package com.github.fommil.outsight

import com.github.fommil.emokit.{Packet, Emotiv, EmotivListener}
import akka.contrib.jul.JavaLogging
import com.codeminders.hidapi.HIDDeviceNotFoundException
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConversions
import JavaConversions._


// Finite state machine {stopped, started} that wraps the EmotivListener
// interface such that a connectionBroken signal will attempt to restart the Emotiv.
//
// https://github.com/fommil/outsight/issues/31
class ResilientEmotiv(listeners: EmotivListener*) extends EmotivListener with JavaLogging {

  private var stopped = true
  private var emotiv: Emotiv = null

  def start() {
    this.synchronized {
      if (!stopped) return
      try {
        emotiv = new Emotiv
        emotiv.addEmotivListener(this)
        emotiv.start()
      } catch {
        case t: HIDDeviceNotFoundException =>
          log.error(t, "Starting Emotiv")
          stop()
      }
    }
  }

  def stop() {
    this.synchronized {
      if (stopped) return
      stopped = true
      emotiv.removeEmotivListener(this)
      try emotiv.close()
      catch {
        case t: IOException => log.error(t, "Closing Emotiv")
      }
      emotiv = null
    }
  }

  def receivePacket(p: Packet) {
    listeners.foreach(listener => listener.receivePacket(p))
  }

  def connectionBroken() {
    this.synchronized {
      log.info("Connection Broken")
      stop()
      Thread.sleep(1000) // minimise busy waiting
      start()
    }
    listeners.foreach(listener => listener.connectionBroken())
  }
}
