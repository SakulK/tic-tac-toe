package tictactoe.service

import java.{util => ju}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.util.Timeout
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.persistence.typed.PersistenceId
import tictactoe.behavior._
import tictactoe.model._
import org.slf4j.LoggerFactory

class GameService(sharding: ClusterSharding)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(this.getClass())

  implicit val timeout = Timeout(3.seconds)

  def newGame(player: PlayerId): Future[Either[JoinError, GameId]] = {
    val newGameId = GameId(ju.UUID.randomUUID().toString())
    joinGame(newGameId, player).map(_.map(_ => newGameId))
  }

  def joinGame(gameId: GameId, player: PlayerId): Future[Either[JoinError, Unit]] =
    withGameRef(gameId) { gameRef => gameRef.ask(JoinGame(player, _)) }

  def gameState(gameId: GameId, player: PlayerId): Future[Either[Unit, BoardState]] =
    withGameRef(gameId) { gameRef => gameRef.ask(GetState(player, _)).map { response => Right(response) } }

  def executeMove(
      gameId: GameId,
      player: PlayerId,
      field: GameField.FieldNumber
  ): Future[Either[MoveError, BoardState]] =
    withGameRef(gameId) { gameRef => gameRef.ask(ExecuteMove(player, field, _)) }

  def initGameSharding(): Unit = {
    logger.info("Initializing game sharding...")
    sharding.init(
      Entity(GameBehavior.TypeKey)(createBehavior =
        entityContext =>
          GameBehavior(
            entityContext.entityId,
            PersistenceId(
              entityContext.entityTypeKey.name,
              entityContext.entityId
            )
          )
      )
    )
  }

  private def withGameRef[T](gameId: GameId)(f: EntityRef[GameCommand] => Future[T]): Future[T] =
    f(sharding.entityRefFor(GameBehavior.TypeKey, gameId.id))
}
