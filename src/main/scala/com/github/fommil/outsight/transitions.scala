package com.github.fommil.outsight

import scala.util.Random
import akka.contrib.jul.JavaLogging
import scala.annotation.tailrec

trait Rules {

  def start = next(Journey(), Nil)

  def next(journey: Journey, variables: Seq[Variable]): Scene

  def extract(journey: Journey): Seq[Variable]

}


case class SnowWhiteRules() extends Rules with JavaLogging {

  private def scene(name: String) = Scene(s"classpath:com/github/fommil/outsight/snowwhite/$name.md")

  private val (dreaming_princess,life1, death1, kiss, queen, dwarfs, hunter, ending_life, ending_death)
  = (scene("dreaming-princess"), scene("life1"), scene("death1"), scene("kiss"), scene("queen"), scene("dwarfs"), scene("hunter"), scene("ending-life"), scene("ending-death"))
  
  
  private val endings = Map(life1 -> ending_life, death1 -> ending_death)

  private val dynamic = Set(kiss, queen, dwarfs, hunter)

  private val extractors = EmotivHistExtractor(endings.keys.toList) :: Nil

  def extract(journey: Journey) = extractors.flatMap(_.variables(journey))

  // ._1 is death path, ._2 is life path
  // would have been cleaner as a map from path to this, instead of tuples
  // also, refactoring weighted random sampling would severely clean things up.
  private val weights = Map(
    kiss   -> Map(dwarfs -> (0.20, 0.60), hunter -> (0.20, 0.20), queen  -> (0.60, 0.20)),
    dwarfs -> Map(kiss   -> (0.20, 0.60), hunter -> (0.60, 0.20), queen  -> (0.20, 0.20)),
    hunter -> Map(kiss   -> (0.60, 0.20), dwarfs -> (0.20, 0.60), queen  -> (0.20, 0.20)),
    queen  -> Map(kiss   -> (0.20, 0.60), dwarfs -> (0.20, 0.20), hunter -> (0.60, 0.20))
  )

  // quite a lot of abstraction is possible here for #4
  // 1. simple flow
  // 2. random flow with visited exclusion
  // 3. split on Variable
  def next(journey: Journey, variables: Seq[Variable]) =
    if (journey.history.isEmpty) dreaming_princess
    else journey.history.head.scene match {
      case `dreaming_princess` => life1
      case `life1` => death1
      case `death1` => dynamic.toList(new Random().nextInt(dynamic.size))
      case scene if dynamic.contains(scene) =>
        (dynamic -- journey.history.map{_.scene}).toList match {
          case Nil => endings(path(variables))
          case x :: Nil => x
          case xs =>
            val pruned = weights(scene).filter(w=> xs.contains(w._1))
            val targets = (path(variables) match {
              case `death1` => pruned.mapValues(_._1 / pruned.values.map{_._1}.sum)
              case `life1` => pruned.mapValues(_._2 /  pruned.values.map{_._2}.sum)
              case _ => throw new IllegalArgumentException
            }).toList
            val r = new Random().nextDouble()

            @tailrec
            def weighted(choices: List[(Scene, Double)], incr: Double = 1): Scene = {
              if (incr <= r) choices.head._1
              else if (choices.tail.isEmpty) choices.head._1
              else weighted(choices.tail, incr - choices.head._2)
            }
            weighted(targets)
        }

      case `ending_death` => Fin
      case `ending_life` => Fin
      case _ => throw new IllegalArgumentException
    }

  private def path(variables: Seq[Variable]) =
    variables.collect {
      case EmotivHistVariable(like) => like
    }.head

}
