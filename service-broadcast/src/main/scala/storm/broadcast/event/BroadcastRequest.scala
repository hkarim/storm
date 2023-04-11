package storm.broadcast.event

import storm.model.*

type BroadcastRequest = Request[BroadcastRequestBody]

case class BroadcastRequestBody(
  messageId: Option[Long],
  message: Long,
) extends RequestBody {
  override final val tpe: String = "broadcast"
}


