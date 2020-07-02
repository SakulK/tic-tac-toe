package tictactoe.api

import sttp.tapir._
import sttp.tapir.codec.refined._
import sttp.tapir.json.circe._
import tictactoe.model.{GameId, PlayerId, GameField}
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import tictactoe.model._

object Endpoints {
  import TapirCodecs._
  import JsonCodecs._

  val newGame: Endpoint[PlayerId, JoinError, GameId, Nothing] =
    endpoint.put
      .in("game")
      .in("new")
      .in(path[PlayerId]("player id"))
      .out(jsonBody[GameId])
      .errorOut(plainBody[JoinError])

  val joinGame: Endpoint[(GameId, PlayerId), JoinError, Unit, Nothing] =
    endpoint.post
      .in("game")
      .in("join")
      .in(path[GameId]("game id"))
      .in(path[PlayerId]("player id"))
      .out(emptyOutput)
      .errorOut(plainBody[JoinError])

  val gameState: Endpoint[(GameId, PlayerId), Unit, BoardState, Nothing] =
    endpoint.get
      .in("game")
      .in("state")
      .in(path[GameId]("game id"))
      .in(path[PlayerId]("player id"))
      .out(jsonBody[BoardState])

  val executeMove: Endpoint[
    (GameId, PlayerId, GameField.FieldNumber),
    MoveError,
    BoardState,
    Nothing
  ] =
    endpoint.post
      .in("game")
      .in("move")
      .in(path[GameId]("game id"))
      .in(path[PlayerId]("player id"))
      .in(path[GameField.FieldNumber]("field"))
      .out(jsonBody[BoardState])
      .errorOut(plainBody[MoveError])

  val all = List(newGame, joinGame, gameState, executeMove)
}
