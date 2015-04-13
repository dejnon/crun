package controllers

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import fr.janalyse.ssh.ExecPart
import play.api.libs.json.Json

/**
 * Created by danone on 03/04/15.
 */

object ScreensWebSocketActor {
  def props(out: ActorRef) = Props(new ScreensWebSocketActor(out))
}

class ScreensWebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case cmd: String if cmd.startsWith("{") =>
      println("thanks for command")
      val json = Json.parse(cmd)
      val workerId: String = (json \ "id").asOpt[String].get
      val workerCommand: String = (json \ "cmd").asOpt[String].get
      println(Api.machines)
      if (workerId != null && Api.machines.size > 0) {
        val instance = Api.machines(workerId)
        println(instance)
        println("Connecting to " + instance.getPublicIpAddress )
        sendCommand(instance.getPublicIpAddress, "pwd", out)
      } else if (workerId != null) {
        sendCommand(workerId, workerCommand, out)
      }
//    case _ =>
//      out ! (
//        s"""
//           | {
//           |   "id": "1",
//           |   "cmd": "Sent command: ls hello"
//           | }
//              """.
//          stripMargin)
  }

  def sendCommand(host: String, command: String, out: ActorRef): Unit = {
    val conf = fr.janalyse.ssh.SSHOptions(
      host = host,
      username = "ubuntu",
      sshUserDir = "/Users/danone/.ssh/",
      sshKeyFile = Option("ens14dka-keypair.pem")
    )
    val ssh=jassh.SSH.once(conf) {
      _.run(command, {
        case ExecPart(content) =>
          out ! (
            s"""
               | {
               |   "id": "$host",
               |   "cmd": "$content"
               | }
            """.stripMargin)
        case _ =>
      }).waitForEnd
    }
  }
}