package part3.akka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.MemberStatus
import akka.cluster.ddata.Replicator.UpdateResponse
import akka.cluster.ddata.typed.scaladsl.Replicator._
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.cluster.ddata.{ ORSet, ORSetKey, SelfUniqueAddress}
import akka.cluster.typed.Cluster
import part3.akka.Player.{CreateTiles, Failure, PlayerCommand, Tiles}

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

object PuzzleService {

  trait PuzzleServiceCommand
  final case object SpawnPlayer extends PuzzleServiceCommand
  final case class GetTiles(replyTo: ActorRef[PlayerCommand]) extends PuzzleServiceCommand
  final case class SetTiles(tiles: Set[Tile]) extends PuzzleServiceCommand
  final case object ClearTiles extends PuzzleServiceCommand
  final case class AddTiles(tiles: Set[Tile]) extends PuzzleServiceCommand

  sealed trait InternalPuzzleServiceCommand extends PuzzleServiceCommand
  final case class InternalUpdateResponse(response: UpdateResponse[ORSet[Tile]]) extends InternalPuzzleServiceCommand
  final case class InternalGetResponse(replyTo: ActorRef[PlayerCommand], rsp: GetResponse[ORSet[Tile]]) extends InternalPuzzleServiceCommand
  final case class InternalSubscribeResponse(chg: SubscribeResponse[ORSet[Tile]]) extends InternalPuzzleServiceCommand

  def apply(): Behavior[PuzzleServiceCommand] = Behaviors.setup { context =>

    implicit val node: SelfUniqueAddress = DistributedData(context.system).selfUniqueAddress
    val key = ORSetKey[Tile]("tiles")
    val cluster = Cluster(context.system)
    var player: ActorRef[PlayerCommand] = null

    context.self ! SpawnPlayer

    DistributedData.withReplicatorMessageAdapter[PuzzleServiceCommand, ORSet[Tile]] { replicator =>
      // Subscribe to changes of the given `key`.
      replicator.subscribe(key, InternalSubscribeResponse.apply)

      implicit val node: SelfUniqueAddress = DistributedData(context.system).selfUniqueAddress
      Behaviors.receiveMessage[PuzzleServiceCommand] {

        //VERY IMPORTANT
        //if I'm up, Spawn the player
        case SpawnPlayer =>
          if (cluster.selfMember.status == MemberStatus.Up)
            player = context.spawn(Player(context.self), "player")
          else
            context.self ! SpawnPlayer
          Behaviors.same

        case GetTiles(replyTo) =>
          context.log.debug("ENTERING GET TILES")
          replicator.askGet(
            Get(key, ReadAll(FiniteDuration(500, MILLISECONDS))),
            value => InternalGetResponse(replyTo, value)
          )
          Behaviors.same

//      We are using a ORSet, Observed-remove set, so first we delete all tiles and then we add them again
        case SetTiles(tiles) =>
          context.log.debug("ENTERING SET TILES")
          context.log.debug(s"RECEIVED ${tiles.size.toString} TILES" )
          context.self ! ClearTiles
          context.self ! AddTiles(tiles)
          Behaviors.same

        case ClearTiles =>
          context.log.debug("CLEARING TILES")
          replicator.askUpdate(
            Update(key, ORSet.empty[Tile], WriteLocal)(_.clear(node)),
            InternalUpdateResponse.apply
          )
          Behaviors.same

        case AddTiles(tiles) =>
          context.log.debug("ENTERING ADD TILES")
          var myNewORSet = ORSet.empty[Tile]
          for (t <- tiles) {
            myNewORSet = myNewORSet.add(node, t)
          }
          replicator.askUpdate(
            Update(key, ORSet.empty[Tile], WriteLocal)(_.merge(myNewORSet)),
            InternalUpdateResponse.apply
          )
          Behaviors.same
        case internal: InternalPuzzleServiceCommand => internal match {

          case _: InternalUpdateResponse =>
            context.log.debug("ENTERING INTERNAL UPDATE RESPONSE")
            Behaviors.same

          case InternalGetResponse(replyTo, NotFound(_)) =>
            context.log.debug("ENTERING INTERNAL GET -> NOT FOUND RESPONSE")
            replyTo ! CreateTiles
            Behaviors.same

          case InternalGetResponse(replyTo, g @ GetSuccess(_)) =>
            context.log.debug("ENTERING INTERNAL GET -> SUCCESS RESPONSE")
            context.log.debug(g.dataValue.elements.toString())
            replyTo ! Tiles(g.dataValue.elements)
            Behaviors.same

          case InternalGetResponse(replyTo, GetFailure(_) ) =>
            context.log.debug("ENTERING INTERNAL GET -> FAILURE RESPONSE")
            replyTo ! Failure
            Behaviors.same

//            when a modify occurs send tiles to player
          case InternalSubscribeResponse(Replicator.Changed(_)) =>
            context.log.debug("ENTERING INTERNAL SUBSCRIBE RESPONSE")
            context.self ! GetTiles(player)
            Behaviors.same
        }
      }
    }
  }
}



