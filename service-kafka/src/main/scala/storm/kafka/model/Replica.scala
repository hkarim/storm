package storm.kafka.model

opaque type Replica = Map[String, Log]

object Replica {

  def empty: Replica = Map.empty[String, Log]

  extension (self: Replica) {

    def put(key: String, message: Option[Int]): (Replica, Int) =
      self.get(key) match {
        case Some(log) =>
          val modifiedLog     = log.append(message)
          val modifiedReplica = self.updated(key, modifiedLog)
          // System.err.println(s"[Replica::put] (key found) returning => key: $key, log: $modifiedLog")
          (modifiedReplica, modifiedLog.offset)
        case None =>
          val newLog          = Log(key, 1, Map(1 -> message))
          val modifiedReplica = self.updated(key, newLog)
          // System.err.println(s"[Replica::put] (key not found) returning => key: $key, log: $newLog")
          (modifiedReplica, newLog.offset)
      }

    def poll(offsets: Map[String, Int]): Map[String, Vector[(Int, Option[Int])]] =
      offsets.foldLeft(Map.empty[String, Vector[(Int, Option[Int])]]) { (acc, next) =>
        val (key, offset) = next
        self.get(key) match {
          case Some(log) =>
            val messages = log.query(offset)
            // System.err.println(s"[Replica::poll] (key found) returning => key: $key, messages: $messages")
            acc.updated(key, messages)
          case None =>
            // System.err.println(s"[Replica::poll] (key found) returning => key: $key")
            acc
        }
      }

  }

}
