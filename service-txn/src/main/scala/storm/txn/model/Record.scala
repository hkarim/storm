package storm.txn.model

import io.circe.*

case class Record(
  operation: Operation,
  key: Key,
  value: Value
)

object Record {
  given Encoder[Record] =
    Encoder[(Operation, Key, Value)].contramap[Record] { record =>
      (record.operation, record.key, record.value)
    }

  given Decoder[Record] =
    Decoder[(Operation, Key, Value)].map {
      case (operation, key, value) => Record(operation, key, value)
    }
}
