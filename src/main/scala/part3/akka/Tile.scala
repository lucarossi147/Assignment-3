package part3.akka

import java.io.Serializable

case class Tile(originalPosition: Int,
                var currentPosition: Int,
               ) extends Comparable[Tile] with Serializable {
  override def compareTo(o: Tile): Int = Integer.compare(currentPosition, o.currentPosition)

  def currentPosition_(pos: Int): Unit = currentPosition = pos

  def isInRightPlace:Boolean = currentPosition == originalPosition

  def changeCurrentPos(newPos: Int): Unit = currentPosition = newPos
}
