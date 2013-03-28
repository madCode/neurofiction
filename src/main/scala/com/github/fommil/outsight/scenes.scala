package com.github.fommil.outsight

import org.w3c.dom.Document

/** @param resource as a Spring resource string
  * @see [[http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/resources.html#resources-resource-strings Spring Resource Strings]]
  */
case class Scene(resource: String) extends ResourceSupport with MarkdownSupport with XmlSupport {

  override lazy val css = loadResource(resource.replaceAll("/[^/]+?$", "/style.css"))

  override lazy val title = resource.split("/").last.replace(".md", "")

  private lazy val markup = markdown(loadResource(resource))

  def xml: Document = htmlToXml(markup)

}

object Fin extends Scene("Fin")

case class ObservedScene(scene: Scene, responses: Seq[Response]) {

  def responseT[T] = responses.collect {
    case r: T => r
  }
}

trait Response


/** Snapshot of what the user has experienced so far.
  *
  * To remain general, we allow `Scene`s to be visited
  * multiple times.
  *
  * Note, the history is functionally ordered, in the
  * sense that the head is the most recent entry.
  */
case class Journey(history: List[ObservedScene] = Nil) {

  def +(scene: Scene, responses: Response*) =
    copy(history = ObservedScene(scene, responses) :: history)

}
