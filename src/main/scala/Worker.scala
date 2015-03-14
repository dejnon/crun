import akka.actor.{ActorLogging, Actor}
import akka.actor
import akka.actor._
import scala.sys.process.{ProcessLogger, Process}

class WorkerActor extends Actor with ActorLogging {

  def run(in: String): (List[String], List[String], Int) = {
    val qb = Process(in)
    var out = List[String]()
    var err = List[String]()

    def stdout(s: String): Unit = {
      out ::= s
      sender ! ResultLine(s)
    }

    val exit = qb ! ProcessLogger(stdout, (s) => err ::= s)

    (out.reverse, err.reverse, exit)
  }

  def receive = {
    case Job(cmd) =>
      log.info("Received")
      sender ! JobReceived
      val result = run(cmd)
      sender ! JobFinished.tupled(result)
  }

}
