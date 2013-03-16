package com.github.fommil.outsight

import org.xhtmlrenderer.simple.{FSScrollPane, XHTMLPanel}
import javax.swing._
import java.awt.BorderLayout
import java.awt.event._
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

  private val xhtml = new XHTMLPanel
  private val scroll = new FSScrollPane(xhtml)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  xhtml.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
    KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
    FSScrollPane.PAGE_DOWN
  )

  layers.add(scroll)
  layers.setLayer(scroll, 0)

  private val next = new JButton("Next")
  next.setSize(150, 150)
  next.setFocusable(false)
  next.setVisible(false)
  layers.add(next)
  layers.setLayer(next, 1)

  next.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) {
      callback(journey, scene)
    }
  })

  layers.addComponentListener(new ComponentAdapter {
    override def componentResized(e: ComponentEvent) {
      val size = layers.getSize()
      scroll.setSize(size)
      next.setLocation(
        size.width - next.getWidth - scroll.getVerticalScrollBar.getWidth,
        size.height - next.getHeight
      )
    }
  })

  scroll.getViewport.addChangeListener(new ChangeListener {
    def stateChanged(e: ChangeEvent) {
      val view = scroll.getViewport
      val where = (view.getViewPosition.getY + view.getHeight)
      next.setVisible(where > 0.99 * xhtml.getSize().getHeight)
    }
  })

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) = e.getKeyChar match {
      case ' ' if next.isVisible => next.doClick()
      case _ =>
    }
  })

  def update(journey: Journey) {
    this.journey = journey
    this.scene = story.transitions.next(journey)
    xhtml.setDocument(scene.xml)
  }

  update(Journey(Nil, Nil))

}
