package part1.controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import part1.model.{RankCommand, Page, UpdateRank}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import java.io.File
import scala.annotation.tailrec

sealed trait AnalyzeCommand

case object StartAnalysis extends AnalyzeCommand

case class Read(index: Int) extends AnalyzeCommand

case class GetWords(page: Page, i: Int) extends AnalyzeCommand

case class Analyze(words: Seq[String], index: Int) extends AnalyzeCommand

case class AnalyzedUpTo(upTo: Int) extends AnalyzeCommand


object Analyzer {

  def apply(path: String,
            replyTo: ActorRef[RankCommand],
            unwantedWords: Set[String] = Set.empty,
           ): Behavior[AnalyzeCommand] = Behaviors.setup { context =>
    val numberOfPages = getNumberOfPages(path)
    Behaviors.receiveMessage {
      case StartAnalysis =>
        context.self ! Read(0)
        Behaviors.same

      case Read(index) =>
        val p: Page = readPage(path, index).getOrElse(Page(""))
        context.self ! GetWords(p, index)
        Behaviors.same

      case GetWords(page: Page, index: Int) =>
        val words = page.getRelevantWords(unwantedWords)
        context.self ! Analyze(words, index)
        Behaviors.same

      case Analyze(words: Seq[String], index: Int) =>
        val rank = analyze(words)
        replyTo ! UpdateRank(rank, words.size)
        context.self ! AnalyzedUpTo(index)
        Behaviors.same

      case AnalyzedUpTo(upTo) =>
        if (upTo == numberOfPages)
          Behaviors.stopped
        else {

          context.self ! Read(upTo + 1)
          Behaviors.same
        }
    }
  }


  def getNumberOfPages(path: String): Int = {
    val doc = PDDocument.load(new File(path))
    val n = doc.getNumberOfPages
    doc.close()
    n
  }

  //if the path to doc exist extract the page else none
  def readPage(path: String, from: Int = 0, to:Int =0): Option[Page] = {
    try {
      val doc = PDDocument.load(new File(path))
      val stripper = new PDFTextStripper()
      stripper.setStartPage(from)
      stripper.setEndPage(to)
      if (doc.getCurrentAccessPermission.canExtractContent) {
        val p = Page(new PDFTextStripper().getText(doc))
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
        _analyze(t.filterNot(_ == h), rank + (h -> words.count(_ == h)))
      case _ => rank
    }

    _analyze(words.toList)
  }

}
