package storm.counter.event

import io.circe.*
import io.circe.syntax.*
import storm.counter.event
import storm.model.*

sealed trait CounterRequestData {
  def widen: CounterRequestData = this
}

object CounterRequestData {

  given DataType[CounterRequestData] =
    DataType.instance[CounterRequestData] {
      case _: Add =>
        Add.Type
      case Read =>
        Read.Type
      case Pull =>
        Pull.Type
      case _: CounterSharedData.AckPull =>
        CounterSharedData.AckPull.Type
    }

  given Decoder[CounterRequestData] =
    Decoder[String].at("type").flatMap {
      case Add.Type =>
        Decoder[Add].map(_.widen)
      case Read.Type =>
        Decoder[Read.type].map(_.widen)
      case Pull.Type =>
        Decoder[Pull.type].map(_.widen)
      case CounterSharedData.AckPull.Type =>
        Decoder[CounterSharedData.AckPull].map(_.widen)
      case otherwise =>
        Decoder.failed(DecodingFailure.apply(s"unrecognized counter request type `$otherwise`", Nil))
    }

  case class Add(delta: Int) extends CounterRequestData

  object Add {
    final val Type: String = "add"

    given Decoder[Add] =
      for {
        delta <- Decoder[Int].at("delta")
      } yield Add(delta = delta)

  }

  case object Read extends CounterRequestData {
    final val Type: String   = "read"
    given Decoder[Read.type] = Decoder.const(Read)

  }

  object Pull extends CounterRequestData {
    final val Type: String = "pull"

    given Encoder[Pull.type] =
      Encoder.instance[Pull.type] { _ =>
        Json.obj()
      }

    given Decoder[Pull.type] = Decoder.const(Pull)

  }

}

sealed trait CounterResponseData

object CounterResponseData {

  given DataType[CounterResponseData] =
    DataType.instance[CounterResponseData] {
      case _: Add =>
        Add.Type
      case _: Read =>
        Read.Type
      case _: CounterSharedData.AckPull =>
        CounterSharedData.AckPull.Type
    }

  given Encoder[CounterResponseData] =
    Encoder.instance[CounterResponseData] {
      case v: Add =>
        Encoder[Add].apply(v)
      case v: Read =>
        Encoder[Read].apply(v)
      case v: CounterSharedData.AckPull =>
        Encoder[CounterSharedData.AckPull].apply(v)
    }

  case class Add(
    inReplyTo: Long,
  ) extends CounterResponseData

  object Add {
    final val Type: String = "add_ok"

    given Encoder[Add] =
      Encoder.instance[Add] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo)
        )
      }
  }

  case class Read(
    inReplyTo: Long,
    value: Int,
  ) extends CounterResponseData

  object Read {
    final val Type: String = "read_ok"

    given Encoder[Read] =
      Encoder.instance[Read] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "value" -> v.value.asJson,
        )
      }
  }

}

sealed trait CounterSharedData
  extends CounterRequestData
  with CounterResponseData

object CounterSharedData {
  case class AckPull(
    inReplyTo: Long,
    value: Map[String, Int],
  ) extends CounterRequestData with CounterResponseData

  object AckPull {
    final val Type: String = "pull_ok"

    given Encoder[AckPull] =
      Encoder.instance[AckPull] { v =>
        Json.obj(
          Message.Encoders.inReplyTo(v.inReplyTo),
          "value" -> v.value.asJson,
        )
      }

    given Decoder[AckPull] =
      for {
        inReplyTo <- Message.Decoders.inReplyTo
        value     <- Decoder[Map[String, Int]].at("value")
      } yield AckPull(
        inReplyTo = inReplyTo,
        value = value,
      )

  }
}
