package com.github.fommil.outsight

import org.springframework.core.io.DefaultResourceLoader
import io.{Codec, Source}
import org.pegdown.{Extensions, PegDownProcessor}
import javax.xml.parsers.DocumentBuilderFactory
import java.io.{StringReader, ByteArrayInputStream}
import akka.contrib.jul.JavaLogging
import org.xml.sax.InputSource


case class Story(extractors: Set[VariableExtractor],
                 transitions: TransitionCalculator)

/** @param resource as a Spring resource string
  * @see [[http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/resources.html#resources-resource-strings Spring Resource Strings]]
  */
case class Scene(resource: String) {

  private lazy val raw = {
    val stream = new DefaultResourceLoader().getResource(resource).getInputStream
    try Source.fromInputStream(stream)(Codec.UTF8).mkString
    finally stream.close()
  }

  private lazy val html = new PegDownProcessor().markdownToHtml(raw)

  // TODO: nice CSS
  private lazy val xmlHeader = s"""<?xml version="1.0" encoding="utf-8"?>
                            |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN"
                            |    "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd">
                            |<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
                            |<head>
                            |	<title>$id</title>
                            |</head>
                            |<body>""".stripMargin
  private val xmlFooter = "</body></html>"

  def xml = loadXMLFromString(xmlHeader + html + xmlFooter)

  private def loadXMLFromString(xml: String) = {
    val fac = DocumentBuilderFactory.newInstance()
    fac.setNamespaceAware(true)
    fac.setValidating(false) // does this ever do anything except hang?
    //  fac.setFeature("http://xml.org/sax/features/namespaces", false)
    //  fac.setFeature("http://xml.org/sax/features/validation", false)
    //  fac.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    fac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    val input = new InputSource(new StringReader(xml))
    fac.newDocumentBuilder().parse(input)
  }

  lazy val id = resource.split("/").last.replace(".md", "")
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
