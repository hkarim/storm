package storm.model

trait MessageBody {
  def tpe: String
  def messageId: Long // optional in the specs but we need to avoid boxing
}
