package tictactoe.api

import io.circe._
import io.circe.generic.semiauto._
import tictactoe.model._

object JsonCodecs {

  implicit val FieldStateEncoder: Encoder[FieldState] = {
    case EmptyField      => Json.fromString("empty")
    case Occupied(MarkX) => Json.fromString("X")
    case Occupied(MarkO) => Json.fromString("O")
  }
  implicit val FieldStateDecoder: Decoder[FieldState] = { cursor =>
    cursor.value.asString
      .map {
        case "empty" => EmptyField
        case "X"     => Occupied(MarkX)
        case "O"     => Occupied(MarkO)
      }
      .map(Right.apply)
      .getOrElse(Left(DecodingFailure("Failed to decode field state", Nil)))
  }

  implicit val BoardStateEncoder: Encoder[BoardState] = deriveEncoder
  implicit val BoardStateDecoder: Decoder[BoardState] = deriveDecoder

  implicit val GameIdEncoder: Encoder[GameId] = deriveEncoder
  implicit val GameIdDecoder: Decoder[GameId] = deriveDecoder
}
