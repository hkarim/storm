package storm.event

import io.circe.*
import io.circe.syntax.*

sealed trait Message {
  def source: String

  def destination: String

  def body: MessageBody
}

sealed trait Request extends Message {
  override def body: RequestBody
}

object Request {

  given Decoder[Request] =
    Decoder.instance { c =>
      c.downField("body").downField("type").focus.flatMap(_.asString) match {
        case None =>
          val failure = DecodingFailure.apply(
            reason = DecodingFailure.Reason.CustomReason.apply("expected body/type field"),
            cursor = c
          )
          Left(failure)
        case Some(RequestBody.Type.InitializationRequest) =>
          Decoder[InitializationRequest].apply(c)
        case Some(otherwise) =>
          val failure = DecodingFailure.apply(
            reason = DecodingFailure.Reason.CustomReason.apply(s"unexpected body/type value `$otherwise`"),
            cursor = c
          )
          Left(failure)
      }
    }

  case class InitializationRequest(
    source: String,
    destination: String,
    body: RequestBody.InitializationRequest,
  ) extends Request

  object InitializationRequest {
    given Decoder[InitializationRequest] = for {
      source <- Decoder[String].at("src")
      destination <- Decoder[String].at("dest")
      body <- Decoder[RequestBody.InitializationRequest].at("body")
    } yield InitializationRequest(
      source = source,
      destination = destination,
      body = body,
    )
  }

  case class EchoRequest(
    source: String,
    destination: String,
    body: RequestBody.EchoRequest,
  ) extends Request

  object EchoRequest {
    given Decoder[EchoRequest] = for {
      source <- Decoder[String].at("src")
      destination <- Decoder[String].at("dest")
      body <- Decoder[RequestBody.EchoRequest].at("body")
    } yield EchoRequest(
      source = source,
      destination = destination,
      body = body,
    )
  }

}

sealed trait Response extends Message {
  override def body: ResponseBody
}

object Response {

  given Encoder[Response] = Encoder.instance {
    case v: InitializationResponse =>
      Encoder[InitializationResponse].apply(v)
    case v: EchoResponse =>
      Encoder[EchoResponse].apply(v)
    case v: ErrorResponse =>
      Encoder[ErrorResponse].apply(v)
  }

  case class InitializationResponse(
    source: String,
    destination: String,
    body: ResponseBody.InitializationResponse,
  ) extends Response

  object InitializationResponse {
    given Encoder[InitializationResponse] = Encoder.instance { v =>
      Json.obj(
        "src" -> v.source.asJson,
        "dest" -> v.destination.asJson,
        "body" -> v.body.asJson
      )
    }
  }

  case class EchoResponse(
    source: String,
    destination: String,
    body: ResponseBody.EchoResponse,
  ) extends Response

  object EchoResponse {
    given Encoder[EchoResponse] = Encoder.instance { v =>
      Json.obj(
        "src" -> v.source.asJson,
        "dest" -> v.destination.asJson,
        "body" -> v.body.asJson
      )
    }
  }

  case class ErrorResponse(
    source: String,
    destination: String,
    body: ResponseBody.ErrorResponse,
  ) extends Response

  object ErrorResponse {
    given Encoder[ErrorResponse] = Encoder.instance { v =>
      Json.obj(
        "src" -> v.source.asJson,
        "dest" -> v.destination.asJson,
        "body" -> v.body.asJson
      )
    }
  }
}



