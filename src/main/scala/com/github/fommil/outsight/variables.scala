package com.github.fommil.outsight

import com.github.fommil.emokit.jpa.EmotivSession
import scala.util.Random
import com.github.fommil.emokit.Packet.Sensor


/** For calculating transitions between `Scene`s, and may be shown to the audience. */
trait Variable

/** Story-specific variable extraction. */
trait VariableExtractor {

  /** Extract variables for the user's journey thus far
    * using the domain specific rules of the implementation.
    *
    * Will be combined with other `Variable`s obtained from
    * other implementations after every `Scene`.
    */
  def variables(journey: Journey): List[Variable]
}


case class EmotivResponse(session: EmotivSession) extends Response

case class EmotivHistVariable(similar: Scene) extends Variable

// a machine learning classifier that finds the
// most similar previous Scene for a sample Scene.
case class EmotivHistExtractor(restriction: Seq[Scene]) extends VariableExtractor {

  protected def classify(data: Map[Scene, Histograms], sample: Histograms): Scene = {
    data.toList(new Random().nextInt(data.size))._1
  }


  def variables(journey: Journey): List[Variable] = {
    val (labelled, unlabelled) = journey.history.partition(h => restriction.contains(h.scene))
    if (labelled.isEmpty || unlabelled.isEmpty) return Nil

    // this is so bad... accessing global variables...
    val analytics = new EmotivHistogramQuery(Main.emf)
    val data = labelled.map{h => (h.scene, analytics.histogramsFor(h.responseT[EmotivResponse].head.session))}.toMap
    val sample = analytics.histogramsFor(unlabelled.head.responseT[EmotivResponse].head.session)

    val classified = classify(data, sample)

    EmotivHistVariable(classified) :: Nil
  }
}
