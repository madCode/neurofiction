package com.github.fommil.outsight

import org.xhtmlrenderer.simple.XHTMLPanel
import javax.swing._
import java.awt.{Rectangle, Point, BorderLayout}
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
  private val scroll = new JScrollPane(xhtml)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

  layers.add(scroll)
  layers.setLayer(scroll, 0)

  private val next = new JLabel("Press Space to proceed")
  next.setSize(150, 125)
  next.setFocusable(false)
  next.setVisible(false)
  layers.add(next)
  layers.setLayer(next, 1)

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
    override def keyPressed(e: KeyEvent) = {
      val view = scroll.getViewport
      e.getKeyCode match {
        // WORKAROUND: http://code.google.com/p/flying-saucer/issues/detail?id=219
        case KeyEvent.VK_SPACE if next.isVisible =>
          callback(journey, scene)
          view.setViewPosition(new Point)

        case KeyEvent.VK_SPACE | KeyEvent.VK_DOWN =>
          val where = (view.getViewPosition.getLocation.y + view.getHeight + 50)
          xhtml.scrollRectToVisible(new Rectangle(0, where, 0, 0))

        case KeyEvent.VK_UP =>
          val where = (view.getViewPosition.getLocation.y - 50)
          xhtml.scrollRectToVisible(new Rectangle(0, where, 0, 0))

        case _ =>
      }
    }
  })

  def update(journey: Journey, scene: Scene) {
    this.journey = journey
    this.scene = scene
    xhtml.setDocument(scene.xml)
  }

}
