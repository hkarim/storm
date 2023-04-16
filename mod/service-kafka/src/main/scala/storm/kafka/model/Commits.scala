package storm.kafka.model

opaque type Commits = Map[Partition, Offset]

object Commits {

  def empty: Commits = Map.empty[Partition, Offset]

  extension (self: Commits) {
    def commit(offsets: Map[Partition, Offset]): Commits =
      self ++ offsets

    def list(partitions: Vector[Partition]): Map[Partition, Offset] =
      partitions.foldLeft(Map.empty[Partition, Offset]) { (acc, partition) =>
        self.get(partition) match {
          case Some(offset) =>
            acc.updated(partition, offset)
          case None =>
            acc
        }
      }
  }
}
