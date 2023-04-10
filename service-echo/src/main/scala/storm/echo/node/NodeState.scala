package storm.echo.node

sealed trait NodeState

object NodeState {
  case object Uninitialized extends NodeState

  case class Initialized(
    id: String,
    clusterIds: List[String],
  ) extends NodeState
}


