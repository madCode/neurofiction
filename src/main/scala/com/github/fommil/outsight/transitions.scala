package com.github.fommil.outsight

// TODO: ideally this would be a DSL reader, not a trait/implementation combo
trait TransitionCalculator {

  def next(journey: Journey): Scene

}

class SnowWhiteTransitions extends TransitionCalculator {

  def next(journey: Journey) = {


  }

}
