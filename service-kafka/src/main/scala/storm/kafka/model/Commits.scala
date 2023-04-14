package storm.kafka.model

opaque type Commits = Map[String, Int]

object Commits {

  def empty: Commits = Map.empty[String, Int]

  extension (self: Commits) {
    def commit(offsets: Map[String, Int]): Commits =
      self ++ offsets

    def list(keys: Vector[String]): Map[String, Int] =
      keys.foldLeft(Map.empty[String, Int]) { (acc, key) =>
        self.get(key) match {
          case Some(offset) =>
            acc.updated(key, offset)
          case None =>
            acc
        }
      }
  }
}
