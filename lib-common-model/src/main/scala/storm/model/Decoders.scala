package storm.model

import io.circe.Decoder

object Decoders {
  
  val messageId: Decoder[Long] = Decoder[Long].at("msg_id")
  val inReplyTo: Decoder[Long] = Decoder[Long].at("in_reply_to")

}
