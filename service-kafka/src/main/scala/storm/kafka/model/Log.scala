package storm.kafka.model

case class Log(
  key: String,
  offset: Int,                     // last offset
  messages: Map[Int, Option[Int]], // offset -> message
)

extension (self: Log) {

  def append(message: Option[Int]): Log = {
    val lastOffset = self.offset + 1
    self.copy(
      offset = lastOffset,
      messages = self.messages.updated(lastOffset, message)
    )
  }

  def query(offset: Int): Vector[(Int, Option[Int])] =
    self.messages
      .filter { case (o, _) => o >= offset }
      .toVector

}
