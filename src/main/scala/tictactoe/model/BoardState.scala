package tictactoe.model

import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.api.Refined

sealed trait Mark
case object MarkX extends Mark
case object MarkO extends Mark

sealed trait FieldState
case object EmptyField extends FieldState
case class Occupied(mark: Mark) extends FieldState

case class BoardState(fields: Vector[FieldState] = Vector.fill(9)(EmptyField)) {
  def isFinished: Boolean =
    BoardState.winningPositions.exists { indices =>
      indices
        .map(fields.apply)
        .collect { case Occupied(mark) => mark }
        .groupBy(identity)
        .exists(_._2.size == 3)
    } || fields.forall(_ != EmptyField)

  def nextMark: Mark = {
    val counts = fields.collect { case Occupied(mark) => mark }.groupBy(identity).view.mapValues(_.size)
    if (counts.getOrElse(MarkX, 0) <= counts.getOrElse(MarkO, 0)) {
      MarkX
    } else {
      MarkO
    }
  }

  def updated(index: GameField.FieldNumber, mark: Mark) =
    copy(fields = fields.updated(index.value, Occupied(mark)))
}

object GameField {
  type ValidFieldNumber = Interval.ClosedOpen[0, 9]
  type FieldNumber = Int Refined ValidFieldNumber
}

object BoardState {
  val winningPositions =
    (0 until 3).flatMap { i =>
      Seq(
        Seq(i, i + 3, i + 6),
        Seq(i * 3, i * 3 + 1, i * 3 + 2)
      )
    } ++ Seq(
      Seq(0, 4, 8),
      Seq(2, 4, 6)
    )
}
