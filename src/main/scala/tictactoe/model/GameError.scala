package tictactoe.model

sealed trait GameError

sealed trait JoinError extends GameError
case object AlreadyJoined extends JoinError
case object AllSpotsTaken extends JoinError

sealed trait MoveError extends GameError
case object SpaceOccupied extends MoveError
case object NotYourTurn extends MoveError
case object GameNotStarted extends MoveError
case object GameIsFinished extends MoveError
