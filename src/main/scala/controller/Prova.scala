package controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import view.ViewScala

import java.awt.event.ActionEvent
import java.nio.file.{Files, Paths}
import scala.util.Random

object Alias{

  def sumRanking(rnk1: Map[String, Int], rnk2: Map[String, Int]): Map[String, Int] = {
    def merge[A, B](a: Map[A, B], b: Map[A, B])(mergef: (B, Option[B]) => B): Map[A, B] = {
      val (big, small) = if (a.size > b.size) (a, b) else (b, a)
      small.foldLeft(big) { case (z, (k, v)) => z + (k -> mergef(v, z.get(k))) }
    }
    merge(rnk1, rnk2)((v1, v2) => v2.map(_ + v1).getOrElse(v1))
  }
}



object RankDiProva {
  import Alias.sumRanking
  var i: Map[String, Int] = Map.empty
  sealed trait RankCommand
  case class CreateRank(path: String) extends RankCommand
  case class UpdateRank(rank: Map[String, Int]) extends RankCommand

  def apply(viewScala: ViewScala): Behavior[RankCommand] = {
    Behaviors.receive{ (context, message) => message match {
      case CreateRank(path) =>
        context.log.info("rank di prova crea classifica")
        Files.walk(Paths.get(path))
          .filter(_.isAbsolute)
          .filter(_.getFileName.toString.endsWith(".pdf"))
          .forEach(path => {
            val a1 = context.spawn(controller.Analyzer(path.toString, context.self, Set("i")), "analyzer"+ Random.nextInt().toString)
            a1 ! Analyzer.Analyze()
          })
        Behaviors.same

      case UpdateRank(rank: Map[String, Int]) =>
        i = sumRanking(i, rank)
        import scala.jdk.CollectionConverters._
        //notify gui of changes
        val javaMap = rank
          .filter(_._2 > 10)
          .take(10)
          .map{ case (k, v)  =>
          (k, int2Integer(v))
        }.asJava

        viewScala.rankUpdated(javaMap)
        context.log.info("rank: " + i.take(10))

        Behaviors.same
      }
    }
  }
}

object Main extends App {
  var v = new ViewScala()
  val system: ActorSystem[RankDiProva.RankCommand] = ActorSystem(RankDiProva(v), "rank-controller")
  v.getStartButton.addActionListener((_: ActionEvent) => {
    system ! RankDiProva.CreateRank(v.getDirectory)
  })

  v.getStopButton.addActionListener((_: ActionEvent) => {
    system.terminate()
  })
}
