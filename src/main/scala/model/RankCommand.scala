package model

sealed trait RankCommand

case class CreateRank(path: String) extends RankCommand
case class UpdateRank(rank: Map[String, Int], wordsCount: Int) extends RankCommand
case object StopRank extends RankCommand