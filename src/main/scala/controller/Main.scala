package controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import model.{CreateRank, Rank, RankCommand, StopRank}
import view.View

import java.awt.event.ActionEvent
import scala.jdk.CollectionConverters._

object Main extends App {
  var v = new View()
  val system: ActorSystem[MainCommand] = ActorSystem(MainActor(v), "rank-controller")
  v.setStartButtonStatus(true)
  v.getStartButton.addActionListener((_: ActionEvent) => {
    v.setStartButtonStatus(false)
    v.setStopButtonStatus(true)
    system ! Start
  })

  v.getStopButton.addActionListener((_: ActionEvent) => {
    system ! Stop
  })

}

sealed trait MainCommand

case object Start extends MainCommand

case object Stop extends MainCommand

case class UpdateView(sorted: Map[String, Int], wordCount: Int) extends MainCommand

object MainActor {

  var rank: ActorRef[RankCommand] = _

  def apply(viewScala: View): Behavior[MainCommand] = {
    Behaviors.receive[MainCommand] { (context, message) =>
      message match {
        case Start =>
          viewScala.reset()
          rank = context.spawn(Rank(context.self, viewScala.getNumOfWordsToBePrinted), "rank")
          context.watch(rank)
          rank ! CreateRank(viewScala.getDirectory, viewScala.getIgnorePath)
          Behaviors.same
        case Stop =>
          viewScala.setStopButtonStatus(false)
          rank ! StopRank
          Behaviors.same
        case UpdateView(sorted, wordCount) =>
          viewScala.rankUpdated(sorted.map(t => (t._1, int2Integer(t._2))).asJava)
          viewScala.updateWordsCounter(wordCount)
          Behaviors.same
      }
    }.receiveSignal {
        case (context, Terminated(ref)) =>
          context.log.info("Job stopped: {}", ref.path.name)
          viewScala.setStartButtonStatus(true)
          Behaviors.same
      }
  }
}