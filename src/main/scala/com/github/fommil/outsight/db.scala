package com.github.fommil.outsight

import javax.persistence.EntityManagerFactory
import com.github.fommil.emokit.jpa.EmotivSession
import scala.collection.JavaConversions._
import com.github.fommil.emokit.Packet.Sensor

// this would probably be best implemented with JdbcTemplate
// but I don't know how to get the DataSource from JPA
class EmotivHistogramQuery(emf: EntityManagerFactory) {

  def histogramFor(session: EmotivSession, sensor: Sensor,
                   min: Int = 0, max: Int = 20000, bins: Int = 100) = {
    val em = emf.createEntityManager()
    try {
      val query = em.createNativeQuery(
        s"""
          |select session_id,
          |       width_bucket(${sensor.name.toLowerCase}, :min, :max, :bins),
          |       count(*)
          |from emotivdatum
          |where session_id = :session_id
          |group by 1, 2
          |order by 2;
        """.stripMargin)
        .setParameter("session_id", session.getId)
        .setParameter("min", min)
        .setParameter("max", max)
        .setParameter("bins", bins)
      query.getResultList.map{row =>
        val e = row.asInstanceOf[Array[_]]
        (e(1).asInstanceOf[Number].longValue(), e(2).asInstanceOf[Number].longValue())
      }
    } finally {
      em.close()
    }
  }
}
