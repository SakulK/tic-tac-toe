package tictactoe.api

import sttp.tapir.server.akkahttp._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import tictactoe.service.GameService
import akka.http.scaladsl.server.Directives._

class GameController(gameService: GameService)(implicit ec: ExecutionContext) {

  val newGame = Endpoints.newGame.toRoute { playerId => gameService.newGame(playerId) }

  val joinGame = Endpoints.joinGame.toRoute {
    case (gameId, playerId) =>
      gameService.joinGame(gameId, playerId)
  }

  val gameState = Endpoints.gameState.toRoute {
    case (gameId, playerId) =>
      gameService.gameState(gameId, playerId)
  }

  val executeMove = Endpoints.executeMove.toRoute {
    case (gameId, playerId, field) =>
      gameService.executeMove(gameId, playerId, field)
  }

  val routes = newGame ~ joinGame ~ gameState ~ executeMove
}
