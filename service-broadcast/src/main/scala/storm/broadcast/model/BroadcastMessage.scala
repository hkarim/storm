package storm.broadcast.model

case class BroadcastMessage(
  source: String,
  destination: String,
  value: Int,
)
