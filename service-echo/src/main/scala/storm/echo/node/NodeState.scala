package storm.echo.node

sealed trait NodeState {
  def description: String
}

object NodeState {
  case object Uninitialized extends NodeState {
    override final val description: String = "Uninitialized"
  }

  case class Initialized(
    nodeId: String,
    nodeIds: List[String],
  ) extends NodeState {
    override final val description: String = "Initialized"
  }
}


