package com.github.fommil.outsight

import org.xhtmlrenderer.simple.XHTMLPanel
import javax.swing._
import java.awt.event._
import akka.contrib.jul.JavaLogging
import javax.swing.event.{ChangeEvent, ChangeListener}
import scala.swing._
import org.w3c.dom.Document
import java.awt.{CardLayout, BorderLayout, Point, Rectangle}
import scala.swing.Component
import scala.swing.Label
import com.github.fommil.emokit.{Packet, EmotivListener}
import BorderPanel.Position._
import com.github.fommil.emokit.gui.{SensorView, SensorQualityView}

// I'm not proud of this file... @fommil

class OutsightFrame extends JFrame("Insight / Outsight") {
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  setLayout(new BorderLayout)

  def setCentre(pane: JPanel) {
    getContentPane.getComponents.foreach{c=>
      c.setVisible(false)
      c.setFocusable(false)
    }

    pane.setVisible(true)
    pane.setFocusable(true)
    add(pane, BorderLayout.CENTER)
    revalidate()
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

  private val xhtml = new XHTMLPanel
  private val scroll = new JScrollPane(xhtml)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

  layers.add(scroll)
  layers.setLayer(scroll, 0)

  private val next = new JLabel("Spacebar to continue...")
  next.setSize(200, 125)
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

  val timer = new Timer(2000, new ActionListener {
    def actionPerformed(e: ActionEvent) {
      next.setVisible(true)
    }
  })
  timer.setRepeats(false)

  scroll.getViewport.addChangeListener(new ChangeListener {
    def stateChanged(e: ChangeEvent) {
      val view = scroll.getViewport
      val where = (view.getViewPosition.getY + view.getHeight)
      if (where > 0.99 * xhtml.getSize().getHeight) timer.start()
      else {
        next.setVisible(false)
        timer.stop()
      }
    }
  })

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) {
      val view = scroll.getViewport
      e.getKeyCode match {
        // WORKAROUND: http://code.google.com/p/flying-saucer/issues/detail?id=219
        case (KeyEvent.VK_ENTER | KeyEvent.VK_SPACE) if next.isVisible =>
          timer.stop()
          next.setVisible(false)
          cutscene(journey, scene)
          view.setViewPosition(new Point(1, 0)) // needed to fire the change event
          view.setViewPosition(new Point)

        case KeyEvent.VK_ENTER | KeyEvent.VK_SPACE | KeyEvent.VK_DOWN =>
          val where = (view.getViewPosition.getLocation.y + view.getHeight + 50)
          xhtml.scrollRectToVisible(new Rectangle(0, where, 0, 0))

        case KeyEvent.VK_UP =>
          val where = (view.getViewPosition.getLocation.y - 50)
          xhtml.scrollRectToVisible(new Rectangle(0, where, 0, 0))

        case KeyEvent.VK_ESCAPE =>
          timer.stop()
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

// FSM Panel which is in one of 3 states:
//
// * request with instructions to recharge.md the device
// * summary introduction and username input
// * interactive calibration with visual feedback
//
// can't use scala.swing just yet https://issues.scala-lang.org/browse/SI-3933
// a really nice CardPane with focus control and sensible next/previous would
// *really* clean up this class.
class IntroductionView(introduced: String => Unit) extends JPanel with JavaLogging with EmotivListener {

  val cards = new CardLayout
  setLayout(cards)

  // we always want nameField to acquire Focus if it is displayed,
  // but there doesn't appear to be a way to do this without manual
  // delegation at all parts of the focus tree, and on card changes.
  val prompt = "Please let us know your name and then press Enter:"
  val nameField = (new TextField(42)).peer
  // https://issues.scala-lang.org/browse/SI-7307
  nameField.addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) {
      if (e.getKeyCode == KeyEvent.VK_ENTER) next()
    }
  })

  implicit def wrap(j: JComponent) = Component.wrap(j)

  val panel3 = new BorderPanel() {
    layout(new MarkdownPanel("recharge")) = Center
  }.peer

  val panel1 = new BorderPanel() {
    // https://issues.scala-lang.org/browse/SI-7309
    //    reactions += {
    //      case e: FocusGained => nameField.requestFocus()
    //    }
    layout(new MarkdownPanel("summary")) = Center
    layout(new FlowPanel {
      contents += new Label(prompt)
      contents += nameField
      contents += Swing.VStrut(400)
    }) = South
  }.peer
  panel1.addFocusListener(new FocusAdapter {
    override def focusGained(e: FocusEvent) {
      nameField.requestFocus()
    }
  })

  val calibration = new SensorQualityView
  val feedback = new SensorView

  val panel2 = new BorderPanel() {
    layout(new MarkdownPanel("instructions")) = North
    layout(calibration) = Center
    layout(feedback) = South
  }.peer


  add(panel1)
  add(panel2)
  add(panel3)

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) {
      e.getKeyCode match {
        case KeyEvent.VK_ENTER | KeyEvent.VK_SPACE => next()
        case KeyEvent.VK_ESCAPE => previous()
        case _ =>
      }
    }
  })

  def current = getComponents.filter(_.isVisible).head

  def next() {
    if (current == panel1 && nameField.getText.isEmpty) return
    if (current == panel2) introduced(nameField.getText)

    cards.next(this)
    if (current == panel1) panel1.requestFocus()
  }

  def previous() {
    if (current != panel3 && current != panel1) cards.previous(this)
    if (current == panel1) panel1.requestFocus()
  }

  def reset() {
    nameField.setText("")
    cards.first(this)
    panel1.requestFocus()
  }

  def receivePacket(p: Packet) {
    if (!isVisible) return

    calibration.receivePacket(p)
    feedback.receivePacket(p)

    if (p.getBatteryLevel > 0 && p.getBatteryLevel < 66) cards.last(this)
  }

  def connectionBroken() {}

  addFocusListener(new FocusAdapter {
    override def focusGained(e: FocusEvent) {
      if (current == panel1) panel1.requestFocus()
    }
  })
}

class MarkdownPanel(val resource: String) extends XHTMLPanel with ResourceSupport with MarkdownSupport with XmlSupport {
  override lazy val css = loadResource("classpath:/com/github/fommil/outsight/style.css")

  private lazy val markup = markdown(loadResource("classpath:/com/github/fommil/outsight/" + resource + ".md"))

  private def xml: Document = htmlToXml(markup)

  setDocument(xml)

}