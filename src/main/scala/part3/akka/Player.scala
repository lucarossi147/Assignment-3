package part3.akka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import part3.akka.PuzzleService.{GetTiles, PuzzleServiceCommand, SetTiles}
import part3.akka.puzzle.PuzzleBoard

import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

object Player {

  sealed trait PlayerCommand
  case object Failure extends PlayerCommand
  case object CreateTiles extends PlayerCommand
  final case class Tiles(tiles: Set[Tile]) extends PlayerCommand

  val n = 4
  val m = 4
  def apply(puzzleService: ActorRef[PuzzleServiceCommand]): Behavior[PlayerCommand] = Behaviors.setup {ctx =>
    val imagePath = "./src/main/resources/park.jpg"
    val board = new PuzzleBoard(n, m, imagePath)
    val observer = new MyObserver(puzzleService)
    board.addPropertyChangeListener(observer)

    //init the PuzzleBoard with the board state taken from the cluster
    puzzleService ! GetTiles(ctx.self)

    Behaviors.receiveMessage {
      case Failure =>
        Behaviors.same

      case CreateTiles =>
        puzzleService ! SetTiles(board.getTiles.asScala.toSet)
        Behaviors.same

      case Tiles(tiles) =>
        ctx.log.debug("I GOT THE TILES: " + tiles.toString())
        val javaTiles: ArrayBuffer[Tile] = ArrayBuffer.from(tiles)
        board.setTiles(javaTiles.asJava)
        board.rePaintPuzzle()
        Behaviors.same
    }
  }

}

class MyObserver(puzzleService: ActorRef[PuzzleServiceCommand]) extends PropertyChangeListener {
  override def propertyChange(evt: PropertyChangeEvent): Unit = {
//    println("tiles changed")
    val javaList = evt.getNewValue.asInstanceOf[java.util.List[Tile]]
//    transform a java list to a scala list
    puzzleService ! SetTiles(javaList.asScala.toSet)
  }
}