package storm.txn.model

import io.circe.*

opaque type Store = Map[Key, Versioned]

object Store {

  given Encoder[Store] = Encoder.encodeMap[Key, Versioned]
  given Decoder[Store] = Decoder.decodeMap[Key, Versioned]

  def empty: Store = Map.empty[Key, Versioned]

  extension (self: Store) {

    def modify(transaction: Vector[Record]): (Store, Vector[Record]) =
      transaction.foldLeft((self, Vector.empty[Record])) { (acc, record) =>
        val (store, records) = acc
        record match {
          case Record(Operation.Read, k, _) =>
            store.get(k) match {
              case Some(v) =>
                (store, records :+ Record(Operation.Read, k, v.value))
              case None =>
                (store, records :+ Record(Operation.Read, k, Value.empty))
            }
          case Record(Operation.Write, k, v) =>
            store.get(k) match {
              case Some(versioned) =>
                (store.updated(k, versioned.set(v)), records :+ Record(Operation.Write, k, v))
              case None =>
                (store.updated(k, Versioned.fresh(v)), records :+ Record(Operation.Write, k, v))
            }
        }

      }

    def merge(incoming: Store): Store =
      incoming.foldLeft(self) { (acc, next) =>
        val (key, candidateVersioned) = next
        acc.get(key) match {
          case Some(currentVersioned) =>
            if candidateVersioned.succeeds(currentVersioned) then
              acc.updated(key, candidateVersioned)
            else
              acc
          case None =>
            acc.updated(key, candidateVersioned)
        }
      }
  }
}
