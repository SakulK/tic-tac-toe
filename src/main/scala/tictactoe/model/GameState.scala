package tictactoe.model

sealed trait GameState {
  def boardState: BoardState
}

case class WaitingForPlayers(
    players: Seq[PlayerId] = Seq.empty
) extends GameState {
  def boardState: BoardState = BoardState()
}

case class GameInProgress(
    xPlayer: PlayerId,
    oPlayer: PlayerId,
    boardState: BoardState
) extends GameState

case class GameFinished(
    xPlayer: PlayerId,
    oPlayer: PlayerId,
    boardState: BoardState
) extends GameState
