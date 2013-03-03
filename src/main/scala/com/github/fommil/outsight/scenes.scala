package com.github.fommil.outsight

import org.w3c.dom.Document

case class Story(extractors: Set[VariableExtractor],
                 transitions: TransitionCalculator)

/** @param resource as a Spring resource string
  * @see [[http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/resources.html#resources-resource-strings Spring Resource Strings]]
  */
case class Scene(resource: String) extends ResourceSupport with MarkdownSupport with XmlSupport {

  private lazy val html = markdown(loadResource(resource))

  def xml: Document = htmlToXml(html)

}

object Fin extends Scene("Fin")


trait Response

/** Snapshot of what the user has experienced so far.
  *
  * To remain general, we allow `Scene`s to be visited
  * multiple times.
  */
case class Journey(scenes: List[(Scene, Set[Response])],
                   variables: List[Variable])
