import Alias.Rank
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import model.ScalaPage
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import java.io.File
import scala.annotation.tailrec

object Alias{
  type Rank = Map[String, Int]

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
  var i: Rank = Map.empty
  sealed trait RankCommand
  case class CreateRank() extends RankCommand
  case class UpdateRank(rank: Rank) extends RankCommand

  object Analyzer {
    case class Analyze()

    def apply(path: String,
              replyTo: ActorRef[RankCommand],
              unwantedWords: Set[String] = Set.empty,
             ):Behavior[Analyze] = {
      Behaviors.receive{(context, message) => message match {
        case Analyze() =>
          context.log.info(s"I am an analyzer and I started analyzing $path")
          val p: ScalaPage = read(path).getOrElse(ScalaPage(""))
          val words = p.getRelevantWords(unwantedWords)
          val rank = analyze(words)
          replyTo ! RankDiProva.UpdateRank(rank)
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
        case _ :Throwable => println("something went wrong with path", path); None
      }
    }

    def analyze(words: Seq[String]): Map[String, Int] = {
      @tailrec
      def _analyze(words: Seq[String], rank: Map[String, Int] = Map.empty): Map[String, Int] = words match {
        case h::t =>
          if (rank.keySet.contains(h)) _analyze(t, rank)
          else _analyze(t.filterNot(_ == h), rank +(h -> words.count(_ == h)))
        case _ => rank
      }
      _analyze(words.toList)
    }

  }

  def apply(): Behavior[RankCommand] = {
    Behaviors.receive{ (context, message) => message match {
      case CreateRank() =>
        context.log.info("rank di prova crea classifica")
        val a1 = context.spawn(Analyzer("/home/luca/Desktop/Effective Java.pdf" , context.self, Set("i")), "analyzer")
        a1 ! Analyzer.Analyze()
        Behaviors.same

      case UpdateRank(rank: Rank) =>
        i = sumRanking(i, rank)
        //notify gui of changes
        context.log.info(s"rank: $i")
        Behaviors.same
      }
    }
  }
}

object Main extends App {
  val system = ActorSystem(RankDiProva(), "fire-and-forget-sample")
  system ! RankDiProva.CreateRank()
}
