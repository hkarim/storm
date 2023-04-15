package storm.kafka.model

case class Log(
  partition: Partition,
  offset: Offset, // last offset
  messages: Map[Offset, KafkaMessage],
)

object Log {
  extension (self: Log) {

    def append(message: KafkaMessage): Log = {
      val lastOffset = self.offset.increment
      self.copy(
        offset = lastOffset,
        messages = self.messages.updated(lastOffset, message)
      )
    }

    def poll(offset: Offset): Vector[(Offset, KafkaMessage)] =
      self.messages
        .filter { case (o, _) => o.value >= offset.value }
        .toVector
  }
}
