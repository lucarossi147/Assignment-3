package model

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import controller.{Analyzer, MainCommand}

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}
import scala.collection.immutable.ListMap
import scala.util.Random

object Rank {

  var i: Map[String, Int] = Map.empty
  var wordsCounted = 0

  def apply(replyTo: ActorRef[MainCommand]): Behavior[RankCommand] = {
    Behaviors.receive { (context, message) =>
      message match {
        case CreateRank(path, ignoreWordsPath) =>
          context.log.info("rank di prova crea classifica")
          Files.walk(Paths.get(path))
            .filter(_.isAbsolute)
            .filter(_.getFileName.toString.endsWith(".pdf"))
            .forEach(path => {
              val a = context.spawn(
                Analyzer(path.toString,
                  context.self,
                  getFromIgnoreText(ignoreWordsPath)
                    .getOrElse(Set())), "analyzer" + Random.nextInt().toString)
              a ! Analyzer.Analyze()
            })
          Behaviors.same

        case UpdateRank(rank: Map[String, Int], wordsCount: Int) =>
          wordsCounted += wordsCount
          i = sumRanking(i, rank)

          val sortedMap = ListMap
            .from(rank.toSeq.sortWith(_._2 > _._2)) //Ordering
            .take(10) //Take the first ten

          context.log.info("rank: " + i.take(10))
          Behaviors.same

        case StopRank => Behaviors.stopped
      }
    }
  }

  private def getFromIgnoreText(fileName: String): Option[Set[String]] = {
    val words = """([A-Za-z])+""".r
    try {
      val src = io.Source.fromFile(fileName)
      val res = src.getLines.flatMap(words.findAllIn).toSet
      src.close()
      Some(res)
    } catch {
      case _: FileNotFoundException => println("Ignore File not found"); None
    }
  }

  private def sumRanking(rnk1: Map[String, Int], rnk2: Map[String, Int]): Map[String, Int] = {
    def merge[A, B](a: Map[A, B], b: Map[A, B])(mergef: (B, Option[B]) => B): Map[A, B] = {
      val (big, small) = if (a.size > b.size) (a, b) else (b, a)
      small.foldLeft(big) { case (z, (k, v)) => z + (k -> mergef(v, z.get(k))) }
    }

    merge(rnk1, rnk2)((v1, v2) => v2.map(_ + v1).getOrElse(v1))
  }
}