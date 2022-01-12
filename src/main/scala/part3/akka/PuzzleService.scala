package part3.akka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ddata.Replicator.UpdateResponse
import akka.cluster.ddata.typed.scaladsl.Replicator._
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.cluster.ddata.{GSet, GSetKey, SelfUniqueAddress}
import part3.akka.Player.{CreateTiles, Failure, PlayerCommand, Tiles}

object PuzzleService {

  trait PuzzleServiceCommand
  final case class GetTiles(replyTo: ActorRef[PlayerCommand]) extends PuzzleServiceCommand
  final case class SetTiles(tiles: Set[Tile]) extends PuzzleServiceCommand
  final case object Won extends PuzzleServiceCommand
  final case class PaintPuzzle(tiles: Set[Tile]) extends PuzzleServiceCommand

  sealed trait InternalPuzzleServiceCommand extends PuzzleServiceCommand
  final case class InternalUpdateResponse(response: UpdateResponse[GSet[Tile]]) extends InternalPuzzleServiceCommand
  final case class InternalGetResponse(replyTo: ActorRef[PlayerCommand], rsp: GetResponse[GSet[Tile]]) extends InternalPuzzleServiceCommand
  final case class InternalSubscribeResponse(chg: SubscribeResponse[GSet[Tile]]) extends InternalPuzzleServiceCommand
  final case class InternalUpdate(asd: Replicator.Update[GSet[Tile]]) extends InternalPuzzleServiceCommand

  def apply(): Behavior[PuzzleServiceCommand] = Behaviors.setup { context =>

    implicit val node: SelfUniqueAddress = DistributedData(context.system).selfUniqueAddress
    val key = GSetKey[Tile]("puzzleBoard")

    val player = context.spawn(Player(context.self), "player")

    DistributedData.withReplicatorMessageAdapter[PuzzleServiceCommand, GSet[Tile]] { replicator =>
      // Subscribe to changes of the given `key`.
      replicator.subscribe(key, InternalSubscribeResponse.apply)

      implicit val node: SelfUniqueAddress = DistributedData(context.system).selfUniqueAddress
      Behaviors.receiveMessage[PuzzleServiceCommand] {

        case GetTiles(replyTo) =>
          context.log.debug("ENTERING GET TILES")
          replicator.askGet(
            Get(key, ReadLocal),
            value => InternalGetResponse(replyTo, value)
          )
          Behaviors.same

        case SetTiles(tiles) =>
          context.log.debug("ENTERING SET TILES")
          context.log.debug(s"RECEIVED ${tiles.size.toString} TILES" )
          replicator.askUpdate(
            Update(key, GSet.empty[Tile], WriteLocal)(_.copy(tiles)),
            InternalUpdateResponse.apply
          )
          context.log.debug("EXITING SET TILES")
          Behaviors.same

        case internal: InternalPuzzleServiceCommand => internal match {

          case _: InternalUpdateResponse =>
            context.log.debug("ENTERING INTERNAL UPDATE RESPONSE")
            Behaviors.same

          case InternalGetResponse(replyTo, NotFound(_)) =>
            context.log.debug("ENTERING INTERNAL GET -> NOT FOUND RESPONSE")
            context.log.info("PUZZLE NOT FOUND IN CLUSTER")
            replyTo ! CreateTiles
            Behaviors.same

          case InternalGetResponse(replyTo, g @ GetSuccess(_)) =>
            context.log.debug("ENTERING INTERNAL GET -> SUCCESS RESPONSE")
            context.log.info(g.dataValue.elements.toString())
            replyTo ! Tiles(g.dataValue.elements)
            Behaviors.same

          case InternalGetResponse(replyTo, GetFailure(_) ) =>
            context.log.debug("ENTERING INTERNAL GET -> FAILURE RESPONSE")
            context.log.info("THERE HAVE BEEN A FAILURE")
            replyTo ! Failure
            Behaviors.same

            //quando cambia mi mando un messaggio per aggiornare
          case InternalSubscribeResponse(Replicator.Changed(_)) =>
            context.log.debug("ENTERING INTERNAL SUBSCRIBE RESPONSE")
            //non e' detto che vada
            context.self ! GetTiles(player)
            Behaviors.same

          case InternalSubscribeResponse(chg) =>
            context.log.debug("ENTERING INTERNAL SUBSCRIBE RESPONSE")
            Behaviors.same

          case _ =>
            context.log.debug("OTHER INTERNAL")
            Behaviors.same

        }
        case _ =>
          context.log.debug("OTHER")
          Behaviors.same
      }
    }
  }
}



