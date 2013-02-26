package com.github.fommil.outsight

import org.springframework.core.io.DefaultResourceLoader
import io.{Codec, Source}


case class Story(scenes: Set[Scene],
                 extractors: Set[VariableExtractor],
                 transitions: TransitionCalculator)

/** @param resource as a Spring resource string
  * @see [[http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/resources.html#resources-resource-strings Spring Resource Strings]]
  */
case class Scene(resource: String) {

  def raw = {
    val stream = new DefaultResourceLoader().getResource(resource).getInputStream
    try Source.fromInputStream(stream)(Codec.UTF8).mkString
    finally stream.close()
  }
}


trait Response

/** Snapshot of what the user has experienced so far.
  *
  * To remain general, we allow `Scene`s to be visited
  * multiple times.
  */
case class Journey(scenes: List[(Scene, Set[Response])],
                   variables: List[Variable])
