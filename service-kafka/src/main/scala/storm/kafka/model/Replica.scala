package storm.kafka.model

opaque type Replica = Map[Partition, Log]

object Replica {

  def empty: Replica = Map.empty[Partition, Log]

  extension (self: Replica) {

    def put(partition: Partition, message: KafkaMessage): (Replica, Offset) =
      self.get(partition) match {
        case Some(log) =>
          val modifiedLog     = log.append(message)
          val modifiedReplica = self.updated(partition, modifiedLog)
          (modifiedReplica, modifiedLog.offset)
        case None =>
          val newLog          = Log(partition, Offset.one, Map(Offset.one -> message))
          val modifiedReplica = self.updated(partition, newLog)
          (modifiedReplica, newLog.offset)
      }

    def poll(offsets: Map[Partition, Offset]): Map[Partition, Vector[(Offset, KafkaMessage)]] =
      offsets.foldLeft(Map.empty[Partition, Vector[(Offset, KafkaMessage)]]) { (acc, next) =>
        val (partition, offset) = next
        self.get(partition) match {
          case Some(log) =>
            val messages = log.poll(offset)
            acc.updated(partition, messages)
          case None =>
            acc
        }
      }

  }

}
