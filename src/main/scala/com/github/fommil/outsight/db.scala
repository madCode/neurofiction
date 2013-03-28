package com.github.fommil.outsight

import javax.persistence.EntityManagerFactory
import com.github.fommil.emokit.jpa.EmotivSession
import scala.collection.JavaConversions._
import com.github.fommil.emokit.Packet.Sensor
import scala.collection.immutable.TreeMap

// this would probably be best implemented with JdbcTemplate
// but I don't know how to get the DataSource from JPA
class EmotivHistogramQuery(emf: EntityManagerFactory, min: Int = 0, max: Int = 20000, bins: Int = 100) {

  def histogramFor(session: EmotivSession, sensor: Sensor) = {
    val em = emf.createEntityManager()
    try {
      val query = em.createNativeQuery(
        s"""
          |select width_bucket(${sensor.name.toLowerCase}, :min, :max, :bins),
          |       count(*)
          |from emotivdatum
          |where session_id = :session_id
          |group by 1
          |order by 1;
        """.stripMargin)
        .setParameter("session_id", session.getId)
        .setParameter("min", min)
        .setParameter("max", max)
        .setParameter("bins", bins)
      val results = query.getResultList.map{row =>
        val e = row.asInstanceOf[Array[_]]
        (e(0).asInstanceOf[Number].longValue(),
          e(1).asInstanceOf[Number].longValue())
      }
      TreeMap(results:_*)
    } finally {
      em.close()
    }
  }

  def histogramsFor(session: EmotivSession) = {
    Sensor.values().filterNot(_ == Sensor.QUALITY).par.map{sensor =>
      (sensor, histogramFor(session, sensor))
    }.toMap
  }
}
