package com.github.fommil.outsight

import org.xhtmlrenderer.simple.XHTMLPanel
import javax.swing._
import java.awt._
import java.awt.event._
import akka.contrib.jul.JavaLogging
import javax.swing.event.{ChangeEvent, ChangeListener}
import scala.swing._
import org.w3c.dom.Document
import java.awt.Point
import java.awt.Rectangle
import scala.swing.Component
import scala.swing.Label
import java.awt.Color
import scala.swing.GridBagPanel.Fill
import org.jdesktop.swingx.JXTextField
import javax.swing.plaf.ColorUIResource
import com.github.fommil.emokit.{Packet, EmotivListener}


class OutsightFrame extends JFrame("Insight / Outsight") {
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  setLayout(new BorderLayout)

  UIManager.put("Panel.background",  Color.WHITE)

  def setCentre(pane: JPanel) {
    add(pane, BorderLayout.CENTER)
    pane.requestFocus()
  }
}

// callback is called when the user requests the next scene
class StoryView(cutscene: (Journey, Scene) => Unit,
                back: (Journey, Seq[Variable]) => Unit) extends JPanel(new BorderLayout) with JavaLogging {

  private var journey: Journey = null
  private var scene: Scene = null
  private var variables: Seq[Variable] = null

  private val layers = new JLayeredPane
  add(layers, BorderLayout.CENTER)
  setFocusable(true)

  private val xhtml = new XHTMLPanel
  private val scroll = new JScrollPane(xhtml)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

  layers.add(scroll)
  layers.setLayer(scroll, 0)

  private val next = new JLabel("Press Enter to proceed")
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
    override def keyPressed(e: KeyEvent) {
      val view = scroll.getViewport
      e.getKeyCode match {
        // WORKAROUND: http://code.google.com/p/flying-saucer/issues/detail?id=219
        case KeyEvent.VK_ENTER if next.isVisible =>
          cutscene(journey, scene)
          view.setViewPosition(new Point)

        case KeyEvent.VK_SPACE | KeyEvent.VK_DOWN =>
          val where = (view.getViewPosition.getLocation.y + view.getHeight + 50)
          xhtml.scrollRectToVisible(new Rectangle(0, where, 0, 0))

        case KeyEvent.VK_UP =>
          val where = (view.getViewPosition.getLocation.y - 50)
          xhtml.scrollRectToVisible(new Rectangle(0, where, 0, 0))

        case KeyEvent.VK_LEFT =>
          back(journey, variables)

        case _ =>
      }
    }
  })

  def setModel(journey: Journey, scene: Scene, variables: Seq[Variable] = Nil) {
    require(journey != null)
    require(scene != null)
    require(variables != null)

    this.journey = journey
    this.scene = scene
    this.variables = variables
    xhtml.setDocument(scene.xml)
  }

}

// FSM Panel which is in one of 4 states:
//
// 0. request with instructions to recharge the device
// 1. summary introduction and username input
// 2. displaying instructions for the EEG
// 3. interactive calibration with visual feedback
//
// all stages have a timeout of 5 minutes, reverting to state 0.
// State 0 will attempt to put the device into power saving mode
// (i.e. ask someone to charge the device!)
//
// can't use scala.swing just yet https://issues.scala-lang.org/browse/SI-3933
class IntroductionView extends JPanel with JavaLogging with EmotivListener {

  val cards = new CardLayout
  setLayout(cards)
  setFocusable(true)

  val prompt = "Please let us know your name (or your email address) and then press Enter."
  val nameField = new JXTextField(prompt)
  nameField.setColumns(42)
  // https://issues.scala-lang.org/browse/SI-7307e
  nameField.addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) {
      if (e.getKeyCode == KeyEvent.VK_ENTER) next()
    }
  })

  val panel0 = new FlowPanel() {
    contents += new Label("Request Recharge")
  }.peer

  implicit def wrap(j: JComponent) = Component.wrap(j)


  val panel1 = new BorderPanel() {

    import BorderPanel.Position._

    layout(new MarkdownPanel("summary")) = Center

    layout(new GridBagPanel {
      layout(nameField) = new Constraints
    }) = East
  }.peer

  val panel2 = new FlowPanel() {
    contents += new Label("Instructions")
  }.peer

  val panel3 = new FlowPanel() {
    contents += new Label("Calibration")
  }.peer

  add(panel0)
  add(panel1)
  add(panel2)
  add(panel3)

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) {
      e.getKeyCode match {
        case KeyEvent.VK_ENTER if current == panel3 =>
          log.info("finished...")

        case KeyEvent.VK_ENTER =>
          next()

        case KeyEvent.VK_LEFT if current != panel0 =>
          previous()

        case _ =>
      }
    }
  })

  def current = getComponents.filter(_.isVisible).head

  def next() {
    if (current == panel1 && nameField.getText.isEmpty)
      return

    cards.next(this)
    invalidate()
  }

  def previous() {
    cards.previous(this)
  }

  def receivePacket(p: Packet) {
    if (p.getBatteryLevel < 200) {
      cards.first(this)
    }
  }

  def connectionBroken() {}

  def username = nameField.getText

  next()
}

class MarkdownPanel(val resource: String) extends XHTMLPanel with ResourceSupport with MarkdownSupport with XmlSupport {
  override lazy val css = loadResource("classpath:/com/github/fommil/outsight/style.css")

  private lazy val markup = markdown(loadResource("classpath:/com/github/fommil/outsight/" + resource + ".md"))

  private def xml: Document = htmlToXml(markup)

  setDocument(xml)

}