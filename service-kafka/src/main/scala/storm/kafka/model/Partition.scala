package storm.kafka.model

import io.circe.*

opaque type Partition = String
object Partition {
  given Decoder[Partition]    = Decoder.decodeString
  given Encoder[Partition]    = Encoder.encodeString
  given KeyDecoder[Partition] = KeyDecoder.decodeKeyString
  given KeyEncoder[Partition] = KeyEncoder.encodeKeyString
}
