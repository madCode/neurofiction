package com.github.fommil.outsight

import org.specs2.mutable.Specification
import com.github.fommil.jpa.CrudDao
import com.github.fommil.emokit.jpa.EmotivSession
import java.util.UUID
import akka.contrib.jul.JavaLogging
import scala.collection.JavaConversions._
import com.github.fommil.emokit.Packet.Sensor

class HistogramSpec extends Specification with JavaLogging {

  "the native query" should {
    "return results for a known session" in {
      // select encode(session_id, 'hex')::uuid, count(*) From emotivdatum group by session_id;
      val emf = CrudDao.createEntityManagerFactory("OutsightPU")
      val analytics = new EmotivHistogramQuery(emf)

      val session = new EmotivSession
      session.setId(UUID.fromString("d5389f47-6600-4659-97a2-085c6ac9ace9"))
      val histogram = analytics.histogramFor(session, Sensor.AF3)

      val start = System.currentTimeMillis()
      val histograms = analytics.histogramsFor(session)
      val end = System.currentTimeMillis()
      log.info(histograms.toString)
      log.info(s"took ${end - start}")

      // if we got here without I/O problems, it's probably all good
      success

    }
  }
}
