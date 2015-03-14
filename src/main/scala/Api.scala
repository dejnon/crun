import akka.actor.ActorRef

case class Job(command: String)
case object JobReceived
case class JobsDone(done: Int, total: Int)
case class ResultLine(line: String)
case class JobFinished(out: List[String],err: List[String], status: Int)
