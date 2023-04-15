package storm.txn.model

opaque type Store = Map[Key, Value]

object Store {

  def empty: Store = Map.empty[Key, Value]

  extension (self: Store) {

    def modify(transaction: Vector[Record]): (Store, Vector[Record]) =
      transaction.foldLeft((self, Vector.empty[Record])) { (acc, record) =>
        val (store, records) = acc
        record match {
          case Record(Operation.Read, k, _) =>
            store.get(k) match {
              case Some(v) =>
                (store, records :+ Record(Operation.Read, k, v))
              case None =>
                (store, records :+ Record(Operation.Read, k, Value.empty))
            }
          case Record(Operation.Write, k, v) =>
            (store.updated(k, v), records :+ Record(Operation.Write, k, v))
        }

      }
  }
}
