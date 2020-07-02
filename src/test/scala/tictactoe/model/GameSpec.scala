package tictactoe.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import eu.timepit.refined.auto._

class GameSpec extends AnyFlatSpec with Matchers {

  "BoardState.isFinished" should "return false when there is no winner or draw" in {
    BoardState().isFinished shouldBe false
    BoardState().updated(1, MarkX).updated(3, MarkX).updated(5, MarkX).isFinished shouldBe false
    BoardState().updated(2, MarkO).updated(8, MarkO).updated(4, MarkO).isFinished shouldBe false
  }

  it should "return true when one of the players has won" in {
    BoardState().updated(0, MarkX).updated(1, MarkX).updated(2, MarkX).isFinished shouldBe true
    BoardState().updated(3, MarkO).updated(4, MarkO).updated(5, MarkO).isFinished shouldBe true
    BoardState().updated(0, MarkO).updated(3, MarkO).updated(6, MarkO).isFinished shouldBe true
    BoardState().updated(2, MarkX).updated(5, MarkX).updated(8, MarkX).isFinished shouldBe true
    BoardState().updated(0, MarkX).updated(4, MarkX).updated(8, MarkX).isFinished shouldBe true
    BoardState().updated(2, MarkO).updated(4, MarkO).updated(6, MarkO).isFinished shouldBe true
  }

  it should "return true when there is a draw" in {
    BoardState()
      .updated(0, MarkX)
      .updated(1, MarkO)
      .updated(2, MarkX)
      .updated(3, MarkO)
      .updated(4, MarkX)
      .updated(5, MarkX)
      .updated(6, MarkO)
      .updated(7, MarkX)
      .updated(8, MarkO)
      .isFinished shouldBe true
  }
}
