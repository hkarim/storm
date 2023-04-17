package storm.controller.model

import io.circe.Json
import io.circe.literal.json

object Message {

  def init(messageId: Long, source: String, destination: String, nodeId: String, nodeIds: List[String]): Json =
    json"""{"src": $source, "dest": $destination, "body": {"msg_id": $messageId, "type": "init", "node_id": $nodeId, "node_ids": $nodeIds}}"""

  def echo(messageId: Long, source: String, destination: String, echo: String): Json =
    json"""{"src": $source, "dest": $destination, "body": {"msg_id": $messageId, "type": "echo", "echo": $echo}}"""

}
