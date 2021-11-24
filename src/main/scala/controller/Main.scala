package controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import model.{CreateRank, Rank, RankCommand, StopRank}
import view.ViewScala

import java.awt.event.ActionEvent
import scala.jdk.CollectionConverters._

object Main extends App {
  var v = new ViewScala()
  val system: ActorSystem[MainCommand] = ActorSystem(MainActor(v), "rank-controller")
  v.setStartButtonStatus(true)
  v.getStartButton.addActionListener((_: ActionEvent) => {
    v.setStartButtonStatus(false)
    system ! Start
  })

  v.getStopButton.addActionListener((_: ActionEvent) => {
    v.setStartButtonStatus(true)
    system ! Stop
  })

}

sealed trait MainCommand

case object Start extends MainCommand

case object Stop extends MainCommand

case class UpdateView(sorted: Map[String, Int], wordCount: Int) extends MainCommand

object MainActor {

  def apply(viewScala: ViewScala): Behavior[MainCommand] = {
    Behaviors.receive[MainCommand] { (context, message) =>
      message match {
        case Start =>
          viewScala.reset()
          val r = context.spawn(Rank(context.self), "rank")
          context.
          r ! CreateRank(viewScala.getDirectory, viewScala.getIgnorePath)
          Behaviors.same
        case Stop =>
          context.child("rank").get ! StopRank
          Behaviors.same
        case UpdateView(sorted, wordCount) =>
          viewScala.rankUpdated(sorted.map(t => (t._1, int2Integer(t._2))).asJava)
          viewScala.updateWordsCounter(wordCount)
          Behaviors.same
      }
    }
  }
}