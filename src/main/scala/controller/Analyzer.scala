package controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import model.{RankCommand, ScalaPage, UpdateRank}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import java.io.File
import scala.annotation.tailrec

object Analyzer {
  case class Analyze()

  def apply(path: String,
            replyTo: ActorRef[RankCommand],
            unwantedWords: Set[String] = Set.empty,
           ): Behavior[Analyze] = {
    Behaviors.receive { (context, message) =>
      message match {
        case Analyze() =>
          context.log.info(s"I am ${context.self} and I started analyzing $path")
          val p: ScalaPage = read(path).getOrElse(ScalaPage(""))
          val words = p.getRelevantWords(unwantedWords)
          val rank = analyze(words)
          replyTo ! UpdateRank(rank, words.size)
          Behaviors.stopped
      }
    }
  }

  //if the path to doc exist extract the page else none
  def read(path: String): Option[ScalaPage] = {
    try {
      val doc = PDDocument.load(new File(path))
      if (doc.getCurrentAccessPermission.canExtractContent) {
        val p = ScalaPage(new PDFTextStripper().getText(doc))
        doc.close()
        Some(p)
      } else
        None
    } catch {
      case _: Throwable => println("something went wrong with path", path); None
    }
  }

  def analyze(words: Seq[String]): Map[String, Int] = {
    @tailrec
    def _analyze(words: Seq[String], rank: Map[String, Int] = Map.empty): Map[String, Int] = words match {
      case h :: t =>
        if (rank.keySet.contains(h)) _analyze(t, rank)
        else _analyze(t.filterNot(_ == h), rank + (h -> words.count(_ == h)))
      case _ => rank
    }

    _analyze(words.toList)
  }

}
