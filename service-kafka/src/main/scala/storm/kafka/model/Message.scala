package storm.kafka.model

import io.circe.*

opaque type KafkaMessage = Option[Int]

object KafkaMessage {
  given Encoder[KafkaMessage] = Encoder.encodeOption(Encoder.encodeInt)
  given Decoder[KafkaMessage] = Decoder.decodeOption(Decoder.decodeInt)
}
