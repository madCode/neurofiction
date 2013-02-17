package com.github.fommil.outsight

import com.github.fommil.emokit.jpa.EmotivSession


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
  def variables(journey: Journey): Set[Variable]
}


case class EmotivResponse(session: EmotivSession) extends Response
case class EmotivHistVariable(sample: Scene, similar: Scene) extends Variable


// a machine learning classifier that finds the
// most similar previous Scene for a sample Scene.
class EmotivHistClassifier(restriction: List[Scene] = Nil) extends VariableExtractor {

  type Histogram = Map[Int, Double]

  protected def classify(data: List[Histogram], sample: Histogram): Histogram = ???

  def variables(journey: Journey) = ???
}
