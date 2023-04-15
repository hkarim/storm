package storm.txn.model

import io.circe.*
import io.circe.syntax.*

enum Operation {
  case Read
  case Write
}

object Operation {
  given Encoder[Operation] =
    Encoder.instance[Operation] {
      case Operation.Read  => "r".asJson
      case Operation.Write => "w".asJson
    }

  given Decoder[Operation] =
    Decoder.decodeString.flatMap {
      case "r" => Decoder.const(Operation.Read)
      case "w" => Decoder.const(Operation.Write)
      case otherwise => Decoder.failed(DecodingFailure(s"unrecognized Operation `$otherwise`", Nil))
    }
}
