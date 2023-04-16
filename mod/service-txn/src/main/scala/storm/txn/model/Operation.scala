package storm.txn.model

import io.circe.*
import io.circe.syntax.*

// enums doesn't work currently in 3.3.0-RC3
// https://github.com/lampepfl/dotty/issues/16878
//enum Operation {
//  case Read
//  case Write
//}

sealed trait Operation

object Operation {
  case object Read  extends Operation
  case object Write extends Operation

  given Encoder[Operation] =
    Encoder.instance[Operation] {
      case Operation.Read  => "r".asJson
      case Operation.Write => "w".asJson
    }

  given Decoder[Operation] =
    Decoder.decodeString.flatMap {
      case "r"       => Decoder.const(Operation.Read)
      case "w"       => Decoder.const(Operation.Write)
      case otherwise => Decoder.failed(DecodingFailure(s"unrecognized Operation `$otherwise`", Nil))
    }
}
