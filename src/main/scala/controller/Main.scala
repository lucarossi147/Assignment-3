package controller

import akka.actor.typed.ActorSystem
import model._
import view.ViewScala

import java.awt.event.ActionEvent

object Main extends App {
  var v = new ViewScala()
  val system: ActorSystem[RankCommand] = ActorSystem(Rank(v), "rank-controller")
  v.setStartButtonStatus(true)
  v.getStartButton.addActionListener((_: ActionEvent) => {
    v.setStartButtonStatus(false)
    system ! CreateRank(v.getDirectory)
  })

  v.getStopButton.addActionListener((_: ActionEvent) => {
    v.setStartButtonStatus(true)
    system ! StopRank
  })

}