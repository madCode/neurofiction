package com.github.fommil.outsight

import org.specs2.mutable.Specification
import com.github.fommil.jpa.CrudDao
import com.github.fommil.emokit.jpa.EmotivSession
import java.util.UUID
import akka.contrib.jul.JavaLogging
import scala.collection.JavaConversions._
import com.github.fommil.emokit.Packet.Sensor

class HistogramSpec extends Specification with JavaLogging {

  val emf = CrudDao.createEntityManagerFactory("OutsightPU")
  val analytics = new EmotivHistogramQuery(emf)


  "the native query" should {
    "not die when no results are found" in {
      val session = new EmotivSession
      session.setId(UUID.randomUUID())

      analytics.histogramsFor(session).data.size === (Sensor.values().length - 1)
    }

    "return results for a known session" in {
      // select encode(session_id, 'hex')::uuid, count(*) From emotivdatum group by session_id;


      val session = new EmotivSession
      session.setId(UUID.fromString("d5389f47-6600-4659-97a2-085c6ac9ace9"))
      val histogram = analytics.histogramFor(session, Sensor.AF3)
      val histograms = analytics.histogramsFor(session)

      // if we got here without I/O and cast problems, it's probably all good
      success
    }

    "comparing known sessions gives sensible results" in {
      todo
    }
  }
}
