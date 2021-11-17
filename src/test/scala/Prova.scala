import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}


type Rank = Map[String, Int]

def sumRanking(rnk1: Rank, rnk2: Rank): Rank = {
  def merge[A, B](a: Map[A, B], b: Map[A, B])(mergef: (B, Option[B]) => B): Map[A, B] = {
    val (big, small) = if (a.size > b.size) (a, b) else (b, a)
    small.foldLeft(big) { case (z, (k, v)) => z + (k -> mergef(v, z.get(k))) }
  }
  merge(rnk1, rnk2)((v1, v2) => v2.map(_ + v1).getOrElse(v1))
}

object RankDiProva {
  var i: Rank = Map.empty
  sealed trait RankCommand
  case class CreateRank() extends RankCommand
  case class UpdateRank(rank: Rank) extends RankCommand

  def apply(): Behavior[RankCommand] = {
    Behaviors.receive{ (context, message) => message match {
      case CreateRank() =>
        context.log.info("rank di prova crea classifica")
        Behaviors.same

      case UpdateRank(rank: Rank) =>
        i = sumRanking(i, rank)
        context.log.info(s"rank: $i")
        Behaviors.same
    }

    }
  }
}




object Printer {

  case class PrintMe(partialRank: Map[String, Int])

  def apply(): Behavior[PrintMe] =
    Behaviors.receive {
      case (context, PrintMe(message)) =>
//        context.log.info(message)
        Behaviors.stopped
    }
}

//object Rank {
//  sealed trait RankCommand
//  case class CreateRank() extends RankCommand
//  case class UpdateRank(rank:Map[String,Int], partialRank: Map[String,Int]) extends RankCommand
//
//
//  def rank(rank: Map[String, Int], partialRank: Map[String,Int]): Map[String, Int] = {
//    println("merging two ranks...")
//    rank
//  }
//
//  def apply(): Behavior[RankCommand] = Behaviors.receive {
//    case (context, UpdateRank(partialRank)) =>
//      context.log.info("updating rank")
//
//  }
////  case class UpdateRank(partialRank: Map[String, Int], replyTo: ActorRef[] )
//}


object Main extends App {
  val system = ActorSystem(RankDiProva(), "fire-and-forget-sample")

  system ! RankDiProva.CreateRank()
  system ! RankDiProva.UpdateRank(5)
  system ! RankDiProva.UpdateRank(4)
  system ! RankDiProva.UpdateRank(1)

//  // note how the system is also the top level actor ref
//  val printer: ActorRef[Printer.PrintMe] = system
//  val printer2: ActorRef[Printer.PrintMe] = system
//  // these are all fire and forget
//  printer ! Printer.PrintMe("message 1")
//  printer2 ! Printer.PrintMe("Fok")
}

//object Main extends App {
//  val system = ActorSystem(Printer(), "fire-and-forget-sample")
//
//  // note how the system is also the top level actor ref
//  val printer: ActorRef[Printer.PrintMe] = system
//  val printer2: ActorRef[Printer.PrintMe] = system
//   // these are all fire and forget
//  printer ! Printer.PrintMe("message 1")
//  printer2 ! Printer.PrintMe("Fok")
//}