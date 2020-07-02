package tictactoe.behavior

import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId
import akka.actor.typed.Behavior
import tictactoe.model._
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.scaladsl.Effect

sealed trait GameCommand
case class JoinGame(player: PlayerId, replyTo: ActorRef[Either[JoinError, Unit]]) extends GameCommand
case class ExecuteMove(
    player: PlayerId,
    fieldNumber: GameField.FieldNumber,
    replyTo: ActorRef[Either[MoveError, BoardState]]
) extends GameCommand
case class GetState(player: PlayerId, replyTo: ActorRef[BoardState]) extends GameCommand

sealed trait GameEvent
case class PlayerJoined(player: PlayerId) extends GameEvent
case class MoveExecuted(player: PlayerId, fieldNumber: GameField.FieldNumber) extends GameEvent

object GameBehavior {
  val TypeKey: EntityTypeKey[GameCommand] = EntityTypeKey[GameCommand]("Game")

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[GameCommand] =
    EventSourcedBehavior[GameCommand, GameEvent, GameState](
      persistenceId = persistenceId,
      emptyState = WaitingForPlayers(),
      commandHandler = gameCommandHandler,
      eventHandler = gameEventHandler
    )

  def gameCommandHandler(state: GameState, cmd: GameCommand): Effect[GameEvent, GameState] =
    state match {
      case WaitingForPlayers(players) if players.size < 2 =>
        cmd match {
          case JoinGame(player, replyTo) =>
            if (!players.contains(player)) {
              Effect
                .persist(PlayerJoined(player))
                .thenReply(replyTo)(_ => Right(()))
            } else {
              Effect.reply(replyTo)(Left(AlreadyJoined))
            }

          case ExecuteMove(_, _, replyTo) =>
            Effect.reply(replyTo)(Left(GameNotStarted))

          case GetState(player, replyTo) =>
            Effect.reply(replyTo)(BoardState())
        }

      case GameInProgress(xPlayer, oPlayer, boardState) =>
        cmd match {
          case JoinGame(player, replyTo) =>
            Effect.reply(replyTo)(Left(AllSpotsTaken))

          case GetState(player, replyTo) if player == xPlayer || player == oPlayer =>
            Effect.reply(replyTo)(boardState)

          case ExecuteMove(player, fieldNumber, replyTo) if player == xPlayer || player == oPlayer =>
            if (boardState.fields(fieldNumber.value) == EmptyField) {
              val mark = if (player == xPlayer) {
                MarkX
              } else {
                MarkO
              }
              if (boardState.nextMark == mark) {
                Effect
                  .persist(MoveExecuted(player, fieldNumber))
                  .thenReply(replyTo)(state => Right(state.boardState))
              } else {
                Effect.reply(replyTo)(Left(NotYourTurn))
              }
            } else {
              Effect.reply(replyTo)(Left(SpaceOccupied))
            }

          case _ =>
            Effect.unhandled
        }

      case GameFinished(xPlayer, oPlayer, boardState) =>
        cmd match {
          case JoinGame(player, replyTo) => Effect.reply(replyTo)(Left(AllSpotsTaken))

          case GetState(player, replyTo) if player == xPlayer || player == oPlayer =>
            Effect.reply(replyTo)(boardState)

          case ExecuteMove(_, _, replyTo) =>
            Effect.reply(replyTo)(Left(GameIsFinished))
        }
    }

  def gameEventHandler(state: GameState, event: GameEvent): GameState =
    (state, event) match {
      case (WaitingForPlayers(players), PlayerJoined(player)) =>
        val newPlayers = players :+ player
        if (newPlayers.size == 2) {
          GameInProgress(newPlayers(0), newPlayers(1), BoardState())
        } else {
          WaitingForPlayers(newPlayers)
        }

      case (GameInProgress(xPlayer, oPlayer, boardState), MoveExecuted(player, fieldNumber)) =>
        val mark = if (player == xPlayer) {
          MarkX
        } else {
          MarkO
        }
        val newBoardState = boardState.updated(fieldNumber, mark)
        if (newBoardState.isFinished) {
          GameFinished(xPlayer, oPlayer, newBoardState)
        } else {
          GameInProgress(xPlayer, oPlayer, newBoardState)
        }

      case _ => state
    }
}
