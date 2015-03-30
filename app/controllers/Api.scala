package controllers

import java.util.{Properties, UUID}

import akka.actor._
import akka.contrib.pattern.{ClusterSingletonProxy, DistributedPubSubMediator}
import aws.HelloCloud
import com.typesafe.config.ConfigFactory
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.libs.json._
//import worker._
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.Reservation
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.amazonaws.services.ec2.model.StopInstancesRequest
import com.amazonaws.services.ec2.model.StopInstancesResult
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

object Api extends Controller {
  var machines = scala.collection.mutable.Map[String, Instance]()
  val helloCloud: HelloCloud = new HelloCloud(HelloCloud.setupCredentials)
  var listWs: ActorRef = null

  def wsListMachines = WebSocket.acceptWithActor[String, String] { request => out =>
    ListWebSocketActor.props(out)
  }

  def updateList = {
    var mach = "["
    Api.machines.foreach({
      case (id: String, instance: Instance) => {
        mach ++= s"""{"id": "${id}", "name": "${id}"},"""
      }
    })
    if (mach.endsWith(",")) {
      mach = mach.dropRight(1)
    }
    mach ++= "]"
    if(listWs != null) {
      listWs ! mach
    }
  }

  def addmachine = Action {
    helloCloud.printRunning

    var instance: Instance = helloCloud.startInstance
    println(machines)

    // Wait while not running
    try {
      System.out.println("Waiting for " + instance.getInstanceId + " to start.")
      while (instance.getState.getName != "running") {
          Thread.sleep(5000)
          instance = helloCloud.updateDescription(instance)
          System.out.println("Current state: " + instance.getState.getName)
      }
      if (instance.getState.getName == "running") {
        System.out.println("Running!")
      }
    }
    catch {
      case e: InterruptedException => {
      }
    }
    machines.put(instance.getInstanceId.toString, instance)
    println(s"$machines")
    updateList
    Ok("OK")
  }

  def removeall = Action {
    helloCloud.printRunning
    machines.foreach {
      case (id: String, instance: Instance) => {
        if (instance != null) {
          var tempInstance = instance
          helloCloud.stopInstance(tempInstance)
          // Wait for instance to stop
          try {
            System.out.println("Waiting for " + tempInstance.getInstanceId + " to stop.")
            while (tempInstance.getState.getName != "stopped") {
              Thread.sleep(5000)
              tempInstance = helloCloud.updateDescription(tempInstance)
              System.out.println("Current state: " + tempInstance.getState.getName)
            }
            if (tempInstance.getState.getName == "stopped") {
              System.out.println("Stopped!")
            }
          }
          catch {
            case e: InterruptedException => {
            }
          }
        }
        else {
          println(s"Didnt close instance $instance")
        }
      }
        println("ok")
    }
    println(s"$machines")
    machines = scala.collection.mutable.Map[String, Instance]()
    updateList
    Ok("OK")
  }

  def wsScreens = WebSocket.acceptWithActor[String, String] { request => out =>
    ScreensWebSocketActor.props(out)
  }

}

object ListWebSocketActor {
  def props(out: ActorRef) = Props(new ListWebSocketActor(out))
}

object ScreensWebSocketActor {
  def props(out: ActorRef) = Props(new ScreensWebSocketActor(out))
}

class ListWebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case msg: String =>
      Api.listWs = out
      Api.updateList
      Thread.sleep(5000)
      out ! (
        """
          | [{
          |   "name": "Alfredo",
          |   "id": "3",
          |   "status": "Avilable"
          | }, {
          |   "name": "Bartus",
          |   "id": "4",
          |   "status": "Working"
          | }, {
          |   "name": "Celina",
          |   "id": "2",
          |   "status": "Offline"
          | }]
        """.stripMargin)
  }
}

class ScreensWebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case cmd: String if cmd.startsWith("{") =>
      out ! "thanks for command"
      val json = Json.parse(cmd)
      val workerId: String = (json \ "id").asOpt[String].get
      val workerCommand: String = (json \ "cmd").asOpt[String].get
      out ! (
        s"""
           | {
           |   "id": "$workerId",
           |   "cmd": "# $workerCommand"
           | }
        """.
          stripMargin)
      println(Api.machines)
      if (workerId != null && Api.machines.size > 0) {
        val instance = Api.machines(workerId)
        println(instance)
        println("Connecting to " + instance.getPublicIpAddress )
        sendCommand(instance.getPublicIpAddress)
      }


    case _ =>
      out ! (
        s"""
          | {
          |   "id": "1",
          |   "cmd": "Sent command: ls hello"
          | }
        """.
          stripMargin)
  }

  def sendCommand(host: String) = {
    try {
      val jsch: JSch = new JSch
      val user: String = "ubuntu"
//      val host: String = "192.18.0.246"
      val port: Int = 22
      val privateKey: String = "/Users/danone/.ssh/ens14dka-keypair.pem"
      jsch.addIdentity(privateKey)
      System.out.println("identity added ")
      val session: Session = jsch.getSession(user, host, port)
      System.out.println("session created.")
      val config: Properties = new Properties
      config.put("StrictHostKeyChecking", "no")
      session.setConfig(config)
      session.connect
      System.out.println("session connected.....")
      val channel: Channel = session.openChannel("sftp")
      channel.setInputStream(System.in)
      channel.setOutputStream(System.out)
      channel.connect
      System.out.println("shell channel connected....")
      val c: ChannelSftp = channel.asInstanceOf[ChannelSftp]
      val fileName: String = "test.txt"
      c.put(fileName, "./in/")
      c.exit
      System.out.println("done")
    }
    catch {
      case e: Exception => {
        System.err.println(e)
      }
    }
  }

}