package com.github.fommil.outsight

import org.xhtmlrenderer.simple.{FSScrollPane, XHTMLPanel}
import javax.swing.{ScrollPaneConstants, JFrame}
import java.awt.GraphicsEnvironment
import akka.contrib.jul.JavaLogging

object Main extends App with JavaLogging {

  val rules = SnowWhiteRules()
  val story = Story(Set(EmotivHistExtractor(rules)), rules)
  val journey = Journey(Nil, Nil)

  val document = story.transitions.next(journey).xml

  val panel = new XHTMLPanel()
  panel.setDocument(document)

  val scroll = new FSScrollPane(panel)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  val frame = new JFrame("Insight / Outsight")
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.setUndecorated(true)
  frame.add(scroll)
  frame.pack()

  val dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
  dev.setFullScreenWindow(frame)
  frame.setVisible(true)

  //  val emotiv = new Emotiv()
  //  import scala.collection.JavaConversions._
  //  val session = new EmotivSession()
  //  session.setName("My Session")
  //  for (packet <- emotiv) {
  //    val datum = EmotivDatum.fromPacket(packet)
  //    datum.setSession(session)
  //    log.info(datum.toString)
  //  }


}
