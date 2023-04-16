package storm.txn.model

import io.circe.*

opaque type Value = Option[Int]

object Value {

  given Encoder[Value] = Encoder.encodeOption(Encoder.encodeInt)

  given Decoder[Value] = Decoder.decodeOption(Decoder.decodeInt)

  def empty: Value = Option.empty[Int]

}
