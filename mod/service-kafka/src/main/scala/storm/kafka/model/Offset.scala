package storm.kafka.model

import io.circe.*

opaque type Offset = Int

object Offset {
  given Encoder[Offset] = Encoder.encodeInt
  given Decoder[Offset] = Decoder.decodeInt
  def one: Offset       = 1
  extension (self: Offset) {
    def increment: Offset = self + 1
    def value: Int        = self
  }
}
