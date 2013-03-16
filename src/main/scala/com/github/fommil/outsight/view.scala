package com.github.fommil.outsight

import org.xhtmlrenderer.simple.{FSScrollPane, XHTMLPanel}
import javax.swing._
import java.awt.BorderLayout
import java.awt.event.{ActionEvent, ActionListener, ComponentEvent, ComponentAdapter}
import akka.contrib.jul.JavaLogging
import javax.swing.event.{ChangeEvent, ChangeListener}

// callback is called when the user requests the next scene
class StoryView(story: Story, callback: (Journey, Scene) => Unit) extends JFrame("Insight / Outsight") with JavaLogging {

  private var journey: Journey = null
  private var scene: Scene = null

  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  setLayout(new BorderLayout)
  private val layers = new JLayeredPane
  add(layers, BorderLayout.CENTER)

  private val panel = new XHTMLPanel
  private val scroll = new FSScrollPane(panel)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  // use of Integer not a typo
  // http://docs.oracle.com/javase/tutorial/uiswing/components/layeredpane.html
  layers.add(scroll, new Integer(0))

  private val nextButton = new JButton("Next")
  nextButton.setSize(150, 150)
  nextButton.setFocusable(false)
  nextButton.setVisible(false)
  layers.add(nextButton, new Integer(1))

  nextButton.addActionListener(new ActionListener{
    def actionPerformed(e: ActionEvent) {
      callback(journey, scene)
    }
  })

  layers.addComponentListener(new ComponentAdapter {
    override def componentResized(e: ComponentEvent) {
      val size = layers.getSize()
      scroll.setSize(size)
      nextButton.setLocation(
        size.width - nextButton.getWidth - scroll.getVerticalScrollBar.getWidth,
        size.height - nextButton.getHeight
      )
    }
  })

  scroll.getViewport.addChangeListener(new ChangeListener {
    def stateChanged(e: ChangeEvent) {
      val view = scroll.getViewport
      val where = (view.getViewPosition.getY + view.getHeight)
      nextButton.setVisible(where > 0.95 * panel.getSize().getHeight)
    }
  })

  def update(journey: Journey) {
    this.journey = journey
    this.scene = story.transitions.next(journey)
    panel.setDocument(scene.xml)
  }

  update(Journey(Nil, Nil))

}
