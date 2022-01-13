package part3.akka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import part3.akka.PuzzleService.{GetTiles, PuzzleServiceCommand, SetTiles}
import part3.akka.puzzle.PuzzleBoard

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

object Player {

  sealed trait PlayerCommand
  case object Failure extends PlayerCommand
  case object CreateTiles extends PlayerCommand
  final case class Tiles(tiles: Set[Tile]) extends PlayerCommand

  val n = 2
  val m = 2
  def apply(puzzleService: ActorRef[PuzzleServiceCommand]): Behavior[PlayerCommand] = Behaviors.setup {ctx =>
    val imagePath = "./src/main/resources/customLogo.png"
    val board = new PuzzleBoard(n, m, imagePath)
    var initTries = 0

    puzzleService ! GetTiles(ctx.self)

    Behaviors.receiveMessage {

      case Failure =>
        Behaviors.same

      case CreateTiles =>
        if (initTries < 10) {
          initTries += 1
          puzzleService ! GetTiles(ctx.self)
        }
        else puzzleService ! SetTiles(board.getTiles.asScala.toSet)
        Behaviors.same

      case Tiles(tiles) =>
        ctx.log.info("I GOT THE TILES: ")
        ctx.log.info(tiles.toString())
        val javaTiles: ArrayBuffer[Tile] = ArrayBuffer.from(tiles)
        board.setTiles(javaTiles.asJava)
        board.rePaintPuzzle()
        Behaviors.same
    }
  }

}
