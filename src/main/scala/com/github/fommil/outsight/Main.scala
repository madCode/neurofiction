package com.github.fommil.outsight

import org.xhtmlrenderer.simple.{FSScrollPane, XHTMLPanel}
import javax.swing.JFrame
import com.github.fommil.swing.SwingConvenience

object Main extends App {

  val rules = SnowWhiteRules()
  val story = Story(Set(EmotivHistExtractor(rules)), rules)
  val journey = Journey(Nil, Nil)

  val document = story.transitions.next(journey).xml

  val panel = new XHTMLPanel()
  panel.getSharedContext.getTextRenderer.setFontScale(3)
  panel.getSharedContext.getTextRenderer.setSmoothingThreshold(0)
  panel.setDocument(document)

  val scroll = new FSScrollPane(panel)
  val frame = new JFrame("Insight / Outsight")
  SwingConvenience.enableOSXFullscreen(frame)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.add(scroll)
  frame.pack()
  frame.setSize(1024, 768)
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
