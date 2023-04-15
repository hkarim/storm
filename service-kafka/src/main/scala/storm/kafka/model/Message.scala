package storm.kafka.model

import io.circe.*

opaque type Message = Option[Int]

object Message {
  given Encoder[Message] = Encoder.encodeOption(Encoder.encodeInt)
  given Decoder[Message] = Decoder.decodeOption(Decoder.decodeInt)
}
