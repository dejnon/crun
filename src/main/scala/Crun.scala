import akka.actor._

object Crun extends App {
  val system = ActorSystem("crun")

  println("Initiating objects")
  val master = system.actorOf(Props(new MasterActor), "m1")

  def workers(quantity: Int): List[ActorRef] =
    List.tabulate(quantity)(n =>

  def assignCommands(cmds: List[String], workers: List[ActorRef]): Map[ActorRef, String] =
    (workers zip cmds).toMap

  def schedule(cmds: Map[ActorRef, String]): Any =
    cmds.map { case(worker, cmd) => master.tell(Job(cmd), worker) }

  val cmds = List("ls", "ls ..", "ls /", "ls")
  val workerList = workers(cmds.length)
  schedule(assignCommands(cmds, workerList))

  system.awaitTermination()

}


