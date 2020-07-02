package tictactoe

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.akkahttp.SwaggerAkka
import scala.concurrent._
import tictactoe.api._
import tictactoe.service._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import org.slf4j.LoggerFactory
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.actor.typed.scaladsl.Behaviors

object TicTacToeApp extends App {

  val logger = LoggerFactory.getLogger(this.getClass())
  val actorSystem = ActorSystem(Behaviors.empty, "tic-tac-toe-system")
  import actorSystem.executionContext
  val cluster = Cluster(actorSystem)
  val sharding = ClusterSharding(actorSystem)
  val gameService = new GameService(sharding)
  val gameController = new GameController(gameService)
  val openApiYaml = Endpoints.all.toOpenAPI("Tic Tac Toe", "1.0").toYaml
  val swagger = new SwaggerAkka(openApiYaml)

  val routes = gameController.routes ~ swagger.routes

  val hostname = "localhost"
  val port = 8080

  logger.info(s"Joining akka cluster...")
  cluster.manager ! Join(cluster.selfMember.address)
  gameService.initGameSharding()

  logger.info(s"Starting TicTacToe service on http://$hostname:$port")
  implicit val classicSystem = actorSystem.classicSystem
  Http().bindAndHandle(routes, hostname, port)
}
