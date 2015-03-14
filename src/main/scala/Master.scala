import akka.actor
import akka.actor._
import scala.sys.process.{ProcessLogger, Process}

class MasterActor extends Actor with ActorLogging {
  def receive = {
    case JobReceived =>
      log.info("Received by master")
    case JobFinished(out, err, e) =>
      log.info(s"M W finished got $out and $err $e")
    case ResultLine(line) =>
      log.info(s"M got $line")
    case Job(cmd) =>
      log.info(s"M got $cmd")
      sender ! Job(cmd)
  }

}
