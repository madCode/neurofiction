package com.github.fommil.outsight

import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.pegdown.PegDownProcessor
import org.springframework.core.io.DefaultResourceLoader
import org.xml.sax.InputSource
import scala.io.{Codec, Source}

trait ResourceSupport {

  protected def loadResource(uri: String) = {
    val stream = new DefaultResourceLoader().getResource(uri).getInputStream
    try Source.fromInputStream(stream)(Codec.UTF8).mkString
    finally stream.close()
  }
}

trait MarkdownSupport {
  protected def markdown(input: String) = new PegDownProcessor().markdownToHtml(input)
}

trait XmlSupport {

  def title = "An XHTML 1.1 document"

  private lazy val header = s"""<?xml version="1.0" encoding="utf-8"?>
                            |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN"
                            |    "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd">
                            |<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
                            |<head>
                            |	<title>$title</title>
                            |</head>
                            |<body>""".stripMargin
  private val footer = "</body></html>"

  protected def htmlToXml(html: String) = loadXMLFromString(header + html + footer)

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
}