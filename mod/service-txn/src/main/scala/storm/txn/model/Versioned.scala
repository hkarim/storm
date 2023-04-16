package storm.txn.model
import io.circe.*

sealed trait Versioned {
  def version: Int
  def value: Value
}

object Versioned {

  given Encoder[Versioned] =
    Encoder[(Int, Value)].contramap[Versioned](versioned => (versioned.version, versioned.value))

  given Decoder[Versioned] =
    Decoder[(Int, Value)].map {
      case (version, value) => Default(version, value)
    }

  private final case class Default(
    version: Int,
    value: Value,
  ) extends Versioned

  def fresh(value: Value): Versioned =
    Default(1, value)

  extension (self: Versioned) {

    def set(value: Value): Versioned =
      self match {
        case Default(currentVersion, _) =>
          Default(currentVersion + 1, value)
      }

    def precedes(that: Versioned): Boolean =
      self.version < that.version

    def succeeds(that: Versioned): Boolean =
      self.version > that.version
  }
}
