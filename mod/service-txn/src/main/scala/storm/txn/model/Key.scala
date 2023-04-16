package storm.txn.model

import io.circe.*

opaque type Key = Int

object Key {
  given Encoder[Key]    = Encoder.encodeInt
  given Decoder[Key]    = Decoder.decodeInt
  given KeyEncoder[Key] = KeyEncoder.encodeKeyInt
  given KeyDecoder[Key] = KeyDecoder.decodeKeyInt
}
