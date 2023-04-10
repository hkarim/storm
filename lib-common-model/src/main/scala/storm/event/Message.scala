package storm.event

import io.circe.*
import io.circe.syntax.*

sealed trait Message[+A <: MessageBody] {
  def source: String

  def destination: String

  def body: A
}

sealed trait Request[+A <: RequestBody] extends Message[A]

object Request {

  case class InitializationRequest(
    source: String,
    destination: String,
    body: RequestBody.InitializationRequest,
  ) extends Request[RequestBody.InitializationRequest]

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

}

sealed trait Response[A <: ResponseBody] extends Message[A]

object Response {
  case class InitializationResponse(
    source: String,
    destination: String,
    body: ResponseBody.InitializationResponse,
  ) extends Response[ResponseBody.InitializationResponse]

  object InitializationResponse {
    given Encoder[InitializationResponse] = Encoder.instance { v =>
      Json.obj(
        "src" -> v.source.asJson,
        "dest" -> v.destination.asJson,
        "body" -> v.body.asJson
      )
    }
  }
}



