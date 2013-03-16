package com.github.fommil.outsight

import org.xhtmlrenderer.simple.{FSScrollPane, XHTMLPanel}
import javax.swing.{JPanel, JButton, JFrame, ScrollPaneConstants}
import java.awt.BorderLayout

// callback is called when the user requests the next scene
class StoryView(callback: => Unit) extends JFrame("Insight / Outsight") {

  setLayout(new BorderLayout)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  private val panel = new XHTMLPanel()
  private val scroll = new FSScrollPane(panel)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  add(scroll, BorderLayout.CENTER)

  private val buttons = new JPanel(new BorderLayout)
  private val nextButton = new JButton("Next")
  buttons.add(nextButton, BorderLayout.SOUTH)
  add(buttons, BorderLayout.EAST)


  def setScene(scene: Scene) = panel.setDocument(scene.xml)

}
