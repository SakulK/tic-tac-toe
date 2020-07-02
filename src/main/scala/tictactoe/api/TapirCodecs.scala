package tictactoe.api

import sttp.tapir._
import sttp.tapir.json.circe._
import tictactoe.model._

object TapirCodecs {
  implicit val playerIdCodec: Codec[String, PlayerId, CodecFormat.TextPlain] =
    Codec.string.mapDecode(s => DecodeResult.Value(PlayerId(s)))(_.toString())

  implicit val gameIdCodec: Codec[String, GameId, CodecFormat.TextPlain] =
    Codec.string.mapDecode(s => DecodeResult.Value(GameId(s)))(_.toString())

  implicit val joinErrorCodec: Codec[String, JoinError, CodecFormat.TextPlain] =
    Codec.string.mapDecode(s => DecodeResult.Missing)(_.toString())

  implicit val moveErrorCodec: Codec[String, MoveError, CodecFormat.TextPlain] =
    Codec.string.mapDecode(s => DecodeResult.Missing)(_.toString())
}
