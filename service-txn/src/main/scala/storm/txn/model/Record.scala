package storm.txn.model

import io.circe.*
import io.circe.syntax.*

case class Record(
  operation: Operation,
  key: Key,
  value: Value
)

object Record {
  given Encoder[Record] =
    Encoder.instance[Record] { v =>
      Json.arr(
        v.operation.asJson,
        v.key.asJson,
        v.value.asJson,
      )
    }

  given Decoder[Record] =
    Decoder[(Operation, Key, Value)].map {
      case (operation, key, value) => Record(operation, key, value)
    }
}
