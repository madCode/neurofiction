package com.github.fommil.outsight

import javax.persistence.EntityManagerFactory
import com.github.fommil.emokit.jpa.EmotivSession
import scala.collection.JavaConversions._
import com.github.fommil.emokit.Packet.Sensor
import scala.collection.immutable.TreeMap

class Histogram(val data: Map[Long, Double]) {

  // http://jccaicedo.blogspot.co.uk/2012/01/histogram-intersection.html
  def intersection(other: Histogram) = {
    val bins = (data.keys ++ other.data.keys).toSet
    bins.map{bin=>
      freq(bin).min(other.freq(bin))
    }.sum
  }

  def freq(bin: Long) = data.getOrElse(bin, 0.0)
}

class Histograms(val data: Map[Sensor, Histogram]) {

  def distanceTo(other: Histograms) = {
    data.map {e=>
      math.pow(e._2.intersection(other.data.get(e._1).get), 2)
    }.sum
  }
}

// this would probably be best implemented with JdbcTemplate
// but I don't know how to get the DataSource from JPA
// 20,000 is higher than needed, the resolution is 14 bit = 16384
class EmotivHistogramQuery(emf: EntityManagerFactory, min: Int = 0, max: Int = 20000, bins: Int = 1000) {

  def histogramFor(session: EmotivSession, sensor: Sensor): Histogram = {
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
      val results = query.getResultList.map {
        row =>
          val e = row.asInstanceOf[Array[_]]
          (e(0).asInstanceOf[Number].longValue(),
            e(1).asInstanceOf[Number].longValue())
      }
      val total: Double = results.map(_._2).sum

      new Histogram(TreeMap(results.map {
        row =>
          (min + (row._1 - 1) * (max / bins), row._2 / total)
      }: _*
      ))
    } finally em.close()
  }

  def histogramsFor(session: EmotivSession): Histograms = {
    new Histograms(Sensor.values().filterNot(_ == Sensor.QUALITY).par.map {
      sensor =>
        (sensor, histogramFor(session, sensor))
    }.toMap.seq)
  }
}
